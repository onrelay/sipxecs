import {  Monitor, Observable, Observation, Observations } from "@sipxdao/common";
import { CollectionGroupDatabase } from "../../api/dao/collectionGroupDatabase";
import { DatabaseDocument } from "../../api/dao/databaseDocument";
import { DatabaseManager } from "../../api/dao/databaseManager";
import { Template } from "../../api/dao/template";
import { AbstractDatabase } from "./abstractDatabase";
import { ReferenceHandle } from "../../api/dao/referenceHandle";
import { TemplatedDocument } from "../../api/dao/templatedDocument";
import { CollectionGroupPathSuffix, NewObjectId, TemplatePathKey } from "../../api/dao/databaseService";
import { DatabaseDocumentNameKey } from "../../api/dao/databaseDocument";
import { databaseServiceFactory } from "../../api/dao/databaseServiceFactory";
import { log } from "../../api/dao/abstractDatabaseService";
import { configurationServiceFactory } from "@sipxdao/configuration";
import { DatabaseType, DatabaseTypes } from "../../api/types/databaseType";

export class CollectionGroupDatabaseImpl<DerivedDocument extends DatabaseDocument> 
    extends AbstractDatabase<DerivedDocument> implements CollectionGroupDatabase<DerivedDocument> {

    constructor( databaseManager : DatabaseManager, 
        collectionName : string,
        queryDocumentName: string | undefined,
        documentNames : string[],
        allowRootCollection? : boolean,
        encrypted? : boolean,
        owner? : DatabaseDocument,
        template? : Template<TemplatedDocument> ) { 

        super(DatabaseTypes.CollectionGroup as DatabaseType, collectionName, queryDocumentName, documentNames, owner, template ); 

        this.databaseManager = databaseManager;

        this.allowRootCollection = !!allowRootCollection;

        this.encrypted = !!encrypted;

        this.onNotify = this.onNotify.bind(this);
        this.timedCollectionGroupRelease = this.timedCollectionGroupRelease.bind(this);

        //log.traceInOut( "("+this.collectionName()+")", "constructor()" );
    }

    path() : string {

        let path = "";

        if( this.owner() != null ) {

            path += this.owner()!.path();
        }

        path += "/" + this.collectionName()+ CollectionGroupPathSuffix;

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
        log.traceIn( "("+this.collectionName()+")", "documents()" );

        try {
            let documents : Map<string,DerivedDocument> = new Map<string,DerivedDocument>();

            if( this.databaseManager.isMonitoringCollectionGroup( this ) ) {

                const monitorCache = 
                    CollectionGroupDatabaseImpl._monitorCaches.get( this.uri() ) as Map<string, DerivedDocument>;               

                log.traceOut( "("+this.collectionName()+")", "documents()", "from cache" );
                return monitorCache!;    
            }
            else {
                documents = await this.databaseManager.collectionGroup( this ) as Map<string,DerivedDocument>;

                log.traceOut( "("+this.collectionName()+")", "documents()", "from lookup" );
                return documents;
            }
        } catch( error ) {
            log.warn( "("+this.collectionName()+")", "documents()", "Error reading database objects", error );

            throw new Error( (error as any).message );
        }
    }

    newDocument(documentPath?: string ): DerivedDocument {

        //log.traceIn( "("+this.collectionName()+")", "newDocument()", documentPath );

        try {

            let databaseDocument;

            if( documentPath != null ) { 
                databaseDocument = 
                    databaseServiceFactory!.get().databaseFactory.newDocumentFromUri( documentPath ) as DerivedDocument;
            }
            else {

                let newDocumentPath =  "";

                if( this.owner() != null ) {
        
                    newDocumentPath += this.owner()!.path();
                }

                newDocumentPath += "/" + this.collectionName()+ "/" + NewObjectId;

                let variables = "";

                if( this.queryDocumentName() != null  ) {
                    variables += DatabaseDocumentNameKey + "=" + this.queryDocumentName();
                }
        
                if( this.queryTemplatePath() != null  ) {
        
                    variables += (variables.length > 0 ? "&" : "") + TemplatePathKey + "=" + 
                        encodeURIComponent( this.queryTemplatePath()! )
                }
        
                const url = newDocumentPath + (variables.length > 0 ? "?" : "") + variables;
        
                databaseDocument = 
                    databaseServiceFactory!.get().databaseFactory.newDocumentFromUri( url ) as DerivedDocument;
            }

            //log.traceOut( "("+this.collectionName()+")", "newDocument()", document );
            return databaseDocument;
    
        } catch( error ) {

            log.warn( "("+this.collectionName()+")", "newDocument()", "Error reading database object", error );

            throw new Error( (error as any).message );
        }
    }

    async document(documentPath: string ): Promise<DerivedDocument | undefined> {

        //log.traceIn( "document()", documentPath );

        try {

            let databaseDocument = 
                databaseServiceFactory!.get().databaseFactory.newDocumentFromUri( documentPath ) as DerivedDocument;

            await this.readDocument( databaseDocument );

            //log.traceOut( "("+this.collectionName()+")", "document()" );
               
            return databaseDocument;

        } catch( error ) {

            log.warn( "document()", "Error reading database object", error );

            throw new Error( (error as any).message );
        }
    }


    async referenceHandles(): Promise<Map<string,ReferenceHandle<DerivedDocument>>> {
        log.traceIn( "("+this.collectionName()+")", "referenceHandles()");

        try {
            let result = new Map<string,ReferenceHandle<DerivedDocument>>();

            if( this.databaseManager.isMonitoringCollectionGroup( this ) ) {

                const monitorCache = 
                    CollectionGroupDatabaseImpl._monitorCaches.get( this.uri() ) as Map<string, DerivedDocument>;

                if( monitorCache != null ) {

                    monitorCache!.forEach( ( databaseDocument ) => {

                        const referenceHandle = databaseDocument.referenceHandle() as ReferenceHandle<DerivedDocument>;

                        result.set( referenceHandle.uri, referenceHandle );
                    });
                }

                log.traceOut( "("+this.collectionName()+")", "referenceHandles()", "from cache", result ); 
                return result;    
            }
            else {
                result = await this.databaseManager.groupReferenceHandles( this ) as Map<string,ReferenceHandle<DerivedDocument>>;

                log.traceOut( "("+this.collectionName()+")", "referenceHandles()", result );

                return result;
            }

        } catch( error ) {

            log.warn( "("+this.collectionName()+")", "referenceHandles()", "Error reading database reference handles", error );

            throw new Error( (error as any).message );
        }
    }


    async readDocument(databaseDocument: DerivedDocument ): Promise<boolean> {
        //log.traceIn( "readDocument()", databaseDocument.databasePath(), monitor );

        try {
            let exists = false;

            const documentUri = databaseDocument.uri();

            const monitorCache = 
                CollectionGroupDatabaseImpl._monitorCaches.get( this.uri() ) as Map<string, DerivedDocument>;

            if( monitorCache != null && monitorCache.has( this.documentCacheKeyFromUri( documentUri ) ) ) {

                const cachedDocument = monitorCache.get( this.documentCacheKeyFromUri( documentUri ) );

                if( cachedDocument != null ) { 
                    exists = true;

                    await databaseDocument.copyFrom( cachedDocument );

                    //log.debug( "("+this.collectionName()+")", "readDocument()", "read from cache", databaseDocument.referenceHandle().title);
                }
            }
            else {

                const databaseDocument = await databaseServiceFactory!.get().databaseFactory.documentFromUri( documentUri );

                if( databaseDocument != null ) {

                    exists = true;

                    await databaseDocument.copyFrom( databaseDocument );

                    //log.debug( "("+this.collectionName()+")", "readDocument()", "read from database", databaseDocument.referenceHandle().title);
                }
            }
            

           //log.traceOut( "readDocument()", exists );
            return exists;

        } catch( error ) {

            log.warn( "("+this.collectionName()+")", "readDocument()", "Error reading database object", error );

            throw new Error( (error as any).message );
        }
    }

    onNotify = async (observable : Observable, 
        observation : Observation, 
        objectId? : string, 
        object? : any) : Promise<void> => {
            
        //log.traceIn( "onNotify()", Observation[observation], objectId );

        try {

            const monitorCache = 
                CollectionGroupDatabaseImpl._monitorCaches.get( this.uri() ) as Map<string, DerivedDocument>;

            if( monitorCache != null ) {

                let result :  Map<string, DerivedDocument>;

                switch( observation ) {

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
    
                            monitorCache.set( this.documentCacheKeyFromUri( databaseDocument.uri() ), databaseDocument );
                        } 

                        result = monitorCache;

                        break;
                    }
                    case Observations.Delete:
                    {
                        monitorCache!.delete( this.documentCacheKeyFromUri( objectId! ));

                        result = new Map<string,DerivedDocument>();

                        result.set( objectId!, object as DerivedDocument );

                        break;
                    }
                    default: 
                      throw new Error( "Unrecognized observation: " + observation );                  
                }

                this.notifyMonitors( observation, result );
            }
   
            //log.traceOut( "onNotify()" ); 

        } catch( error ) {

            log.warn( "onNotify()", "Error deleting database object", error );

            throw new Error( (error as any).message );
        }
    }

    protected async monitor( newMonitor : Monitor ): Promise<void> {

        //log.traceIn("(" + this.collectionName()+ ")", "monitor()");

        try {
            const releaseTimeout =
                CollectionGroupDatabaseImpl._releaseTimeoutIds.get(this.uri());

            if (releaseTimeout != null) {

                clearTimeout(releaseTimeout);

                CollectionGroupDatabaseImpl._releaseTimeoutIds.delete(this.uri());
            }

            let monitorCache =
                CollectionGroupDatabaseImpl._monitorCaches.get( this.uri() ) as Map<string, DerivedDocument>;

            if ( !this.databaseManager.isMonitoringCollectionGroup(this) ) {

                monitorCache = new Map<string, DerivedDocument>(); 

                CollectionGroupDatabaseImpl._monitorCaches.set( this.uri(), monitorCache );

                await this.databaseManager.monitorCollectionGroup(this);
                
                //log.debug("(" + this.collectionName()+ ")", "monitor()", "Started monitoring entire collection group");
            }

            if( monitorCache == null ) {
                throw new Error( "Inconsistent cache states with database manager")
            }

            this.notifyMonitor( newMonitor, Observations.Create as Observation, monitorCache );
   
            //log.traceOut("(" + this.collectionName()+ ")", "monitor()", "result size", monitorCache.size );
            return;

        } catch (error) {
            log.warn("Error starting monitoring from firestore for", this.collectionName(), error);

            throw new Error( (error as any).message );
        }
    }

    protected async release(): Promise<void> {

        //log.traceIn("(" + this.collectionName()+ ")", "release()");

        try {

            let releaseTimeout =
                CollectionGroupDatabaseImpl._releaseTimeoutIds.get(this.uri());

            if (releaseTimeout != null) {
                //log.traceOut("(" + this.collectionName()+ ")", "release()", "Already being released");
                return;
            }

            const cacheReleaseSeconds = +configurationServiceFactory!.get().cached(
                "database", "cacheReleaseSeconds")!;

            if (isNaN(cacheReleaseSeconds)) {
                throw new Error("Invalid cache release timeout: " + cacheReleaseSeconds);
            }

            releaseTimeout = setTimeout(this.timedCollectionGroupRelease, cacheReleaseSeconds * 1000 );

            CollectionGroupDatabaseImpl._releaseTimeoutIds.set(this.uri(), releaseTimeout );

            //log.traceOut("(" + this.collectionName()+ ")", "release()", "Stopped monitoring all ids for all observations");

        } catch (error) {

            log.warn("Error stopping monitoring collection group", this.collectionName(), error);

            throw new Error( (error as any).message );
        }
    }

    private timedCollectionGroupRelease = async (): Promise<void> => {

        //log.traceIn("(" + this.collectionName()+ ")", "timedCollectionGroupRelease()", observationFilter, objectIdsFilter);

        try {

            await this.databaseManager.releaseCollectionGroup(this);

            CollectionGroupDatabaseImpl._monitorCaches.delete( this.uri() );

            //log.traceOut("(" + this.collectionName()+ ")", "timedCollectionGroupRelease()", "Stopped monitoring all ids for all observations");

        } catch (error) {

            log.warn("Error stopping monitoring from firestore for", this.collectionName(), error);
        }
    }

    readonly databaseManager : DatabaseManager;

    readonly allowRootCollection : boolean;

    readonly encrypted : boolean;

    protected static _monitorCaches = new Map<string,Map<string,DatabaseDocument>>();

    private static _releaseTimeoutIds = new Map<string,number>();


} 