import { ConfigurationManager } from "@sipxdao/configuration";
import { AbstractDatabaseManager } from "../../api/dao/abstractDatabaseManager";
import { log } from "../../api/dao/abstractDatabaseService";
import { CollectionDatabase } from "../../api/dao/collectionDatabase";
import { DatabaseDocument } from "../../api/dao/databaseDocument";
import { DatabaseAccess } from "../../api/types/databaseAccess";
import { databaseServiceFactory, DatabaseServiceFactory } from "../../api/dao/databaseServiceFactory";
import { Observation, Observations } from "@sipxdao/common";
import { Database } from "../../api/dao/database";
import { CollectionGroupDatabaseImpl } from "./collectionGroupDatabaseImpl";
import { CollectionDatabaseImpl } from "./collectionDatabaseImpl";
import { CollectionGroupDatabase } from "../../api/dao/collectionGroupDatabase";
import { GenericDatabaseDocument } from "../../api/dao/genericDatabaseDocument";
import { ConfigurationDatabaseManager } from "../../api/dao/configurationDatabaseManager";
import { BasicDatabaseConverter } from "../../api/dao/configurationConverter";
import { DatabaseFilter } from "../../api/dao/databaseFilter";
import { DocumentsDatabaseImpl } from "./documentsDatabaseImpl";
import { Comparators } from "../../api/types/comparator";
import { DatabaseTypes } from "../../api/types/databaseType";

export class ConfigurationDatabaseManagerImpl extends AbstractDatabaseManager implements ConfigurationDatabaseManager {

    constructor( configurationManager : ConfigurationManager ) {

        super( { 
            clientEncryption: false,
            converter: new BasicDatabaseConverter() 
        } );

        //log.traceIn( "constructor()");

        try {


            //log.traceOut( "constructor()" );
            
        } catch( error ) {

            log.warn( "Error creating configuration manager", error );
            
            throw new Error( (error as any).message );
        }
    }

    async init() : Promise<void> {

        //log.traceIn( "init()");

        try {

            if( this._initialized ) {
                return;
            }

            this._initialized = true;

            //log.traceOut( "constructor()" );
            
        } catch( error ) {

            log.warn( "Error initializing configuration manager", error );
            
            throw new Error( (error as any).message );
        }
    }


    async monitorDatabase( database : Database<DatabaseDocument> ) : Promise<void> {

        if( database.databaseType === DatabaseTypes.CollectionGroup ) {
            await this.monitorCollectionGroup( database as CollectionGroupDatabase<DatabaseDocument> );
            return;
        }

        const collectionDatabase = database as CollectionDatabase<DatabaseDocument>;
        const databaseDocuments = await this.collection( collectionDatabase );

        for( const databaseDocument of databaseDocuments.values() ) {

            await (collectionDatabase as CollectionDatabaseImpl<DatabaseDocument>).onNotify(
                collectionDatabase, 
                Observations.Create as Observation, 
                databaseDocument.uri(), 
                databaseDocument);

        }
    }

    async releaseDatabase( database : Database<DatabaseDocument> ) : Promise<void> {  
    }

    async monitorDocuments( collectionDatabase : CollectionDatabase<DatabaseDocument>, documentPaths : string[] ) : Promise<void> {

        for( const documentPath of documentPaths ) {

            const databaseDocument = await this.configDocument( documentPath );

            if( databaseDocument != null ) {
                await (collectionDatabase as CollectionDatabaseImpl<DatabaseDocument>).onNotify(collectionDatabase, 
                    Observations.Create as Observation, 
                    databaseDocument.uri(), 
                    databaseDocument);
            }
        }
    }

    async releaseDocuments( collectionDatabase : CollectionDatabase<DatabaseDocument>, documentPaths : string[] ) : Promise<void> {
    }

    async releaseAllDocuments( collectionDatabase : CollectionDatabase<DatabaseDocument> ) : Promise<void> {
    }    

    async monitorCollectionGroup( collectionGroupDatabase : CollectionGroupDatabase<DatabaseDocument> ) : Promise<void> {

        log.traceIn( "("+collectionGroupDatabase.collectionName()+")", " monitorCollectionGroup()");

        const databaseDocuments = await this.collectionGroup( collectionGroupDatabase );

        for( const databaseDocument of databaseDocuments.values() ) {

            await (collectionGroupDatabase as CollectionGroupDatabaseImpl<DatabaseDocument>).onNotify(collectionGroupDatabase, 
                Observations.Create as Observation, 
                databaseDocument.uri(), 
                databaseDocument);

        }
    }

    async releaseCollectionGroup( collectionGroupDatabase : CollectionGroupDatabase<DatabaseDocument> ) : Promise<void> {
    }

    async readDocumentRecord(uri: string): Promise<Record<string, any> | undefined> {
    
        const configDocument = await this.configDocument( uri );

        return configDocument?.toRecord();
    }

    documentUri( documentReference : any ) : string | undefined {
        return typeof documentReference === "string" ? documentReference : undefined;
    }

    async documentRecord( documentReference : any ): Promise<[string,Record<string, any>] | undefined> {
        const documentUri = this.documentUri( documentReference );

        if( documentUri == null ) {
            return undefined;
        }

        const documentRecord = await this.readDocumentRecord( documentUri );
        return documentRecord == null ? undefined : [documentUri, documentRecord];
    }

    async database( database : Database<DatabaseDocument> ): Promise<Map<string,DatabaseDocument>> {
        if( database.databaseType === DatabaseTypes.CollectionGroup ) {
            return this.collectionGroup( database as CollectionGroupDatabase<DatabaseDocument> );
        }

        if( database.databaseType === DatabaseTypes.Collection ) {
            return this.collection( database as CollectionDatabase<DatabaseDocument> );
        }

        throw new Error( "Unrecognized database type" );
    }

    documentReference( uri : string ) : any {

        return uri;
    }

    async documentRecords( 
            database : Database<DatabaseDocument>, 
            databaseFilters? : DatabaseFilter[] ): Promise<Map<string,Record<string, any>>>  {
        
        log.traceIn( "("+database.collectionName()+")", "rawDatabaseDocuments()");

        try {

            if( !database.databaseAccess().allowRead ) {
                throw new Error( "permissionDenied" );
            }

            let databaseMap : Map<string,DatabaseDocument> | undefined;

            if( database.databaseType === DatabaseTypes.Collection ) {

                databaseMap = this._collectionDocuments.get( database.path() );
            }
            else if( database.databaseType === DatabaseTypes.CollectionGroup ) {

                databaseMap = this._collectionGroupDocuments.get( database.collectionName());
            }
            else if ( database.databaseType === DatabaseTypes.Documents ) {

                databaseMap = await (database as DocumentsDatabaseImpl<DatabaseDocument>).documents();
            }
            else {
                throw new Error( "Unrecognized database type");
            }

            let result = new Map<string,Record<string, any>>;

            if( databaseMap == null ) {

                log.traceOut( "("+database.collectionName()+")", "documentRecords()", "not found" );
                return result;
            }

            for( const databaseDocument of databaseMap.values() ) {

                if( databaseFilters != null ) {

                    for( const databaseFilter of databaseFilters.values() ) {

                        const filterProperty = databaseDocument.property( databaseFilter.property );

                        if( filterProperty == null ) {
                            continue;
                        }

                        switch( databaseFilter.comparator ) {
    
                            case Comparators.Includes:
                                if( !filterProperty.includesValue( databaseFilter.value, false ) ) {
                                    continue;
                                }
                                break;
    
                            case Comparators.IncludesAny:
                                if( !filterProperty.includesValue( databaseFilter.value, true ) ) {
                                    continue;
                                }
                                break;
    
                            case Comparators.Equal:
                                if( filterProperty.compareValue( databaseFilter.value ) !== 0 ) {
                                    continue;
                                }
                                break;
    
                            case Comparators.NotEqual:
                                if( filterProperty.compareValue( databaseFilter.value ) === 0 ) {
                                    continue;
                                }
                                break;
    
                            case Comparators.LessThan:
                                if( filterProperty.compareValue( databaseFilter.value ) >= 0 ) {
                                    continue;
                                }
                                break;
    
                            case Comparators.LessThanOrEqual:
                                if( filterProperty.compareValue( databaseFilter.value ) > 0 ) {
                                    continue;
                                }
                                break;
    
                            case Comparators.GreaterThan:
                                if( filterProperty.compareValue( databaseFilter.value ) <= 0 ) {
                                    continue;
                                }
                                break;
    
                            case Comparators.GreaterThanOrEqual:
                                if( filterProperty.compareValue( databaseFilter.value ) < 0 ) {
                                    continue;
                                }
                                break;
                            
                            case Comparators.NotIncludes:
                                if( filterProperty.includesValue( databaseFilter.value, false ) ) {
                                    continue;
                                }
                                break;

                            case Comparators.Exists:
                                if( filterProperty.value() == null ) {
                                    continue;
                                }
                                break;

                            case Comparators.NotExists:
                                if( filterProperty.value() != null ) {
                                    continue;
                                }
                                break;

                            default:
                                throw new Error( "Database filter comparator not supported by config: " + databaseFilter.comparator );
                        }

                        // Document was not filtered

                        const documentRecord = await databaseDocument.toRecord();

                        result.set( databaseDocument.path(), documentRecord );
                    }
                }
            }

            log.traceOut( "("+database.collectionName()+")", "documentRecords()", result.size );
            return result; 

        } catch( error ) {

            log.warn( "Error reading collection", error );

            throw new Error( (error as any).message );
        }
    }

    async newDocumentRecordId(collectionDatabase: CollectionDatabase<DatabaseDocument>): Promise<string> {
        throw new Error("Config documents are read only");
    }

    async createDocumentRecord(uri: string, documentRecord: Record<string, any>): Promise<void> {
        throw new Error("Config documents are read only");
    }

    async updateDocumentRecord(uri: string, documentRecord: Record<string, any>): Promise<void> {
        throw new Error("Config documents are read only");
    }

    async deleteDocumentRecord(uri: string): Promise<boolean> {
        throw new Error("Config documents are read only");
    }

    async loadConfigDocument( data : any ) : Promise<DatabaseDocument> {
        log.traceIn( "loadConfigDocument()", {data} );

        try {

            let documentData;

            if (typeof data === 'object' ) {

                documentData = Object.assign( {}, data );
            } 
            else if (typeof data === 'string') {
    
                try {
                    documentData = JSON.parse( data );
                } catch( error ) {
                    documentData = Object.assign( {}, data );
                }
            }
            else {
                throw new Error( "Unrecognized data format")
            }

            if( documentData.path == null ) {
                throw new Error( "Document path missing");
            }

            const databaseDocument = await databaseServiceFactory!.get().databaseFactory.documentFromRecord( 
                documentData.path, documentData ) as DatabaseDocument;

            if( databaseDocument == null ) {
                throw new Error( "Could not read document from data: " + documentData );
            }

            await (databaseDocument as GenericDatabaseDocument).onRead();

            databaseDocument.setDatabaseAccess( DatabaseAccess.allowReadOnly() );

            const collectionDatabasePath = databaseDocument.collectionDatabase.path();

            let collectionMap = this._collectionDocuments.get( collectionDatabasePath );

            if( collectionMap == null ) {

                collectionMap = new Map<string,DatabaseDocument>();

                this._collectionDocuments.set( collectionDatabasePath, collectionMap );
            }

            collectionMap.set( databaseDocument.path(), databaseDocument ); 

            let collectionGroupMap = this._collectionGroupDocuments.get( databaseDocument.collectionDatabase.collectionName());

            if( collectionGroupMap == null ) {

                collectionGroupMap = new Map<string,DatabaseDocument>();

                this._collectionGroupDocuments.set( databaseDocument.collectionDatabase.collectionName(), collectionGroupMap );
            }

            collectionGroupMap.set( databaseDocument.path(), databaseDocument ); 

            log.traceOut("loadConfigDocument()", databaseDocument.path() );
            return databaseDocument;
            
        } catch( error ) {

            log.warn( "Error loading document", error );
            
            throw new Error( (error as any).message );
        }
    }

    async configDocument( databaseDocumentPath: string) : Promise<DatabaseDocument | undefined> {

        const collectionDatabasePath =
            databaseServiceFactory!.get().databaseFactory.collectionPathFromUri( databaseDocumentPath );

        if( collectionDatabasePath == null ) {
            return undefined;
        }

        let collectionMap = this._collectionDocuments.get( collectionDatabasePath );

        if( collectionMap == null ) {
            return undefined;
        }

        const configDocument = collectionMap.get( databaseDocumentPath );

        if( configDocument == null ) {
            return undefined;
        }

        return configDocument;
    }

    private readonly _collectionDocuments = new Map<string,Map<string,DatabaseDocument>>();

    private readonly _collectionGroupDocuments = new Map<string,Map<string,DatabaseDocument>>();

    private _initialized : boolean = false; 


}