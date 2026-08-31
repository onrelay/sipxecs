import { DatabaseRecord } from "../../core/types/databaseRecord";
import { AbstractDatabaseManager } from "../../core/base/abstractDatabaseManager";
import { log } from "../../core/base/abstractDatabaseService";
import { CollectionDatabase } from "../../core/spec/collectionDatabase";
import { CollectionGroupDatabase } from "../../core/spec/collectionGroupDatabase";
import { Database } from "../../core/spec/database";
import { ArchivedPropertyKey, DatabaseDocument } from "../../core/spec/databaseDocument";
import { DatabaseFilter } from "../../core/types/databaseFilter";
import { IdSuffix, OwnerIds, TemplatePathKey } from "../../core/spec/databaseService";
import { PropertiesSelector } from "../../core/types/propertiesSelector";
import { OwnerProperty } from "../../properties/spec/ownerProperty";
import { PropertyType, PropertyTypes } from "../../core/defs/propertyType";
import { FirestoreConverter } from "./firestoreConverter";
import { Comparators } from "../../core/defs/comparator";
import { DatabaseDocumentNameKey } from "../../core/spec/databaseDocument";
import { DatabaseTypes } from "../../core/defs/databaseType";
import { databaseServiceFactory } from "../../core/impl/databaseServiceFactory";


export abstract class AbstractFirestoreDatabaseManager extends AbstractDatabaseManager {

    constructor( params: { 
        clientEncryption : boolean,
        converter : FirestoreConverter,
        firebase : any
        } ) {

        super( {
            clientEncryption: params.clientEncryption,
            converter: params.converter
        });

        //log.traceIn( "constructor()");

        try {

            this._firebase = params.firebase;

            //log.traceOut( "constructor()");

        } catch( error ) {
            log.warn( "constructor()", "Error initializing database manager", error );

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

            const databaseRecords = (await this.databaseRecordQuery( database ).get())?.docs as any[];

            if( databaseRecords != null ) {

                await Promise.all( databaseRecords.map( async databaseRecord => { 

                    try {
                        const documentData = databaseRecord.data();

                        const documentPath = documentData.path;

                        if( documentPath == null ) {
                            log.warn( "Missing path in document: " + documentData );
                        }
                        else {

                            const databaseDocument = 
                                await databaseServiceFactory!.get().databaseFactory.documentFromRecord( 
                                    documentPath, documentData ) as DatabaseDocument;

                            if( databaseDocument != null ) {  

                                result.set( documentPath, databaseDocument! );
                            }
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

    async documentRecords( 
            database : Database<DatabaseDocument>, 
            databaseFilters? : DatabaseFilter[] ): Promise<Map<string,DatabaseRecord>>  {
        
        log.traceIn( "("+database.collectionName()+")", "rawDatabaseDocuments()");

        try {

            if( !database.databaseAccess().allowRead ) {
                throw new Error( "permissionDenied" );
            }

            let result = new Map<string,DatabaseRecord>;

            const documentRecordSnapshots = (await this.databaseRecordQuery( database, databaseFilters ).get())?.docs as any[];

            if( documentRecordSnapshots != null ) {

                await Promise.all( documentRecordSnapshots.map( async documentRecordSnapshot => { 

                    try {
                        const documentRecord = documentRecordSnapshot.data();

                        if (documentRecord == null) {
                            throw new Error("Unable to read raw document data from snapshot: " + documentRecordSnapshot);
                        } 

                        const documentPath = documentRecord.path;

                        if( documentPath == null ) {
                            throw new Error( "No path found in document data: " +  documentRecord );
                        }

                        result.set(documentPath, documentRecord);
                        
                    } catch (error) {
                        log.warn("Error reading document snapshot", error);
                    }               
                }));
            }

            log.traceOut( "("+database.collectionName()+")", "collection()", result.size );
            return result; 

        } catch( error ) {

            log.warn( "Error reading collection", error );

            throw new Error( (error as any).message );
        }
    }

    async documentRecord( documentReference : any ): Promise<[string,DatabaseRecord] | undefined> {

        //log.traceIn( "databaseRecord()", {databaseDocument});

        try {

            const databaseRecord = documentReference.data();

            //log.traceOut( "databaseRecord()", "read:", databaseDocument.referenceHandle().title);
            return databaseRecord;
            
        } catch( error ) {

            log.warn( "databaseRecord()", "Error retrieving raw document data", error );
            
            throw new Error( (error as any).message );
        }
    }

    async newDocumentRecordId( collectionDatabase : CollectionDatabase<DatabaseDocument> ): Promise<string> {
       
        try {
            if( !collectionDatabase.databaseAccess().allowCreate) {
                throw new Error( "permissionDenied" );
            }

            const documentId = await this.collectionReference( collectionDatabase ).doc().id;

            if( documentId == null ) {
                throw new Error( "Error getting new document ID");
            }

            //log.traceOut( "("+collectionDatabase.collectionName()+")", "newDocumentRecordId()", {documentId});
            return documentId ;

        } catch( error ) {

            log.warn( "Error generating database document record ID", error );

            throw new Error( (error as any).message );
        };
    }
    

    async createDocumentRecord( uri : string, documentRecord : DatabaseRecord ): Promise<void> {
        
        log.traceIn( "createDocumentRecord()", uri );

        try {
            await this.documentReference( uri ).set( documentRecord );

            log.traceOut( "createDocumentRecord()", uri);

        } catch( error ) {

            log.warn( "Error creating database document record", error );

            throw new Error( (error as any).message );
        };
    }

    async readDocumentRecord( uri : string ): Promise<DatabaseRecord | undefined> {
        
        //log.traceIn( "readDocumentRecord()", databaseDocumentPath);

        try {

            const documentRecord = await this.documentReference( uri ).get();
           
            if( documentRecord == null ) {
                log.warn( "readDocument()", "Document could not be read at", uri);
                return undefined;
            }

            return documentRecord.data();

        } catch( error ) {

            log.warn( "Error reading document", error );

            throw new Error( (error as any).message );
        }
    }

    async updateDocumentRecord( uri : string, documentRecord : DatabaseRecord ): Promise<void> {
        
        log.traceIn( "updateDocumentRecord()", uri );

        try {
            await this.documentReference( uri ).set( documentRecord );

            log.traceOut( "createDocumentRecord()", uri);

        } catch( error ) {

            log.warn( "Error creating database document record", error );

            throw new Error( (error as any).message );
        };
    }

    async deleteDocumentRecord( uri : string ): Promise<boolean> {
        
        log.traceIn( "updateDocumentRecord()", uri );

        try {
            const result = !!await this.documentReference( uri ).delete();

            log.traceOut( "createDocumentRecord()", uri);
            return result;

        } catch( error ) {

            log.warn( "Error creating database document record", error );

            throw new Error( (error as any).message );
        };
    }

    collectionGroupReference( collectionGroupDatabase : CollectionGroupDatabase<DatabaseDocument> ) : any {

        const rawCollectionGroupReference = 
            this._firebase.database().collectionGroup( collectionGroupDatabase.collectionName());

        return rawCollectionGroupReference;
    }


    collectionReference( collectionDatabase : CollectionDatabase<DatabaseDocument> ) : any {

        const rawCollectionReference = this._firebase.database().collection( collectionDatabase.path() );                

        return rawCollectionReference;
    }

    documentReference( uri : string ) : any {

        //log.traceIn( "documentReference()", {uri});

        const path = databaseServiceFactory!.get().databaseFactory.documentPathFromUri( uri );

        const documentReference = this._firebase.database().doc( path ); 

        //log.traceOut( "rawDocumentReference()", {rawDocumentReference});

        return documentReference; 
    }

    documentUri( documentReference : any ) : string | undefined {

        //log.traceIn( "documentUri()", {documentReference});

        const databaseRecord = documentReference.data();

        //log.traceOut( "documentUri()", {databaseRecord?.path});

        return databaseRecord?.path;
    }

    protected databaseRecordQuery( database : Database<DatabaseDocument>, databaseFilters? : DatabaseFilter[] ) : any {

        //log.traceIn( "databaseRecordQuery()", database.databasePath(), database.owner()?.databasePath() );

        try {
            let databaseRecordQuery;

            if( database.databaseType === DatabaseTypes.CollectionGroup ) {

                databaseRecordQuery = this.collectionGroupReference( database as CollectionGroupDatabase<DatabaseDocument> );
            }
            else if( database.databaseType === DatabaseTypes.Collection )  {
                databaseRecordQuery = this.collectionReference( database as CollectionDatabase<DatabaseDocument> )
            }
            else {
                throw new Error( "Firestore database type must be a collection group or a collection");
            }

            databaseRecordQuery = databaseRecordQuery.where(
                ArchivedPropertyKey,
                "==",
                false );

            if( database.queryDocumentName() != null && database.documentNames().length > 0 ) {
                databaseRecordQuery = databaseRecordQuery.where( 
                        DatabaseDocumentNameKey,
                        "in",
                        database.documentNames() );
            }
            
            if( database.queryTemplatePath() != null && database.queryTemplatePath()!.length > 0 ) {
                databaseRecordQuery = databaseRecordQuery.where( 
                        TemplatePathKey,
                        "==",
                        database.queryTemplatePath() );
            }

            
            const referenceDocument = database.newDocument()!;
            
            //log.debug( "databaseRecordQuery()", "read reference document", referenceDocument.databasePath( true ) );

            
            const ownerId = referenceDocument.ownerId();

            if( ownerId != null && ownerId.length > 0 ) {

                databaseRecordQuery = databaseRecordQuery.where( 
                    OwnerIds,
                    "array-contains",
                    ownerId
                ); 

                //log.debug( "rawDatabaseQuery()", "added", database.collectionName(), OwnerIds, "array-contains", ownerId );

            } 
            
            const ownerProperties = referenceDocument.properties( {
                includePropertyTypes: [PropertyTypes.Owner as PropertyType]
            } as PropertiesSelector ) as Map<string,OwnerProperty<DatabaseDocument>>;

            //log.debug( "databaseRecordQuery()", "read parent properties", organizationProperties.size );

            if( ownerProperties.values() != null ) {

                for( const ownerProperty of ownerProperties.values() ) {

                    const ownerIdKey = ownerProperty.key() + IdSuffix;

                    const ownerId = ownerProperty.id();

                    if( ownerId != null && ownerId.length > 0 ) {

                        databaseRecordQuery = databaseRecordQuery.where( 
                            ownerIdKey,
                            "==",
                            ownerId
                        ); 

                        //log.debug( "rawDatabaseQuery()", "added", ownerIdKey, "==", ownerId );
                    }
                }
            }

            if( databaseFilters != null ) {

                for( const databaseFilter of databaseFilters ) {

                    let firestoreComparator : string;
                    let firestoreValue : string | undefined;

                    switch( databaseFilter.comparator ) {

                        case Comparators.Includes:
                            firestoreComparator = "array-contains";
                            break;

                        case Comparators.IncludesAny:
                            firestoreComparator = "array-contains-any";
                            break;

                        case Comparators.Equal:
                            firestoreComparator = "==";
                            break;

                        case Comparators.NotEqual:
                            firestoreComparator = "!=";
                            break;

                        case Comparators.LessThan:
                            firestoreComparator = "<";
                            break;

                        case Comparators.LessThanOrEqual:
                            firestoreComparator = "<=";
                            break;

                        case Comparators.GreaterThan:
                            firestoreComparator = ">";
                            break;

                        case Comparators.GreaterThanOrEqual:
                            firestoreComparator = ">=";
                            break;
                        
                        case Comparators.In:
                        case Comparators.NotIn:
                        case Comparators.NotIncludes:
                        case Comparators.Exists:
                        case Comparators.NotExists:
                        default:
                            throw new Error( "Database filter comparator not supported by firestore: " + databaseFilter.comparator );
                    }

                    databaseRecordQuery = databaseRecordQuery.where(
                        databaseFilter.property,
                        firestoreComparator,
                        databaseFilter.value );
                }
            }


            //log.traceOut( "databaseRecordQuery()");
            return databaseRecordQuery;

        } catch( error ) {

            log.warn( "databaseQuery()", "Error building database query", error );
            
            throw new Error( (error as any).message );
        }
    }



    private readonly _firebase : any;

 }