import { Monitor, Observable, Observation, Observations } from "@sipxdao/common";
import { CollectionDatabase } from "../../api/dao/collectionDatabase";
import { DatabaseDocument } from "../../api/dao/databaseDocument";
import { DatabaseManager } from "../../api/dao/databaseManager";
import { Template } from "../../api/dao/template";
import { AbstractDatabase } from "./abstractDatabase";
import { ReferenceHandle } from "../../api/dao/referenceHandle";
import { PropertiesSelector } from "../../api/dao/propertiesSelector";
import { DatabaseProperty } from "../../api/dao/databaseProperty";
import { CollectionProperty } from "../../api/properties/collectionProperty";
import { PropertyTypes } from "../../api/types/propertyType";
import { TemplatedDocument } from "../../api/dao/templatedDocument";
import { configurationServiceFactory } from "@sipxdao/configuration";
import { databaseServiceFactory } from "../../api/dao/databaseServiceFactory";
import { TemplatePathKey } from "../../api/dao/databaseService";
import { log } from "../../api/dao/abstractDatabaseService";
import { ReferenceProperty } from "../../api/properties/referenceProperty";
import { DocumentsProperty } from "../../api/properties/documentsProperty";
import { DatabaseDocumentNameKey } from "../../api/dao/databaseDocument";
import { DatabaseType, DatabaseTypes } from "../../api/types/databaseType";

export class CollectionDatabaseImpl<DerivedDocument extends DatabaseDocument> 
    extends AbstractDatabase<DerivedDocument> implements CollectionDatabase<DerivedDocument> { 

    constructor( databaseManager : DatabaseManager, 
        collectionName : string,
        queryDocumentName: string | undefined,
        documentNames : string[],
        allowRootCollection? : boolean,
        encrypted? : boolean,
        owner? : DatabaseDocument,
        template? : Template<TemplatedDocument>  ) { 
        
        super(DatabaseTypes.Collection as DatabaseType, collectionName, queryDocumentName, documentNames, owner, template );  

        this.databaseManager = databaseManager;

        this.allowRootCollection = !!allowRootCollection;

        this.encrypted = !!encrypted;

        this.onNotify = this.onNotify.bind(this);
        this.timedCollectionRelease = this.timedCollectionRelease.bind(this);

        //log.traceInOut( "("+this.collectionName()+")", "constructor()" );
    }


    newDocument( documentPath? : string ) : DerivedDocument{

        return databaseServiceFactory!.get().databaseFactory.newDocument( this, documentPath ) as DerivedDocument;
    }

    path() : string {

        let path = "";

        if( this.owner() != null ) {

            path += this.owner()!.path();
        }

        path += "/" + this.collectionName();

        return path;
    }

    uri() : string {

        let uri = this.path();

        let variables = "";

        if( this.queryDocumentName() != null  ) {
            variables += DatabaseDocumentNameKey + "=" + this.queryDocumentName();
        }

        if( this.queryTemplatePath() != null  ) {

            variables += (variables.length > 0 ? "&" : "") + TemplatePathKey + "=" + 
                encodeURIComponent( this.queryTemplatePath()! )
        }

        if( variables.length > 0 ) {
            uri += "?" + variables;
        }

        return uri;
    }

    async documents(): Promise<Map<string,DerivedDocument>> {
        log.traceIn( "("+this.collectionName()+")", "documents()");

        try {
            let documents : Map<string,DerivedDocument> = new Map<string,DerivedDocument>();

         if( this.databaseManager.isMonitoringCollection( this ) ) {

                const monitorCache = 
                    CollectionDatabaseImpl._monitorCaches.get( this.uri() ) as Map<string,DerivedDocument>;

                log.traceOut( "("+this.collectionName()+")", "documents()", "from cache" );
                return monitorCache != null ? monitorCache : documents;    
            }
            else {
                documents = await this.databaseManager.collection( this ) as Map<string,DerivedDocument>;

                log.traceOut( "("+this.collectionName()+")", "documents()", "from lookup" );
                return documents;
            }
        } catch( error ) {
            log.warn( "("+this.collectionName()+")", "documents()", "Error reading database objects", error );

            throw new Error( (error as any).message );
        }
    }

    async document(documentPath: string ): Promise<DerivedDocument | undefined> {
        
        //log.traceIn("document()", documentPath );

        try {

            let databaseDocument = this.newDocument( documentPath ) as DerivedDocument;

            const exists = await this.readDocument( databaseDocument );

            if( exists ) {
                    
                //log.traceOut( "document()", databaseDocument.referenceHandle().title);
                return databaseDocument;
            }
            else {
                log.warn( "("+this.collectionName()+")", "document()", "doesn't exist" );
                return undefined;              
            }

        } catch( error ) {

            log.warn( "("+this.collectionName()+")", "document()", "Error reading database object", error );

            throw new Error( (error as any).message );
        }
    }

    async referenceHandles(): Promise<Map<string,ReferenceHandle<DerivedDocument>>> {
        log.traceIn( "("+this.collectionName()+")", "referenceHandles()");

        try {
            let result = new Map<string,ReferenceHandle<DerivedDocument>>();

            if( this.databaseManager.isMonitoringCollection( this ) ) {

                const monitorCache = 
                    CollectionDatabaseImpl._monitorCaches.get( this.uri() ) as Map<string,DerivedDocument>;

                if( monitorCache != null ) {

                    monitorCache!.forEach( ( databaseDocument ) => {

                        const referenceHandle = databaseDocument.referenceHandle() as ReferenceHandle<DerivedDocument>;

                        result.set( referenceHandle.uri, referenceHandle );
                    });
                }

                log.traceOut( "("+this.collectionName()+")", "referenceHandles()", "from cache", result.size );
                return result;    
            }
            else {
                result = await this.databaseManager.referenceHandles( this ) as Map<string,ReferenceHandle<DerivedDocument>>;

                log.traceOut( "("+this.collectionName()+")", "referenceHandles()", result );

                return result;
            }

        } catch( error ) {

            log.warn( "("+this.collectionName()+")", "referenceHandles()", "Error reading database reference handles", error );

            throw new Error( (error as any).message );
        }
    }


    async createDocument( databaseDocument: DerivedDocument): Promise<void> {

        log.traceIn( "("+this.collectionName()+")", "createDocument()", databaseDocument.uri() );

        try {
            await databaseDocument.onCreate();

            await this.databaseManager.createDocument( this, databaseDocument! );

            await databaseDocument.onCreated();

            databaseDocument.clearChanged();

            log.traceOut( "("+this.collectionName()+")", "createDocument()", databaseDocument.uri() );
 
        } catch( error ) {

            log.warn( "("+this.collectionName()+")", "createDocument()", "Error creating database object", error );

            throw new Error( (error as any).message );
        }
    }

    async readDocument(databaseDocument: DerivedDocument ): Promise<boolean> {
        //log.traceIn( "readDocument()", databaseDocument.databasePath() );

        try {
            let exists = false;

            const uri = databaseDocument.uri()!;

            const monitorCache = 
                CollectionDatabaseImpl._monitorCaches.get( this.uri() ) as Map<string,DerivedDocument>;

            if( monitorCache != null && monitorCache.has( this.documentCacheKeyFromUri( uri ) ) ) {

                const cachedDocument = monitorCache.get( this.documentCacheKeyFromUri( uri ) );

                if( cachedDocument != null ) {
                    exists = true;

                    await databaseDocument.copyFrom( cachedDocument );

                    //log.debug( "readDocument()", "read from cache", databaseDocument.referenceHandle().title);
                }
            }
            else {
                exists = await this.databaseManager.readDocument( this, databaseDocument );
                //log.debug( "readDocument()", "read from lookup", databaseDocument.referenceHandle().title);
            }

            databaseDocument.clearChanged();
           
            //log.traceOut( "readDocument()", exists );
            return exists;

        } catch( error ) {

            log.warn( "readDocument()", "Error reading database object", error );

            throw new Error( (error as any).message );
        }
    }

    async updateDocument(databaseDocument: DerivedDocument, force? : boolean ): Promise<void> {
        log.traceIn( "("+this.collectionName()+")", "updateDocument()", databaseDocument.uri(), {force} );

        try {
            if( !force && !databaseDocument.isChanged() ) {
                log.traceOut( "("+this.collectionName()+")", "updateDocument()", "no changes", databaseDocument.uri());
                return;
            } 
            await databaseDocument.onUpdate();

            await this.databaseManager.updateDocument( this, databaseDocument, force );

            await databaseDocument.onUpdated();

            databaseDocument.clearChanged();

            log.traceOut( "("+this.collectionName()+")", "updateDocument()", databaseDocument.uri());

        } catch( error ) {

            log.warn( "("+this.collectionName()+")", "updateDocument()", "Error updating database object", error );

            throw new Error( (error as any).message );
        }
    }

    async deleteDocument( databaseDocument: DerivedDocument ): Promise<void> {

        try {
            log.traceIn( "("+this.collectionName()+")", "deleteDocument()", databaseDocument.uri() );

            const useArchive = configurationServiceFactory!.get().cached( 
                "database", "useArchive" );

            await databaseDocument.onDelete();

            if( !!useArchive ) {
                await this.databaseManager.archiveDocument( this, databaseDocument );

            }
            else {
                await this.databaseManager.deleteDocument( this, databaseDocument );
            }
            await databaseDocument.onDeleted();
    
            log.traceOut( "("+this.collectionName()+")", "deleteDocument()" );

        } catch( error ) {

            log.warn( "("+this.collectionName()+")", "deleteDocument()", "Error deleting database object", error );

            throw new Error( (error as any).message );
        }
    }

    async moveDocument( databaseDocument : DerivedDocument, nextCollectionDatabase : CollectionDatabase<DerivedDocument> ) : Promise<DerivedDocument> {

        log.traceIn( "moveDocument()", databaseDocument.path() );
    
        try {
            if( !nextCollectionDatabase.databaseAccess().allowCreate ) {
                throw new Error( "permissionDenied");
            }

            const previousCollectionPath = this.path();

            const nextCollectionPath = nextCollectionDatabase.path();

            let movedDatabaseDocument = nextCollectionDatabase.newDocument() as DerivedDocument;

            await movedDatabaseDocument.copyProperties( databaseDocument );  // Ensures deep copy

            const includePropertyTypes = movedDatabaseDocument.properties( { 
                includePropertyTypes: [PropertyTypes.Reference]
            } as PropertiesSelector ) as Map<string,ReferenceProperty<DatabaseDocument>>;

            includePropertyTypes.forEach( movedReferenceProperty => {

                const movedReferenceHandle = movedReferenceProperty.value() as ReferenceHandle<DerivedDocument>;

                if( movedReferenceHandle != null ) {

                    movedReferenceHandle.path = movedReferenceHandle.path.replace( previousCollectionPath, nextCollectionPath );

                    movedReferenceProperty.setValue( movedReferenceHandle );
                }
            });

            const movedDocumentsProperties = movedDatabaseDocument.properties( { 
                includePropertyTypes: [
                    PropertyTypes.References,
                    PropertyTypes.SymbolicOwners,
                    PropertyTypes.SymbolicCollection 
                ]
            } as PropertiesSelector ) as Map<string,DocumentsProperty<DatabaseDocument>>;

            movedDocumentsProperties.forEach( movedDocumentsProperty => {

                const movedReferenceHandles = movedDocumentsProperty.value() as Map<string,ReferenceHandle<DerivedDocument>>; 

                if( movedReferenceHandles != null ) {

                    movedReferenceHandles.forEach( movedReferenceHandle => {

                        movedReferenceHandle.path = movedReferenceHandle.path.replace( previousCollectionPath, nextCollectionPath );
                    });

                    movedDocumentsProperty.setValue( movedReferenceHandles );
                }
            });

            await this.databaseManager.createDocument( nextCollectionDatabase, movedDatabaseDocument );

            // Recurring move for subcollections 

            const previousCollectionProperties = databaseDocument.properties( { 
                includePropertyTypes: [PropertyTypes.Collection]
            } as PropertiesSelector ) as Map<string,CollectionProperty<DatabaseDocument>>;

            const movedDatabaseDocuments = new Map<string,DatabaseDocument>();

            for( const previousCollectionProperty of previousCollectionProperties.values() ) {

                const previousCollectionDocuments = await previousCollectionProperty.collection().documents();

                for( const previousCollectionDocument of previousCollectionDocuments.values() ) {

                    if( !movedDatabaseDocuments.has( previousCollectionDocument.path() ) ) {

                        movedDatabaseDocuments.set( previousCollectionDocument.path(), previousCollectionDocument );
                        
                        const nextCollectionDatabase = 
                            databaseServiceFactory!.get().databaseFactory.collectionDatabaseFromDocumentName( 
                            previousCollectionDocument.recordName(), movedDatabaseDocument ) as CollectionDatabase<DatabaseDocument>;

                        await previousCollectionDocument.move( nextCollectionDatabase );
                    }
                } 
            }

            if( this.databaseAccess().allowDelete ) { 
                await this.databaseManager.deleteDocument( this, databaseDocument );
            }
            
            log.traceOut( "moveDocument()", movedDatabaseDocument.uri() );
            return movedDatabaseDocument;
    
        } catch( error ) {
            
            log.warn( "moveDocument()", "Error moving database document", error );
    
            throw new Error( (error as any).message );
        }
    }

    async addProperty( documentPath : string, property : DatabaseProperty<any> ): Promise<void> {

        try {
            log.traceIn( "("+this.collectionName()+")", "addProperty()", documentPath, ")" );

            await this.databaseManager.addProperty( this, documentPath, property );
    
            log.traceOut( "("+this.collectionName()+")", "addProperty()" );

        } catch( error ) {

            log.warn( "("+this.collectionName()+")", "addProperty()", "Error adding database property", error );

            throw new Error( (error as any).message );
        }
    }

    async readProperty( documentPath : string, property : DatabaseProperty<any>): Promise<void> {
        try {
            log.traceIn( "("+this.collectionName()+")", "readProperty()", documentPath, ")" );

            await this.databaseManager.readProperty( this, documentPath, property );
    
            log.traceOut( "("+this.collectionName()+")", "readProperty()" );

        } catch( error ) {

            log.warn( "("+this.collectionName()+")", "readProperty()", "Error reading database property", error );

            throw new Error( (error as any).message );
        }
    }

    async updateProperty(  documentPath : string, property : DatabaseProperty<any>): Promise<void> {
        try {
            log.traceIn( "("+this.collectionName()+")", "updateProperty()", documentPath, ")" );

            await this.databaseManager.updateProperty( this, documentPath, property );
    
            log.traceOut( "("+this.collectionName()+")", "updateProperty()" );

        } catch( error ) {

            log.warn( "("+this.collectionName()+")", "updateProperty()", "Error updating database property", error );

            throw new Error( (error as any).message );
        }
    }

    async removeProperty(  documentPath : string, property : DatabaseProperty<any> ): Promise<void> {
        try {
            log.traceIn( "("+this.collectionName()+")", "removeProperty()", documentPath, ")" );

            await this.databaseManager.removeProperty( this, documentPath, property );
    
            log.traceOut( "("+this.collectionName()+")", "removeProperty()" );

        } catch( error ) {

            log.warn( "("+this.collectionName()+")", "removeProperty()", "Error removing database property", error );

            throw new Error( (error as any).message );
        }  
    }

    onNotify = async (observable : Observable, 
        observation : Observation, 
        objectId? : string, 
        object? : any ) : Promise<void> => {
            
        //log.traceIn( "onNotify()", this._observers, Observation[observation], objectId );

        try {
            const monitorCache =
                CollectionDatabaseImpl._monitorCaches.get(this.uri()) as Map<string, DerivedDocument>;

            let result :  Map<string, DerivedDocument>;

            if (monitorCache != null) {

                switch (observation) {

                    case Observations.Create:
                    case Observations.Update:
                    {
                        if( databaseServiceFactory!.get().databaseFactory.equalUris( objectId!, this.uri() ) ) {
                            
                            const initialResult = object as Map<string,DerivedDocument>;

                            if( initialResult != null ) {
                                
                                for( const databaseDocument of initialResult.values() ) {

                                    monitorCache.set( this.documentCacheKeyFromUri( databaseDocument.uri() ), databaseDocument );
                                }
                            }
                        }
                        else {
                            const databaseDocument = object as DerivedDocument;

                            const uri = databaseDocument.uri();
    
                            monitorCache.set( this.documentCacheKeyFromUri( uri ), databaseDocument );
                        }
                        result = monitorCache;
                        break; 
                    }
                    case Observations.Delete:
                    {
                        //log.debug("onNotify()", "deleting", objectId);
                        monitorCache.delete( this.documentCacheKeyFromUri( objectId! ) );

                        result = new Map<string,DerivedDocument>();
                        
                        result.set( objectId!, object as DerivedDocument );
                        break;
                    }
                    default:
                        throw new Error("Unrecognized observation: " + observation);
                }

                this.notifyMonitors( observation, result );
            }


            //log.traceOut( "onNotify()" );

        } catch( error ) {

            log.warn( "("+this.collectionName()+")", "onNotify()", "Error notifying database object", error );

            throw new Error( (error as any).message );
        }
    }

    protected async monitor( newMonitor : Monitor ): Promise<void> {

        //log.traceIn("(" + this.collectionName()+ ")", "monitor()", newMonitor.objectIdsFilter );

        try {
            if (newMonitor.objectIdsFilter == null || newMonitor.objectIdsFilter.length === 0) {
                
                const releaseTimeout =
                    CollectionDatabaseImpl._releaseTimeoutIds.get(this.uri());

                if (releaseTimeout != null) {

                    clearTimeout(releaseTimeout);

                    CollectionDatabaseImpl._releaseTimeoutIds.delete(this.uri());
                }
            }

            let monitorCache = 
                CollectionDatabaseImpl._monitorCaches.get( this.uri() ) as Map<string,DerivedDocument>;

            if ( !this.databaseManager.isMonitoringCollection(this) ) {

                if( newMonitor.objectIdsFilter == null ) {

                    await this.databaseManager.releaseAllDocuments(this);

                    monitorCache = new Map<string,DerivedDocument>();

                    CollectionDatabaseImpl._monitorCaches.set( this.uri(), monitorCache );
                    
                    await this.databaseManager.monitorCollection(this);

                    //log.debug("(" + this.collectionName()+ ")", "monitor()", "Started monitoring entire collection");
                }                     
                else {

                    if (monitorCache == null) {

                        log.debug("(" + this.collectionName()+ ")", "monitor()", "Empty monitor cache, creating");

                        monitorCache = new Map<string, DerivedDocument>();

                        CollectionDatabaseImpl._monitorCaches.set(this.uri(), monitorCache);
                    }

                    //log.debug("(" + this.collectionName()+ ")", "monitor()", "document paths", newMonitor.objectIdsFilter );
                    
                    for( const documentPath of newMonitor.objectIdsFilter ) {

                        if( !monitorCache.has( this.documentCacheKeyFromUri( documentPath ) ) ) {

                            await this.databaseManager.monitorDocuments(this, [documentPath]);
                        }
                    }
                }
            } 

            if( monitorCache == null ) {
                throw new Error( "Inconsistent cache states with database manager")
            }

            this.notifyMonitor( newMonitor, Observations.Create as Observation, monitorCache );

            //log.traceOut("(" + this.collectionName()+ ")", "monitor()");

        } catch (error) {
            log.warn("Error starting monitoring from firestore for", this.collectionName(), error);

            throw new Error( (error as any).message );
        }
    }


    protected async release(observationFilter?: Observation[], objectIdsFilter?: string[]): Promise<void> {

        //log.traceIn("(" + this.collectionName()+ ")", "release()", observationFilter, objectIdsFilter);

        try {
            if (objectIdsFilter == null || objectIdsFilter.length === 0) {

                let releaseTimeout =
                    CollectionDatabaseImpl._releaseTimeoutIds.get(this.uri());

                if (releaseTimeout != null) {
                    //log.traceOut("(" + this.collectionName()+ ")", "release()", "Already being released");
                    return;
                }

                const cacheReleaseSeconds = +configurationServiceFactory!.get().cached(
                    "database", "cacheReleaseSeconds")!;

                if (isNaN(cacheReleaseSeconds)) {
                    throw new Error("Invalid cache release timeout: " + cacheReleaseSeconds);
                }

                releaseTimeout = setTimeout(this.timedCollectionRelease, cacheReleaseSeconds * 1000);

                CollectionDatabaseImpl._releaseTimeoutIds.set(this.uri(), releaseTimeout );

                //log.traceOut("(" + this.collectionName()+ ")", "release()", "Stopped monitoring all ids for all observations");
                return;
            }

            if (objectIdsFilter != null && objectIdsFilter.length > 0) {

                if( !this.databaseManager.isMonitoringCollection( this ) ) {

                    await this.databaseManager.releaseDocuments(this, objectIdsFilter);

                    const monitorCache = CollectionDatabaseImpl._monitorCaches.get( this.uri() );

                    if( monitorCache != null ) {
                        objectIdsFilter.forEach( (key) => {

                            monitorCache!.delete( key.split("?")[0] );
                        });

                        if( monitorCache.size === 0 ) {
                            CollectionDatabaseImpl._monitorCaches.delete( this.uri() );
                        }
                    }
                }

                //log.traceOut("(" + this.collectionName()+ ")", "release()", "Stopped monitoring filtered ids for all observations");
                return;
            }

        } catch (error) {

            log.warn("Error stopping monitoring from firestore for", this.collectionName(), error);

            throw new Error( (error as any).message );
        }
    }

    private timedCollectionRelease = async (): Promise<void> => {

        //log.traceIn("(" + this.collectionName()+ ")", "timedCollectionRelease()", observationFilter, objectIdsFilter);

        try {

                await this.databaseManager.releaseCollection(this);

                await this.databaseManager.releaseAllDocuments(this);

                CollectionDatabaseImpl._monitorCaches.delete( this.uri() );

                //log.traceOut("(" + this.collectionName()+ ")", "timedCollectionRelease()", "Stopped monitoring all ids for all observations");

        } catch (error) {

            log.warn("Error stopping monitoring from firestore for", this.collectionName(), error);
        }
    }


    fromRecord( record : Record<string, any> ) : DerivedDocument {

        //log.traceIn( "("+this.collectionName()+")", "fromRecord()", data );

        try {

            let databaseDocument = this.newDocument( record["id"] );

            databaseDocument.fromRecord( record );
    
            //log.traceOut( "("+this.collectionName()+")", "fromRecord()", databaseDocument );
            return databaseDocument;

        } catch( error ) {

            log.warn( "("+this.collectionName()+")", "fromRecord()", "Error reading database document", error );

            throw new Error( (error as any).message );
        }
    }

    readonly databaseManager : DatabaseManager;

    readonly allowRootCollection : boolean;

    readonly encrypted : boolean;

    private static _monitorCaches = new Map<string,Map<string,DatabaseDocument>>();

    private static _releaseTimeoutIds = new Map<string,number>();

}