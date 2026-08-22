import { AggregationCursor, BSON, ChangeStream, ChangeStreamDocument, Db, MongoClient, ObjectId, WithId } from 'mongodb';


import { AbstractDatabaseManager, 
    ArchivedPropertyKey, 
    CollectionDatabase, 
    CollectionGroupDatabase, 
    Comparators, 
    Database, 
    DatabaseDocument, 
    DatabaseDocumentNameKey, 
    DatabaseFilter, 
    databaseServiceFactory, 
    DatabaseTypes, 
    IdSuffix, 
    log, 
    OwnerIds, 
    OwnerProperty, 
    PropertiesSelector, 
    PropertyType, 
    PropertyTypes, 
    TemplatePathKey} from "@sipxdao/database";
import { MongoConverter } from "./mongoConverter";
import { Monitor, Observation, Observations } from '@sipxdao/common';

export class MongoDatabaseManager extends AbstractDatabaseManager {

    constructor( params: { 
        uri: string,
        databaseName: string,
        clientEncryption : boolean,
        converter : MongoConverter
        } ) {

        super( {
            clientEncryption: params.clientEncryption,
            converter: params.converter
        });

        //log.traceIn( "constructor()");

        try {

            this.uri = params.uri;

            this.databaseName = params.databaseName;

            //log.traceOut( "constructor()");

        } catch( error ) {
            log.warn( "constructor()", "Error initializing mongo database manager", error );

            throw new Error( (error as any).message );
        }
    }

    async init() : Promise<void> {

        //log.traceIn( "init()");

        try {
            
            const client = new MongoClient(this.uri);

            await client.connect(); 

            this._db = client.db(this.databaseName);

            //log.traceOut( "init()");

        } catch( error ) {
            log.warn( "init()", "Error initializing mongo database manager", error );

            throw new Error( (error as any).message );
        }
    }

    documentReference( uri : string ) : any {

        //log.traceInOut( "documentReference()", {uri});

        return uri as any; 
    }

    documentUri( documentReference : any ) : string | undefined {

        //log.traceIn( "documentUri()", {documentReference});

        return documentReference as string;
    }

    async documentRecord( documentReference : any ): Promise<[string,Record<string, any>] | undefined> {

        const documentRecord = await this.readDocumentRecord( documentReference as string );

        return documentRecord != null ? [documentReference as string, documentRecord ] : undefined;
    }

    async newDocumentRecordId( collectionDatabase : CollectionDatabase<DatabaseDocument> ): Promise<string> {
       
        try {
            if( !collectionDatabase.databaseAccess().allowCreate) {
                throw new Error( "permissionDenied" );
            }

            const documentId = new ObjectId().toString();

            //log.traceOut( "("+collectionDatabase.collectionName()+")", "newDocumentRecordId()", {documentId});
            return documentId ;

        } catch( error ) {

            log.warn( "Error generating document record ID", error );

            throw new Error( (error as any).message );
        };
    }
    
    async createDocumentRecord( uri : string, documentRecord : Record<string, any> ): Promise<void> {
        
        log.traceIn( "createDocumentRecord()", uri );

        try {

            const id = databaseServiceFactory!.get().databaseFactory.documentId( uri );

            if( id == null ) {
                throw new Error( "Missing document ID in URI " + uri );
            }

            const collectionName = databaseServiceFactory!.get().databaseFactory.collectionNameFromUri( uri );

            if( collectionName == null ) {
                throw new Error( "Missing collection name in URI " + uri  );
            }

            const bsonDocument = this.includeUri( uri, documentRecord );

            const result = await this._db!.collection( collectionName ).insertOne(bsonDocument);

            if( id !== result?.insertedId?.toString() ) {
                throw new Error( "Failed to create document record with result: " + result );
            }

            this.stripUri( bsonDocument );

            log.traceOut( "createDocumentRecord()");

        } catch( error ) {

            log.warn( "Error creating database document record", error );

            throw new Error( (error as any).message );
        };
    }

    async readDocumentRecord( uri : string ): Promise<Record<string, any> | undefined> {
        
        //log.traceIn( "readDocumentRecord()", databaseDocumentPath);

        try {

            const id = databaseServiceFactory!.get().databaseFactory.documentId( uri );

            if( id == null ) {
                throw new Error( "Missing document ID in URI " + uri );
            }

            const collectionName = databaseServiceFactory!.get().databaseFactory.collectionNameFromUri( uri );

            if( collectionName == null ) {
                throw new Error( "Missing collection name in URI " + uri  );
            }

            const bsonDocument = await this._db!.collection( collectionName ).findOne({ _id: new ObjectId( id ) });

            if( bsonDocument == null ) {
                //log.traceOut( "databaseRecord()", "not found");
                return undefined; 
            }

            const [recordUri, documentRecord] = this.stripUri( bsonDocument );

            if( !databaseServiceFactory!.get().databaseFactory.equalUris( uri, recordUri) ) {
                throw new Error( "Mismatch with record uri: " + recordUri );
            }

            //log.traceOut( "databaseRecord()", recordUri, documentRecord );
            return documentRecord;
            
        } catch( error ) {

            log.warn( "databaseRecord()", "Error retrieving raw document data", error );
            
            throw new Error( (error as any).message );
        }
    }

    async updateDocumentRecord( uri : string, documentRecord : Record<string, any> ): Promise<void> {
        
        log.traceIn( "updateDocumentRecord()", uri );

        try {

            const id = databaseServiceFactory!.get().databaseFactory.documentId( uri );

            if( id == null ) {
                throw new Error( "Missing document ID in URI " + uri );
            }

            const collectionName = databaseServiceFactory!.get().databaseFactory.collectionNameFromUri( uri );

            if( collectionName == null ) {
                throw new Error( "Missing collection name in URI " + uri  );
            }

            const bsonDocument = this.includeUri( uri, documentRecord );

            const result = await this._db!.collection( collectionName ).updateOne(
                { _id: new ObjectId(id) }, 
                bsonDocument
            );

            if( id !== result?.upsertedId?.toString() ) {
                throw new Error( "Failed to update document record with result: " + result );
            }

            this.stripUri( bsonDocument );

            log.traceOut( "updateDocumentRecord()");

        } catch( error ) {

            log.warn( "Error creating database document record", error );

            throw new Error( (error as any).message );
        };
    }

    async deleteDocumentRecord( uri : string ): Promise<boolean> {
        
        log.traceIn( "deleteDocumentRecord()", uri );

            const id = databaseServiceFactory!.get().databaseFactory.documentId( uri );

            if( id == null ) {
                throw new Error( "Missing document ID in URI " + uri );
            }

            const collectionName = databaseServiceFactory!.get().databaseFactory.collectionNameFromUri( uri );

            if( collectionName == null ) {
                throw new Error( "Missing collection name in URI " + uri  );
            }

            const result = await this._db!.collection( collectionName ).deleteOne(
                { _id: new ObjectId(id) } 
            );

            const deleted = result.deletedCount > 0;

            log.traceOut( "deleteDocumentRecord()", {deleted});
            return deleted;
    }


    async documentRecords( 
            database : Database<DatabaseDocument>, 
            databaseFilters? : DatabaseFilter[] ): Promise<Map<string,Record<string, any>>>  {
        
        log.traceIn( "("+database.collectionName()+")", "documentRecords()");

        try {

            if( !database.databaseAccess().allowRead ) {
                throw new Error( "permissionDenied" );
            }

            let result = new Map<string,Record<string, any>>;

            const matches  = this.databaseRecordMatcher( database, databaseFilters );

            const bsonDocuments = 
                await this._db!.collection(database.collectionName()).aggregate(matches).toArray() as BSON.Document[];
            
            if( bsonDocuments != null && bsonDocuments.length > 0 ) {

                await Promise.all( bsonDocuments.map( async bsonDocument => { 

                    try {
                        const [uri, documentRecord] = this.stripUri( bsonDocument );

                        result.set(uri, documentRecord);
                        
                    } catch (error) {
                        log.warn("Error reading document snapshot", error);
                    }               
                }));
            }

            log.traceOut( "("+database.collectionName()+")", "documentRecords()", result.size );
            return result; 

        } catch( error ) {

            log.warn( "Error reading documentRecords", error );

            throw new Error( (error as any).message );
        }
    }

    async database( database : Database<DatabaseDocument> ): Promise<Map<string,DatabaseDocument>> {
        
        log.traceIn( "("+database.collectionName()+")", "database()");

        try {
            if( !database.databaseAccess().allowRead ) {
                throw new Error( "permissionDenied" );
            }

            let result = new Map<string,DatabaseDocument>();

            const documentRecords = await this.documentRecords( database );

            if( documentRecords != null ) {

                await Promise.all( documentRecords.entries().map( async documentRecordEntry => { 

                    try {
                        const uri = documentRecordEntry[0];

                        const documentRecord = documentRecordEntry[1];

                        const databaseDocument = 
                            await databaseServiceFactory!.get().databaseFactory.documentFromRecord( 
                                uri, documentRecord ) as DatabaseDocument;

                        if( databaseDocument != null ) {  

                            result.set( uri, databaseDocument! );
                        }
                        
                    } catch (error) {
                        log.warn("Error reading document snapshot", error);
                    }               
                }));
            }

            log.traceOut( "("+database.collectionName()+")", "database()", result.size );
            return result; 

        } catch( error ) {

            log.warn( "Error reading database", error );

            throw new Error( (error as any).message );
        }
    }

    async monitorDatabase( database : Database<DatabaseDocument> ) : Promise<void> {
        log.traceIn( "("+database.collectionName()+")", database.databaseType, "monitorDatabase()");

        try {
            if( !database.databaseAccess().allowRead ) {
                throw new Error( "No read access to collection " + database.collectionName() );
            }

            let databaseMonitors;

            if( database.databaseType == DatabaseTypes.Collection ) {

                databaseMonitors = this._collectionMonitors;
            }
            else if( database.databaseType == DatabaseTypes.CollectionGroup ) {

                databaseMonitors = this._collectionGroupMonitors;
            }
            else {
                throw new Error( "Database type cannot be monitored " + database.databaseType );
            }

            let databaseMonitor = 
                databaseMonitors.get( database.uri() ) as ChangeStream<BSON.Document, ChangeStreamDocument<BSON.Document>>;;

            if( databaseMonitor !== undefined ) {
                log.traceOut( "("+database.collectionName()+")", "monitorDatabase()", "Already monitoring database" );
                return;
            }

            databaseMonitors.set( database.uri(), null ); // placeholder

            this._db?.command({
                collMod: database.collectionName(),
                changeStreamPreAndPostImages: { enabled: true }
            });

            const matches  = this.databaseRecordMatcher( database );

            databaseMonitor = this._db!.collection(database.collectionName()).watch<BSON.Document>(
                matches,
                { fullDocument: "updateLookup" }
            );

            databaseMonitor.on( "change", async (change) => {

                //log.traceIn("(" + collectionDatabase.collectionName()+ ")", "monitorDatabase()", "change" );

                try {                
                    const databaseObserver = this.observer( database );

                    let hasNotified = false;

                    let uri : string;
                    let databaseDocument : DatabaseDocument;

                    let bsonDocument : BSON.Document;

                    let observation: Observation;

                    if (change.operationType === 'create') {

                        observation = Observations.Create as Observation;

                        bsonDocument = (change as any).fullDocument;
                    }
                    else if (change.operationType === 'update' ||
                             change.operationType === 'replace' ) {

                        observation = Observations.Update as Observation;

                        bsonDocument = (change as any).fullDocument;
                    }
                    else if (change.operationType === 'delete') {

                        observation = Observations.Delete as Observation;

                        bsonDocument = (change as any).fullDocumentBeforeChange;

                    }
                    else {
                        //log.traceOut("(" + collectionDatabase.collectionName()+ ")", "monitorDatabase()", "ignoring change", change.operationType );
                        return;
                    }

                    let databaseRecord : Record<string,any>;

                    [uri, databaseRecord] = this.stripUri( bsonDocument );

                    databaseDocument = await databaseServiceFactory!.get().databaseFactory.documentFromRecord(
                        uri, databaseRecord ) as DatabaseDocument;


                    //log.debug("(" + collectionDatabase.collectionName()+ ")", "monitorDatabase()", "onShapshot", "DB change notification");

                    if( databaseObserver?.onNotify != null ) {

                        databaseObserver.onNotify( this, 
                            observation!,
                            databaseDocument.uri(),
                            databaseDocument );

                            hasNotified = true; 
                    }
                    

                } catch (error) {
                    log.warn("(" + database.collectionName()+ ")", "monitorDatabase()", "onShapshot", "Error reading snapshot", error);
                }
                    
                //log.traceOut("(" + collectionDatabase.collectionName()+ ")", "monitorDatabase()", "onShapshot", changes.length );
            });

            if( databaseMonitors.get(database.uri()) === undefined ) {
                // We have been released while waiting
                databaseMonitor.close();
            }
            else {
                databaseMonitors.set( database.uri(), databaseMonitor! );
            }
            log.traceOut( "("+database.collectionName()+")", "monitorDatabase()");

        } catch( error ) {
            log.warn( "("+database.collectionName()+")", "monitorDatabase()", "Error monitoring collection", error );
            
            throw new Error( (error as any).message );
        }
    }

    async releaseDatabase( database : Database<DatabaseDocument> ) : Promise<void> {
        
        //log.traceIn( "("+database.collectionName()+")", "releaseDatabase()");

        try {

            let databaseMonitors;

            if( database.databaseType == DatabaseTypes.Collection ) {

                databaseMonitors = this._collectionMonitors;
            }
            else if( database.databaseType == DatabaseTypes.CollectionGroup ) {

                databaseMonitors = this._collectionGroupMonitors;
            }
            else {
                throw new Error( "Database type cannot be monitored " + database.databaseType );
            }

            let databaseMonitor = 
                databaseMonitors.get( database.uri() ) as ChangeStream<BSON.Document, ChangeStreamDocument<BSON.Document>>;;

            if( databaseMonitor === undefined ) {
                log.traceOut( "("+database.collectionName()+")", "releaseDatabase()", "Not monitoring database" );
                return;
            }

            databaseMonitor.close();

            databaseMonitors.delete( database.uri() );

            //log.traceOut( "("+database.collectionName()+")", "releaseDatabase()" );

        } catch( error ) {

            log.warn( "Error stopping collection monitoring from firestore for", database.collectionName(), error );
            
            log.traceOut( "("+database.collectionName()+")", "releaseCollection()", error );
        }
    }

    async monitorDocuments( collectionDatabase : CollectionDatabase<DatabaseDocument>, documentPaths : string[] ) : Promise<void> {

        log.traceIn( "("+collectionDatabase.collectionName()+")", "monitorDocuments()", documentPaths );

        try {
            if( documentPaths != null ) {

                for( const documentPath of documentPaths ) {

                    await this.monitorDocument( collectionDatabase, documentPath );
                }
            }

            log.traceOut( "("+collectionDatabase.collectionName()+")", "monitorDocuments()" );
            
        } catch( error ) {

            log.warn( "Error starting document monitoring for", collectionDatabase.collectionName(), error );
            
            throw new Error( (error as any).message );
        }
    }


    async monitorDocument( collectionDatabase : CollectionDatabase<DatabaseDocument>, documentPath : string ) : Promise<void> {

        log.traceIn( "("+collectionDatabase.collectionName()+")", "monitorDocument()", documentPath );

        try {
            const databaseDocument = collectionDatabase.newDocument( documentPath );

            const documentId = databaseDocument.id.value()!;

            if( documentId == null ) {
                throw new Error( "No valid document ID from path: " + documentPath );
            }

            if( !databaseDocument.databaseAccess().allowRead ) {
                throw new Error( "No read access to document");
            }

            const databaseObserver = this.observer( collectionDatabase );

            let collectionDocumentMonitors = this._documentMonitors.get( collectionDatabase.uri() );

            if( collectionDocumentMonitors == null ) {

                collectionDocumentMonitors = new Map<string,() => void>();

                this._documentMonitors.set( collectionDatabase.uri(), collectionDocumentMonitors );
            }

            if( collectionDocumentMonitors.has( documentPath ) ) {

                await databaseDocument.read();

                if( databaseObserver?.onNotify != null ) {
                    await databaseObserver.onNotify( collectionDatabase, 
                        Observations.Create as Observation, 
                        documentPath, 
                        databaseDocument );
                }

                log.traceOut( "("+collectionDatabase.collectionName()+") Already monitoring document: " + documentPath );
                return;
            }

            let hasInitialResult = false;

            collectionDocumentMonitors.set( collectionDatabase.uri(), null ); // placeholder

            this._db?.command({
                collMod: collectionDatabase.collectionName(),
                changeStreamPreAndPostImages: { enabled: true }
            });

            const documentMonitor = this._db!.collection(collectionDatabase.collectionName()).watch<BSON.Document>(
                [{ $match: { "documentKey._id": new ObjectId( documentId ) } }],
                { fullDocument: "updateLookup" }
            );

            documentMonitor.on( "change", async (change) => {

                //log.traceIn("(" + collectionDatabase.collectionName()+ ")", "monitorDatabase()", "change" );

                try {                
                    const databaseObserver = this.observer( collectionDatabase );

                    let hasNotified = false;

                    let uri : string;
                    let databaseDocument : DatabaseDocument;

                    let bsonDocument : BSON.Document;

                    let observation: Observation;

                    if (change.operationType === 'create') {

                        observation = Observations.Create as Observation;

                        bsonDocument = (change as any).fullDocument;
                    }
                    else if (change.operationType === 'update' ||
                             change.operationType === 'replace' ) {

                        if( !hasInitialResult ) {

                            observation = Observations.Create as Observation;
                            hasInitialResult = true;

                        }
                        else {
                            observation = Observations.Update as Observation;
                        }

                        bsonDocument = (change as any).fullDocument;
                    }
                    else if (change.operationType === 'delete') {

                        observation = Observations.Delete as Observation;

                        bsonDocument = (change as any).fullDocumentBeforeChange;

                    }
                    else {
                        //log.traceOut("(" + collectionDatabase.collectionName()+ ")", "monitorDatabase()", "ignoring change", change.operationType );
                        return;
                    }

                    let databaseRecord : Record<string,any>;

                    [uri, databaseRecord] = this.stripUri( bsonDocument );

                    databaseDocument = await databaseServiceFactory!.get().databaseFactory.documentFromRecord(
                        uri, databaseRecord ) as DatabaseDocument;


                    //log.debug("(" + collectionDatabase.collectionName()+ ")", "monitorDatabase()", "onShapshot", "DB change notification");

                    if( databaseDocument == null ) {
                        throw new Error( "Could not read document: " + documentPath );
                    }

                    if( databaseObserver?.onNotify != null ) {

                        databaseObserver.onNotify( this, 
                            observation!,
                            databaseDocument.uri(),
                            databaseDocument );

                            hasNotified = true; 
                    }                    

                } catch (error) {
                    log.warn("(" + collectionDatabase.collectionName()+ ")", "monitorDatabase()", "onShapshot", "Error reading snapshot", error);
                }
                    
                //log.traceOut("(" + collectionDatabase.collectionName()+ ")", "monitorDatabase()", "onShapshot", changes.length );
            }); 

            if( collectionDocumentMonitors.get(collectionDatabase.uri()) === undefined ) {
                // We have been released while waiting
                documentMonitor.close();
            }
            else {
                collectionDocumentMonitors.set( collectionDatabase.uri(), documentMonitor ); 
            }
            
        } catch( error ) {

            log.warn( "Error stopping documents monitoring from firestore for", collectionDatabase.collectionName(), error );
            
            log.traceOut( "("+collectionDatabase.collectionName()+")", "monitorDocument()", undefined );
            return undefined;
        }
    }
    
    async releaseDocuments( collectionDatabase : CollectionDatabase<DatabaseDocument>, uris : string[] ) : Promise<void> {
       
        //log.traceIn( "("+collectionDatabase.collectionName()+")", "releaseDocuments()", documentPaths );

        try {

            const collectionDocumentMonitors = this._documentMonitors.get( collectionDatabase.uri() );

            if( collectionDocumentMonitors != null ) {
                uris.forEach( uri => {

                    const documentMonitor = collectionDocumentMonitors.get( uri );

                    if( documentMonitor != null) {

                        documentMonitor.close(); 
            
                        collectionDocumentMonitors.delete( uri );
                    }    
                });
            }

            //log.traceOut( "("+collectionDatabase.collectionName()+")", "releaseDocuments()" );

        } catch( error ) {

            log.warn( "Error stopping documents monitoring from firestore for", collectionDatabase.collectionName(), error );
            
            log.traceOut( "("+collectionDatabase.collectionName()+")", "releaseDocuments()", error );
        }

    }

    async releaseAllDocuments( collectionDatabase : CollectionDatabase<DatabaseDocument> ) : Promise<void> {
 
        //log.traceIn( "("+collectionDatabase.collectionName()+")", "releaseAllDocuments()");

        try {

            let collectionDocumentMonitors = this._documentMonitors.get( collectionDatabase.uri() );

            if( collectionDocumentMonitors == null ) {
                //log.traceOut( "("+collectionDatabase.collectionName()+")", "releaseAllDocuments()", "No monitors" );
                return;
            }
            collectionDocumentMonitors.forEach( documentMonitor => {

                documentMonitor.close();

            } );

            this._documentMonitors.delete( collectionDatabase.uri() );

            //log.traceOut( "("+collectionDatabase.collectionName()+")", "releaseAllDocuments()" );

        } catch( error ) {

            log.warn( "Error stopping all documents monitoring from firestore for", collectionDatabase.collectionName(), error );
            
            log.traceOut( "("+collectionDatabase.collectionName()+")", "releaseAllDocuments()", error );
        }
    }

        private includeUri( uri : string, documentRecord : Record<string,any> ) : BSON.Document {

        log.traceIn( "includeUri()", uri, documentRecord );

        try {
            const bsonDocument = documentRecord as BSON.Document;

            bsonDocument._id = new ObjectId( databaseServiceFactory!.get().databaseFactory.documentId( uri ) );

            bsonDocument._collection = databaseServiceFactory!.get().databaseFactory.collectionPathFromUri( uri );

            bsonDocument._query = databaseServiceFactory!.get().databaseFactory.decodeUriQuery( uri );

            log.traceOut( "includeUri()", bsonDocument);

            return bsonDocument;

        } catch( error ) {

            log.warn( "Error inserting document record markers", error );

            throw new Error( (error as any).message );
        };
    }

    private stripUri( bsonDocument : BSON.Document ) : [string,Record<string,any>] {
        
        log.traceIn( "stripUri()", bsonDocument );

        try {
            if( bsonDocument._collection == null ) {
                throw new Error( "Document record has no _collection field: " + bsonDocument );
            }

            let uri = bsonDocument._collection; 
            delete bsonDocument._collection; 

            if( bsonDocument._id == null ) {
                throw new Error( "Document record has no _id field: " + bsonDocument );
            }

            uri += "/" + bsonDocument._id;
            delete bsonDocument._id;

            if( bsonDocument._query != null ) {

                uri += databaseServiceFactory!.get().databaseFactory.encodeUriQuery( bsonDocument._query );
                delete bsonDocument._query;
            }

            const documentRecord = bsonDocument as Record<string,any>;

            log.traceOut( "stripUri()", documentRecord );
            return [uri, documentRecord];

        } catch( error ) {

            log.warn( "Error stripping document uri fields", error );

            throw new Error( (error as any).message );
        };
    }

    collectionGroupMatch( collectionGroupDatabase : CollectionGroupDatabase<DatabaseDocument> ) : BSON.Document {

        let collectionGroupMatch : BSON.Document;

        if( collectionGroupDatabase.owner() != null ) {

            const ownerPath = collectionGroupDatabase.owner()!.path() + "/";

            collectionGroupMatch = { $match: {
                        path: { $gte: ownerPath, $lt: ownerPath + "\uffff" } 
                    }};

        }
        else {
            collectionGroupMatch = {};
        }

        return collectionGroupMatch;
    }

    collectionMatch( collectionDatabase : CollectionDatabase<DatabaseDocument> ) : BSON.Document {

        const collectionMatch = { $match: {
                    path: collectionDatabase.path()
                }};

        return collectionMatch;
    }

    protected databaseRecordMatcher( database : Database<DatabaseDocument>, databaseFilters? : DatabaseFilter[] ) : BSON.Document[] {

        //log.traceIn( "databaseRecordMatcher()", database.path(), database.owner()?.path() );

        try {

            const matches: BSON.Document[] = [];

            let databaseRecordQuery : AggregationCursor<WithId<BSON.Document>>;

            if( database.databaseType === DatabaseTypes.CollectionGroup ) {

                matches.push( this.collectionGroupMatch( database as CollectionGroupDatabase<DatabaseDocument> ) );
            }
            else if( database.databaseType === DatabaseTypes.Collection ) {
                matches.push( this.collectionMatch( database as CollectionDatabase<DatabaseDocument> ) );
            }
            else {
                throw new Error( "Database type must be a collection group or a collection");
            }

            matches.push( this.databaseFilterMatch( {
                    property: ArchivedPropertyKey,
                    comparator: Comparators.Equal,
                    value: false
                } as DatabaseFilter ) );

            if( database.queryDocumentName() != null && database.documentNames().length > 0 ) {

                matches.push( this.databaseFilterMatch( {
                    property: DatabaseDocumentNameKey,
                    comparator: Comparators.In,
                    value: database.documentNames()
                } as DatabaseFilter ) );

            }
            
            if( database.queryTemplatePath() != null && database.queryTemplatePath()!.length > 0 ) {

                matches.push( this.databaseFilterMatch( {
                    property: TemplatePathKey,
                    comparator: Comparators.Equal,
                    value: database.queryTemplatePath() 
                } as DatabaseFilter ) );
            }

            
            const referenceDocument = database.newDocument()!;
            
            //log.debug( "databaseRecordMatcher()", "read reference document", referenceDocument.uri() );

            const ownerId = referenceDocument.ownerId();

            if( ownerId != null && ownerId.length > 0 ) {

                matches.push( this.databaseFilterMatch( {
                    property: OwnerIds,
                    comparator: Comparators.Includes,
                    value: ownerId
                } as DatabaseFilter ) );

                //log.debug( "databaseRecordMatcher()", "added", database.collectionName(), OwnerIds, "array-contains", ownerId );

            } 
            
            const ownerProperties = referenceDocument.properties( {
                includePropertyTypes: [PropertyTypes.Owner as PropertyType]
            } as PropertiesSelector ) as Map<string,OwnerProperty<DatabaseDocument>>;

            //log.debug( "databaseRecordMatcher()", "read parent properties", organizationProperties.size );

            if( ownerProperties.values() != null ) {

                for( const ownerProperty of ownerProperties.values() ) {

                    const ownerIdKey = ownerProperty.key() + IdSuffix;

                    const ownerId = ownerProperty.id();

                    if( ownerId != null && ownerId.length > 0 ) {

                        matches.push( this.databaseFilterMatch( {
                            property: ownerIdKey,
                            comparator: Comparators.Equal,
                            value: ownerId
                        } as DatabaseFilter ) );

                        //log.debug( "databaseRecordMatcher()", "added", ownerIdKey, "==", ownerId );
                    }
                }
            }

            if( databaseFilters != null ) {

                for( const databaseFilter of databaseFilters ) {

                    matches.push( this.databaseFilterMatch( databaseFilter ) );
                }
            }

            //log.traceOut( "databaseRecordMatcher()");
            return matches;

        } catch( error ) {

            log.warn( "databaseRecordMatcher()", "Error building database query", error );
            
            throw new Error( (error as any).message );
        }
    }

    protected databaseFilterMatch( databaseFilter : DatabaseFilter ): BSON.Document {

        let match : BSON.Document;

        switch (databaseFilter.comparator) {
            case Comparators.Equal:
                match = { $match: { [databaseFilter.property]: databaseFilter.value } };
                break;

            case Comparators.NotEqual:
                match = { $match: { [databaseFilter.property]: { $ne: databaseFilter.value } } };
                break;

            case Comparators.LessThan:
                match = { $match: { [databaseFilter.property]: { $lt: databaseFilter.value } } };
                break;

            case Comparators.LessThanOrEqual:
                match = { $match: { [databaseFilter.property]: { $lte: databaseFilter.value } } };
                break;

            case Comparators.GreaterThan:
                match = { $match: { [databaseFilter.property]: { $gt: databaseFilter.value } } };
                break;
                
            case Comparators.GreaterThanOrEqual:
                match = { $match: { [databaseFilter.property]: { $gte: databaseFilter.value } } };
                break;

            case Comparators.Includes:
                match = { $match: { [databaseFilter.property]: databaseFilter.value } };
                break;

            case Comparators.IncludesAny:
                match = { $match: { [databaseFilter.property]: { $in: databaseFilter.value } } };
                break;

            case Comparators.NotIncludes:
                match = { $match: { [databaseFilter.property]: { $ne: databaseFilter.value } } };
                break;

            case Comparators.In:
                match = { $match: { [databaseFilter.property]: { $in: databaseFilter.value } } };
                break;

            case Comparators.NotIn:
                match = { $match: { [databaseFilter.property]: { $nin: databaseFilter.value } } };
                break;

            case Comparators.Exists:
                match = { $match: { [databaseFilter.property]: { $exists: true } } };
                break;

            case Comparators.NotExists:
                match = { $match: { [databaseFilter.property]: { $exists: true } } };

                break;
                                
            default:
                throw new Error("Comparator not supported: " + databaseFilter.comparator);
        }

        return match;
    }

    readonly uri : string;

    readonly databaseName : string;

    private _db : Db | undefined;

 }