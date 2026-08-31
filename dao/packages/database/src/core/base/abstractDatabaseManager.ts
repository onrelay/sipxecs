import { DatabaseRecord } from "../types/databaseRecord";
import { AbstractObservable, Monitor, Observation } from "@dao/common";
import { DatabaseManager } from "../spec/databaseManager";
import { DatabaseConverter } from "../spec/databaseConverter";
import { CollectionDatabase } from "../spec/collectionDatabase";
import { DatabaseDocument, EndDatePropertyKey } from "../spec/databaseDocument";
import { DatabaseProperty } from "../spec/databaseProperty";
import { CollectionGroupDatabase } from "../spec/collectionGroupDatabase";
import { Database } from "../spec/database";
import { ReferenceHandle } from "../impl/referenceHandle";
import { log } from "./abstractDatabaseService";
import { databaseServiceFactory } from "../impl/databaseServiceFactory";
import { ChangesCollection } from "../spec/databaseService";
import { ChangeTypes } from "../defs/changeType";
import { User } from "../../documents/spec/user";
import { Change, ChangeTypePropertyKey } from "../../documents/spec/change";
import { DatabaseFilter } from "../types/databaseFilter";
import { Comparator, Comparators } from "../defs/comparator";
import { PropertyTypes } from "../defs/propertyType";
import { PropertiesSelector } from "../types/propertiesSelector";
import { CollectionProperty } from "../../properties/spec/collectionProperty";
import { DatabaseTypes } from "../defs/databaseType";

export abstract class AbstractDatabaseManager extends AbstractObservable implements DatabaseManager {

    constructor( params: { 
        clientEncryption : boolean,
        converter : DatabaseConverter
     } ) {

        super();

        //log.traceIn( "constructor()");

        try {

            this.clientEncryption = params.clientEncryption;

            this.converter = params.converter;

            //log.traceOut( "constructor()");

        } catch( error ) {
            log.warn( "constructor()", "Error initializing database manager", error );

            throw new Error( (error as any).message );
        }
    }


    async documents( collectionDatabase : CollectionDatabase<DatabaseDocument>, documentPaths : string[] ) : Promise<Map<string,DatabaseDocument>> {

        log.traceIn( "("+collectionDatabase.collectionName()+")", "documents()", documentPaths );

        try {
            if( !collectionDatabase.databaseAccess().allowRead ) {
                throw new Error( "No read access to collection " + collectionDatabase.collectionName() );
            }

            let initialResult = new Map<string,DatabaseDocument>();

            if( documentPaths != null ) {
                documentPaths.forEach( async (documentPath) => { 

                    let databaseDocument : DatabaseDocument = collectionDatabase.newDocument( documentPath );

                    await this.readDocument( collectionDatabase, databaseDocument );

                    initialResult.set( documentPath, databaseDocument );
                } );
            }

            log.traceOut( "("+collectionDatabase.collectionName()+")", "documents()", initialResult );
            return initialResult;
            
        } catch( error ) {

            log.warn( "Error reading documents", collectionDatabase.collectionName(), error );
            
            throw new Error( (error as any).message );
        }
    }


    async addProperty( collectionDatabase : CollectionDatabase<DatabaseDocument>, 
        documentPath : string,
        property : DatabaseProperty<any> ): Promise<void> {

        try {
            log.traceIn( "("+collectionDatabase.collectionName()+")", "addProperty()", );

            let storedDocument = collectionDatabase.newDocument( documentPath );

            await this.readDocument( collectionDatabase, storedDocument );

            let storedDocumentData = await storedDocument.toRecord();

            await property.toRecord( storedDocumentData );

            storedDocument.fromRecord( storedDocumentData );

            await this.updateDocument( collectionDatabase, storedDocument );
    
            log.traceOut( "("+collectionDatabase.collectionName()+")", "addProperty()" );

        } catch( error ) {

            log.warn( "("+collectionDatabase.collectionName()+")", "addProperty()", "Error reading database property", error );

            throw new Error( (error as any).message );
        }
    }

    async readProperty( collectionDatabase : CollectionDatabase<DatabaseDocument>, 
        documentPath : string,
        property : DatabaseProperty<any>): Promise<void> {

        try {
            log.traceIn( "("+collectionDatabase.collectionName()+")", "readProperty()", );

            let storedDocument = collectionDatabase.newDocument( documentPath );

            await this.readDocument( collectionDatabase, storedDocument );

            let storedDocumentData = await storedDocument.toRecord();

            property.fromRecord( storedDocumentData[property.key()] as DatabaseRecord );

            log.traceOut( "("+collectionDatabase.collectionName()+")", "readProperty()" );

        } catch( error ) {

            log.warn( "("+collectionDatabase.collectionName()+")", "addProperty()", "Error reading database property", error );

            throw new Error( (error as any).message );
        }

    }

    async updateProperty( collectionDatabase : CollectionDatabase<DatabaseDocument>, 
        documentPath : string,
        property : DatabaseProperty<any>): Promise<void> {

        try {
            log.traceIn( "("+collectionDatabase.collectionName()+")", "updateProperty()", );

            let storedDocument = collectionDatabase.newDocument( documentPath );

            await this.readDocument( collectionDatabase, storedDocument );

            let storedDocumentData = await storedDocument.toRecord();

            await property.toRecord( storedDocumentData );

            storedDocument.fromRecord( storedDocumentData )

            await this.updateDocument( collectionDatabase, storedDocument );
    
            log.traceOut( "("+collectionDatabase.collectionName()+")", "updateProperty()" );

        } catch( error ) {

            log.warn( "("+collectionDatabase.collectionName()+")", "updateProperty()", "Error updating database property", error );

            throw new Error( (error as any).message );
        }      
    }

    async removeProperty( collectionDatabase : CollectionDatabase<DatabaseDocument>, 
        documentPath : string,
        property : DatabaseProperty<any> ): Promise<void> {

        try {
            log.traceIn( "("+collectionDatabase.collectionName()+")", "updateProperty()", );

            let storedDocument = collectionDatabase.newDocument( documentPath );

            await this.readDocument( collectionDatabase, storedDocument );

            let storedDocumentData = await storedDocument.toRecord();

            delete storedDocumentData[property.key()];

            storedDocument.fromRecord( storedDocumentData )

            await this.updateDocument( collectionDatabase, storedDocument );
    
            log.traceOut( "("+collectionDatabase.collectionName()+")", "updateProperty()" );

        } catch( error ) {

            log.warn( "("+collectionDatabase.collectionName()+")", "updateProperty()", "Error updating database property", error );

            throw new Error( (error as any).message );
        }      
    }

    protected async monitor( newMonitor : Monitor): Promise<void> {

        throw new Error( "Database should be monitored via a collection or collection group")
    }

    protected async release(observationFilter?: Observation[], objectIdsFilter?: string[]): Promise<void> {
        throw new Error( "Database should be monitored via a collection or collection group") 
    } 

    isMonitoringDatabase( database : Database<DatabaseDocument> ) : boolean {

        log.traceIn( "("+database.collectionName()+")", "isMonitoringDatabase()" );

        let databaseMonitors;

        if( database.databaseType == DatabaseTypes.Collection ) {

            databaseMonitors = this._collectionMonitors;
        }
        else if( database.databaseType == DatabaseTypes.CollectionGroup ) {

            databaseMonitors = this._collectionGroupMonitors;
        }
        else {
            log.warn( "Database type cannot be monitored " + database.databaseType );
            return false;
        }

        const result = databaseMonitors.get(database.uri()) !== undefined;

        log.traceOut( "("+database.collectionName()+")", "isMonitoringDatabase()", result );
        return result;
    }

    async monitorCollection( collectionDatabase : CollectionDatabase<DatabaseDocument> ): Promise<void> {
        await this.monitorDatabase( collectionDatabase );
    }

    async releaseCollection( collectionDatabase : CollectionDatabase<DatabaseDocument> ): Promise<void> {
        await this.releaseDatabase( collectionDatabase );
    }

    isMonitoringCollection( collectionDatabase : CollectionDatabase<DatabaseDocument> ): boolean {
        return this.isMonitoringDatabase( collectionDatabase );
    }

    async monitorCollectionGroup( collectionGroupDatabase : CollectionGroupDatabase<DatabaseDocument> ): Promise<void> {
        await this.monitorDatabase( collectionGroupDatabase );
    }

    async releaseCollectionGroup( collectionGroupDatabase : CollectionGroupDatabase<DatabaseDocument> ): Promise<void> {
        await this.releaseDatabase( collectionGroupDatabase );
    }

    isMonitoringCollectionGroup( collectionGroupDatabase : CollectionGroupDatabase<DatabaseDocument> ): boolean {
        return this.isMonitoringDatabase( collectionGroupDatabase );
    }


    isMonitoringDocument( collectionDatabase : CollectionDatabase<DatabaseDocument>, uri : string ) : boolean {

        log.traceIn( "("+collectionDatabase.collectionName()+")", "isMonitoringDocument()", uri );

        let result = false;

        const collectionDocumentMonitors = this._documentMonitors.get(collectionDatabase.path());

        if( collectionDocumentMonitors != null ) {

            result = collectionDocumentMonitors.get( uri ) != null;
        }

        log.traceOut( "("+collectionDatabase.collectionName()+")", "isMonitoringDocument()", result );
        return result;
    }

    documentWithProperty( database : Database<DatabaseDocument>, 
        key : string, 
        value : string ) : Promise<DatabaseDocument | undefined> {

        const properties = new Map<string,string>();
        
        properties.set( key, value );

        return this.documentWithProperties( database, properties );

    }

    async documentWithProperties( 
        database : Database<DatabaseDocument>, 
        properties : Map<string,string> ) : Promise<DatabaseDocument | undefined> {

        log.traceIn( "documentWithProperties()", {properties} );

        try {
            let databaseDocuments = await this.documentsWithProperties( database, properties);

            if( databaseDocuments == null || databaseDocuments.size === 0 ) {

                log.traceOut( "documentWithProperties()", "Not found");
                return undefined;
            }

            if( databaseDocuments.size > 1 ) {
                throw new Error( "Multiple documents with properties: [" + {properties} + "]");
            }

            log.traceOut( "documentWithProperties()", {properties} );
            return Array.from( databaseDocuments.values()! )![0]!;

        } catch( error ) {

            log.warn( "Error reading document with property", error );
            
            throw new Error( "Error querying database database: " + (error as any).message );
        }
    }

    documentsWithProperty( database : Database<DatabaseDocument>, 
        key : string, 
        value : string ) : Promise<Map<string,DatabaseDocument>> {

        const properties = new Map<string,string>();
        
        properties.set( key, value );

        return this.documentsWithProperties( database, properties ); 
    }
 
    async collectionGroup( collectionGroupDatabase : CollectionGroupDatabase<DatabaseDocument> ): Promise<Map<string,DatabaseDocument>> {
        
        log.traceIn( "("+collectionGroupDatabase.collectionName()+")", "collectionGroup()");

        try {

            if( !collectionGroupDatabase.databaseAccess().allowRead ) {
                throw new Error( "permissionDenied" );
            }

            let result = new Map<string,DatabaseDocument>();

            const documentRecords = await this.documentRecords( collectionGroupDatabase );

            if( documentRecords != null ) {

                await Promise.all( Array.from( documentRecords.entries() ).map( async documentRecordEntry => { 

                    try {
                        const documentUri = documentRecordEntry[0];
                        const documentRecord = documentRecordEntry[1];

                        const databaseDocument = 
                            await databaseServiceFactory!.get().databaseFactory.documentFromRecord( 
                                documentUri, documentRecord ) as DatabaseDocument;
                        
                        if (databaseDocument == null) {    
                            throw new Error("Unable to read document from data: " + documentRecord);
                        } 

                        result.set( documentUri, databaseDocument! );


                    } catch (error) {
                        log.warn("Error reading document snapshot", error);
                    }               
                }));
            }

            log.traceOut( "("+collectionGroupDatabase.collectionName()+")", "collectionGroup()", result.size );
            return result; 

        } catch( error ) {

            log.warn( "Error reading collection group", error );

            throw new Error( (error as any).message );
        }
    }

    async groupReferenceHandles( collectionGroupDatabase : CollectionGroupDatabase<DatabaseDocument> ): Promise<Map<string,ReferenceHandle<DatabaseDocument>>> {
        
        log.traceIn( "("+collectionGroupDatabase.collectionName()+")", "referenceHandles()");

        try {
            if( !collectionGroupDatabase.databaseAccess().allowRead ) {
                throw new Error( "permissionDenied" );
            }

            let result = new Map<string,ReferenceHandle<DatabaseDocument>>();

            const documentRecords = await this.documentRecords( collectionGroupDatabase );

            if( documentRecords != null ) {

                await Promise.all( Array.from( documentRecords.entries() ).map( async documentRecordEntry => { 

                    try {
                        const documentPath = documentRecordEntry[0];
                        const rawDatabaseDocument = documentRecordEntry[1];

                        const databaseDocument = 
                            await databaseServiceFactory!.get().databaseFactory.documentFromRecord( 
                                documentPath, rawDatabaseDocument ) as DatabaseDocument;

                        if (databaseDocument == null) {    
                            throw new Error("Unable to read document from data: " + rawDatabaseDocument);
                        } 

                        result.set( documentPath, databaseDocument!.referenceHandle() );


                    } catch (error) {
                        log.warn("Error reading document snapshot", error);
                    }               
                }));
            }

            log.traceOut( "("+collectionGroupDatabase.collectionName()+")", "referenceHandles()", result.size );
            return result; 

        } catch( error ) {

            log.warn( "Error reading reference handles", error );

            throw new Error( (error as any).message );
        }
    }
    
    async collection( collectionDatabase : CollectionDatabase<DatabaseDocument> ): Promise<Map<string,DatabaseDocument>> {
        
        log.traceIn( "("+collectionDatabase.collectionName()+")", "collection()");

        try {

            if( !collectionDatabase.databaseAccess().allowRead ) {
                throw new Error( "permissionDenied" );
            }

            let result = new Map<string,DatabaseDocument>();

            const documentRecords = await this.documentRecords( collectionDatabase );

            if( documentRecords != null ) {

                await Promise.all( Array.from( documentRecords.entries() ).map( async documentRecordEntry => { 

                    try {
                        const documentPath = documentRecordEntry[0];
                        const rawDatabaseDocument = documentRecordEntry[1];

                        const databaseDocument = 
                            await databaseServiceFactory!.get().databaseFactory.documentFromRecord(
                                documentPath, rawDatabaseDocument) as DatabaseDocument;
    
                        if (databaseDocument == null) {    
                            throw new Error("Unable to read document from data: " + rawDatabaseDocument);
                        } 
                                            
                        result.set(documentPath, databaseDocument);

                    } catch (error) {
                        log.warn("Error reading document snapshot", error);
                    }               
                }));
            }

            log.traceOut( "("+collectionDatabase.collectionName()+")", "collection()", result.size );
            return result; 

        } catch( error ) {

            log.warn( "Error reading collection", error );

            throw new Error( (error as any).message );
        }
    }

    async referenceHandles( collectionDatabase : CollectionDatabase<DatabaseDocument> ): Promise<Map<string,ReferenceHandle<DatabaseDocument>>> {
        
        log.traceIn( "("+collectionDatabase.collectionName()+")", "referenceHandles()");

        try {

            if( !collectionDatabase.databaseAccess().allowRead ) {
                throw new Error( "permissionDenied" );
            }

            let result = new Map<string,ReferenceHandle<DatabaseDocument>>();

            const documentRecords = await this.documentRecords( collectionDatabase );

            if( documentRecords != null ) {

                await Promise.all( Array.from( documentRecords.entries() ).map( async documentRecordEntry => { 

                    try {
                        const documentPath = documentRecordEntry[0];
                        const rawDatabaseDocument = documentRecordEntry[1];

                        const databaseDocument = 
                            await databaseServiceFactory!.get().databaseFactory.documentFromRecord(
                                documentPath, rawDatabaseDocument) as DatabaseDocument;
    
                        if (databaseDocument == null) {    
                            throw new Error("Unable to read document from data: " + rawDatabaseDocument);
                        } 

                        result.set(databaseDocument.path(), databaseDocument.referenceHandle() );

                    } catch (error) {
                        log.warn("Error reading document snapshot", error);
                    }               
                })); 
            }

            log.traceOut( "("+collectionDatabase.collectionName()+")", "referenceHandles()", result.size );
            return result; 

        } catch( error ) {

            log.warn( "Error reading reference handles", error );

            throw new Error( (error as any).message );
        }
    }
 
    async createDocument( collectionDatabase : CollectionDatabase<DatabaseDocument>, databaseDocument : DatabaseDocument ): Promise<DatabaseDocument> {
        log.traceIn( "("+collectionDatabase.collectionName()+")", "createDocument()", databaseDocument.referenceHandle().path);

        try {
            if( !collectionDatabase.databaseAccess().allowCreate) {
                throw new Error( "permissionDenied" );
            }

            if( !databaseDocument.databaseAccess().allowCreate) {
                throw new Error( "permissionDenied" );
            }

            if( collectionDatabase.owner() != null ) {

                const ownerDatabaseDocumentRecord = await this.readDocumentRecord( collectionDatabase.owner()!.uri() );
   
                if( ownerDatabaseDocumentRecord == null ) {
                    throw new Error( "Owner of collection does not exist: " + collectionDatabase.owner()!.path() )
                }
            }

            if( databaseDocument.id.value() != null ) {
            
                const existingDatabaseDocumentRecord = await this.readDocumentRecord( databaseDocument.uri() );
    
                if( existingDatabaseDocumentRecord != null ) {
                    throw new Error( "Document already exists with ID: " + databaseDocument.id.value() )
                }
            }
            else {
                const documentId = await this.newDocumentRecordId( collectionDatabase );

                if( documentId == null ) {
                    throw new Error( "Error getting new document ID");
                }

                databaseDocument.id.setValue( documentId );
            }

            const now = new Date();

            if( databaseDocument.startDate.value() == null ) {

                const trackChanges = databaseDocument.startDate.trackChanges;
                
                databaseDocument.startDate.trackChanges = false;
                
                databaseDocument.startDate.setValue( now );

                databaseDocument.startDate.trackChanges = trackChanges;

            }

            databaseDocument.lastChangedAt.setValue( now );

            const authenticatedDatabaseEntity = databaseServiceFactory!.get().authenticatedDatabaseEntity();

            databaseDocument.lastChangedBy.setValue( authenticatedDatabaseEntity?.referenceHandle() as ReferenceHandle<User> );

            databaseDocument.archived.setValue( false ); 
            
            let documentRecord = await databaseDocument.toRecord();

            //log.debug( "("+collectionDatabase.collectionName()+")", "documentData", documentData );

            await this.createDocumentRecord( databaseDocument.uri(), documentRecord );

            log.traceOut( "("+collectionDatabase.collectionName()+")", "createDocument()", "resolved:", databaseDocument.referenceHandle().path);
            return databaseDocument ;

        } catch( error ) {

            log.warn( "Error creating document", error );

            throw new Error( (error as any).message );
        };
    }

    async readDocument( collectionDatabase : CollectionDatabase<DatabaseDocument>, databaseDocument: DatabaseDocument): Promise<boolean> {
        
        //log.traceIn( "readDocument()", databaseDocument.referenceHandle().title);

        try {
            if( !databaseDocument.databaseAccess().allowRead ) {
                throw new Error( "permissionDenied");
            }

            if( databaseDocument.id.value() == null  ) {
                throw new Error( "documentNotCreated" );
            }

            const documentId = databaseDocument.id.value()!;
            
            const documentRecord = await this.readDocumentRecord( databaseDocument.uri() );

            if( documentRecord == null ) {
                log.warn( "readDocument()", "Document record could not be read", databaseDocument.referenceHandle().path);
                return false;
            }
                
            databaseDocument.fromRecord( documentRecord );

            databaseDocument.id.setValue( documentId );

            //log.traceOut( "readDocument()", "read:", databaseDocument.referenceHandle().title);
            return true;

        } catch( error ) {

            log.warn( "Error reading document", error );

            throw new Error( (error as any).message );
        }
    }

    async updateDocument( collectionDatabase : CollectionDatabase<DatabaseDocument>, 
        databaseDocument: DatabaseDocument,
        force? : boolean ): Promise<boolean> {
        
        log.traceIn( "("+collectionDatabase.collectionName()+")", "updateDocument(", databaseDocument.referenceHandle().path + ")", {force} );

        try {

            const databaseAccess = databaseDocument.databaseAccess();

            // log.debug( "("+collectionDatabase.collectionName()+")", "updateDocument()", {databaseAccess} );

            if( !databaseAccess.allowUpdate ) {
                throw new Error( "permissionDenied" );
            }
            
            if( databaseDocument.id.value() == null || databaseDocument.id.value()!.length === 0 ) {
                throw new Error( "documentNotCreated");
            }

            const existingDatabaseDocumentRecord = await this.readDocumentRecord( databaseDocument.uri() ); 

            if( existingDatabaseDocumentRecord == null ) {
                throw new Error( "No document found at: " + databaseDocument.path() )
            }

            let documentRecord = await databaseDocument.toRecord( force );

            if( JSON.stringify( documentRecord ) === JSON.stringify( existingDatabaseDocumentRecord ) ) {
                //log.debug( "("+collectionDatabase.collectionName()+")", "no change" );
                return false;
            }

            databaseDocument.lastChangedAt.setValue( new Date() );

            const authenticatedDatabaseEntity = databaseServiceFactory!.get().authenticatedDatabaseEntity();

            databaseDocument.lastChangedBy.setValue( authenticatedDatabaseEntity?.referenceHandle() as ReferenceHandle<User> );

            databaseDocument.archived.setValue( false );
            
            documentRecord = await databaseDocument.toRecord( force );

            //log.debug( "("+collectionDatabase.collectionName()+")", {documentRecord} );

            await this.updateDocumentRecord( databaseDocument.uri(), documentRecord );

            log.traceOut( "("+collectionDatabase.collectionName()+")", "updateDocument()", databaseDocument.referenceHandle().path );
            return true;

        } catch( error ) {

            log.warn( "Error updating document", collectionDatabase.collectionName(), error );
            
            throw new Error( (error as any).message );
        }
    }

    async deleteDocument( collectionDatabase : CollectionDatabase<DatabaseDocument>, databaseDocument : DatabaseDocument ): Promise<boolean> {
        
        log.traceIn( "("+collectionDatabase.collectionName()+")", "deleteDocument()", databaseDocument.referenceHandle().path );

        try {

            if( !collectionDatabase.databaseAccess().allowDelete) {
                throw new Error( "permissionDenied" );
            }

            if( !databaseDocument.databaseAccess().allowDelete) {
                throw new Error( "permissionDenied" );
            }

            if( databaseDocument.id.value() == null ) {
                throw new Error( "documentNotCreated"  );
            }

            const result = 
                await this.deleteDocumentRecord( databaseDocument.uri() );

            log.traceOut( "("+collectionDatabase.collectionName()+")", "deleteDocument()", {result} );
            return result;

        } catch( error ) {

            log.warn( "Error deleting document", collectionDatabase.collectionName(), error );

            throw new Error( (error as any).message );
        }
    }


    async archiveDocument( collectionDatabase : CollectionDatabase<DatabaseDocument>, databaseDocument : DatabaseDocument ): Promise<boolean> {
        
        log.traceIn( "("+collectionDatabase.collectionName()+")", "archiveDocument()", databaseDocument.referenceHandle().path );

        try {

            if( !collectionDatabase.databaseAccess().allowDelete) {
                throw new Error( "permissionDenied" );
            }

            if( !databaseDocument.databaseAccess().allowDelete) {
                throw new Error( "permissionDenied" );
            }

            if( databaseDocument.id.value() == null ) {
                throw new Error( "documentNotCreated"  );
            }

            if( !!databaseDocument.archived.value() ) {
                log.warn( "Document is already archived");
                return false;
            }

            const existingDatabaseDocumentRecord = await this.readDocumentRecord( databaseDocument.uri() ); 

            if( existingDatabaseDocumentRecord == null ) {
                throw new Error( "No document found at: " + databaseDocument.path() )
            }

            // First store audit data in document (functions will pick them up)
            databaseDocument.lastChangedAt.setValue( new Date() );

            const authenticatedDatabaseEntity =  databaseServiceFactory!.get().authenticatedDatabaseEntity();

            databaseDocument.lastChangedBy.setValue( authenticatedDatabaseEntity?.referenceHandle() as ReferenceHandle<User> );

            databaseDocument.archived.setValue( true );

            databaseDocument.archivedAt.setValue( new Date() );

            let documentRecord = await databaseDocument.toRecord();

            //log.debug( "("+collectionDatabase.collectionName()+")", "documentData", documentData );

            await this.updateDocumentRecord( databaseDocument.uri(), documentRecord );

            log.traceOut( "("+collectionDatabase.collectionName()+")", "archiveDocument()" ); 
            return true;

        } catch( error ) {

            log.warn( "Error archiving document", collectionDatabase.collectionName(), error );
            
            throw new Error( (error as any).message );
        }
    }


    async documentsWithProperties( 
        database : Database<DatabaseDocument>, 
        properties : Map<string,string> ) : Promise<Map<string,DatabaseDocument>> {

        log.traceIn( "documentsWithProperties()", database.path(), {properties} );

        try {

            const result = new Map<string,DatabaseDocument>();

            const databaseFilters = [] as DatabaseFilter[];

            properties.forEach( (value, property ) => {

                databaseFilters.push( {
                    property: property,
                    comparator: Comparators.Equal as Comparator,
                    value: value
                });

            });
  
            const documentRecords = await this.documentRecords( database, databaseFilters );

            if( documentRecords != null ) {

                await Promise.all( Array.from( documentRecords.entries() ).map( async documentRecordEntry => { 

                    try {
                        const documentPath = documentRecordEntry[0];
                        const rawDatabaseDocument = documentRecordEntry[1];

                        const databaseDocument = 
                            await databaseServiceFactory!.get().databaseFactory.documentFromRecord(
                                documentPath, rawDatabaseDocument) as DatabaseDocument;
    
                        if (databaseDocument == null) {    
                            throw new Error("Unable to read document from data: " + rawDatabaseDocument);
                        } 

                        result.set( documentPath, databaseDocument);
                        
                    } catch (error) {
                        log.warn("Error reading document snapshot", error);
                    }               
                })); 
            }

            log.traceOut( "documentsWithProperties()", result.size );
            return result;


        } catch( error ) {

            log.warn( "documentsWithProperties()", "Error querying database", database.path(), error );
            
            throw new Error( "Error querying database "+ database.path() );
        }
    }

    async expiredArchivedDocuments() : Promise<Map<string,DatabaseDocument>> {

        log.traceIn( "expiredArchivedDocuments()" );

        try {

            const result = new Map<string,DatabaseDocument>();

            const changesDatabase = 
                databaseServiceFactory!.get().databaseFactory.collectionGroupDatabaseFromCollectionName(
                    ChangesCollection
                )!;

            const databaseFilters = [] as DatabaseFilter[];

            databaseFilters.push( {
                    property: EndDatePropertyKey,
                    comparator: Comparators.LessThan as Comparator,
                    value: Date.now()
                });
            
            databaseFilters.push( {
                    property: ChangeTypePropertyKey,
                    comparator: Comparators.Equal as Comparator,
                    value: ChangeTypes.Archived 
                });

            const documentRecords = await this.documentRecords( changesDatabase, databaseFilters );

            if( documentRecords != null ) {

                await Promise.all( Array.from( documentRecords.entries() ).map( async documentRecordEntry => { 

                    try {
                        const documentPath = documentRecordEntry[0];
                        const rawDatabaseDocument = documentRecordEntry[1];

                        const change = await databaseServiceFactory!.get().databaseFactory.documentFromRecord( 
                            documentPath, rawDatabaseDocument ) as Change;
                            
                        if (change == null) {    
                            throw new Error("Unable to read change from data: " + rawDatabaseDocument);
                        } 

                        const archivedDocument = await change.changedDocument.document();

                        if( archivedDocument == null ) {
                            log.warn( "expiredArchivedDocuments()", "Could not read archived document for change: " + documentPath );
                        }
                        else if( !archivedDocument.archived.value() ) {
                            log.warn( "expiredArchivedDocuments()", "Document is no longer archived, avoid later requeries: " + documentPath);

                            change.endDate.setValue( undefined ); // Prevenst subsequent query matches
                            
                            await change.update(); 
                        }
                        else {
                            result.set( archivedDocument.referenceHandle().path, archivedDocument );
                        }
                        
                    } catch (error) {
                        log.warn("Error reading document snapshot", error);
                    }               
                })); 
                
            }

            log.traceOut( "expiredArchivedDocuments()", result.size );
            return result;


        } catch( error ) {

            log.warn( "expiredArchivedDocuments()", "Error querying archived documents", error );
            
            throw new Error( "Error querying archived documents: " + (error as any).message );
        }
    }

    async rewriteDocument( databaseDocument : DatabaseDocument, recursive? : boolean ) : Promise<void> { 

        try {
            log.traceIn("rewriteDocument()", databaseDocument.title.value() );

            await databaseDocument.update( true );

            if( !recursive ) {
                log.traceOut("rewriteDocument()", "Done non recursive" );
                return;
            }

            const collectionPropertiesSelector =
                { includePropertyTypes: [PropertyTypes.Collection] } as PropertiesSelector;

            const subcollectionProperties = databaseDocument.properties(collectionPropertiesSelector);

            for( const subcollectionProperty of subcollectionProperties.values() ) {

                const collectionDatabase =
                    (subcollectionProperty as CollectionProperty<DatabaseDocument>).collection();

                // Changes have admin security access and must be handled with admin rights from back end delete monitoring

                const collectionDocuments = await collectionDatabase.documents();

                for( const collectionDocument of collectionDocuments.values() ) {

                    await this.rewriteDocument( collectionDocument, recursive );
                }
            }

            log.traceOut("rewriteDocument()", "Done recursive" );

        } catch (error) {
            log.warn("Error rewriting document and its collections", error);
        }
    }

    abstract documentRecords( 
        database : Database<DatabaseDocument>, 
        databaseFilters? : DatabaseFilter[] ): Promise<Map<string,DatabaseRecord>>; 

    abstract documentReference( uri : string ) : any | undefined;

    abstract documentUri( documentReference : any ) : string | undefined;

    abstract documentRecord( documentReference : any ): Promise<[string,DatabaseRecord] | undefined>;

    abstract newDocumentRecordId( collectionDatabase : CollectionDatabase<DatabaseDocument> ): Promise<string>; 
    
    abstract createDocumentRecord( uri : string, documentRecord : DatabaseRecord  ): Promise<void>; 

    abstract readDocumentRecord( uri : string ): Promise<DatabaseRecord | undefined>; 

    abstract updateDocumentRecord( uri : string, documentRecord : DatabaseRecord  ): Promise<void>; 

    abstract deleteDocumentRecord( uri : string ): Promise<boolean>; 

    abstract database( database : Database<DatabaseDocument> ): Promise<Map<string,DatabaseDocument>>; 

    abstract monitorDatabase( database : Database<DatabaseDocument> ): Promise<void>; 

    abstract releaseDatabase( database : Database<DatabaseDocument> ): Promise<void>; 

    abstract monitorDocuments( 
        collectionDatabase : CollectionDatabase<DatabaseDocument>, documentPaths : string[] ) : Promise<void>;

    abstract releaseDocuments( 
        collectionDatabase : CollectionDatabase<DatabaseDocument>, documentPaths : string[] ) : Promise<void>;

    abstract releaseAllDocuments( 
        collectionDatabase : CollectionDatabase<DatabaseDocument> ) : Promise<void>;

    readonly converter : DatabaseConverter;

    protected _collectionGroupMonitors : Map<string,any> = new Map<string,any | null>();

    protected _collectionMonitors : Map<string,any> = new Map<string,any | null>();

    protected _documentMonitors : Map<string,Map<string,any>> = new Map<string,Map<string,any | null>>();

    readonly clientEncryption : boolean;

}
