import {
    AbstractDatabaseManager,
    CollectionDatabase,
    Database,
    DatabaseDocument,
    DatabaseFilter,
    DatabaseRecord,
    databaseServiceFactory,
    log,
} from "@dao/database";
import { ClientRestDatabaseConverter } from "./clientRestDatabaseConverter";
import { ClientRestDatabaseTransport } from "./clientRestDatabaseTransport";

/**
 * This class provides a client-side bridge to a backend REST API,
 * conforming to the AbstractDatabaseManager interface. It mimics the behavior
 * of a direct database connection by translating database
 * operations into HTTP requests.
 */
export class ClientRestDatabaseManager extends AbstractDatabaseManager {

    constructor( params: { 
        baseUrl: string,
        clientEncryption : boolean
        } ) {

        log.traceInOut( "constructor()", params );

        super({
            clientEncryption: false,
            converter: new ClientRestDatabaseConverter(),
        });

        this._transport = new ClientRestDatabaseTransport( params.baseUrl );
    }

    async init() : Promise<void> {

        //log.traceIn( "init()");

        try {
            

            //log.traceOut( "init()");

        } catch( error ) {
            log.warn( "init()", "Error initializing mongo database manager", error );

            throw new Error( (error as any).message );
        }
    }

    documentReference(uri: string): unknown {
        return uri;
    }

    documentUri(documentReference: unknown): string | undefined {
        return typeof documentReference === "string" ? documentReference : undefined;
    }

    async documentRecord( documentReference: unknown ): Promise<[string, DatabaseRecord] | undefined> {
        
        log.traceIn( "documentRecord()", documentReference );

        try {
            const uri = this.documentUri( documentReference );

            if( uri == null ) {
                log.traceOut( "documentRecord()", "invalid reference" );
                return undefined;
            }

            const record = await this._transport.getOne( uri );

            const result = record == null ? undefined : [uri, record] as [string, DatabaseRecord];

            log.traceOut( "documentRecord()", uri, result != null );
            return result;

        } catch( error ) {
            log.warn( "documentRecord()", "Error retrieving database record", documentReference, error );
            throw new Error( (error as Error).message );
        }
    }

    async newDocumentRecordId( collectionDatabase: CollectionDatabase<DatabaseDocument> ): Promise<string> {
        log.traceIn( "newDocumentRecordId()", collectionDatabase.uri() );

        try {
            if( !collectionDatabase.databaseAccess().allowCreate) {
                throw new Error( "permissionDenied" );
            }

            const newDatabaseDocument = collectionDatabase.newDocument();

            const record = await this._transport.getOne( newDatabaseDocument.uri() );

            if( record == null || typeof record.path !== "string" ) {
                throw new Error( "Server did not return a new document record" );
            }

            const newDocumentRecordId = databaseServiceFactory!.get().databaseFactory.documentId(
                record.path );

            if( newDocumentRecordId == null || newDocumentRecordId === "new" ) {
                throw new Error( "Server did not return a real document ID" );
            }

            log.traceOut( "newDocumentRecordId()", newDocumentRecordId );
            return newDocumentRecordId ;

        } catch( error ) {

            log.warn( "Error generating document record ID", error );

            throw new Error( (error as any).message );
        };    
    }

    async createDocumentRecord(
        uri: string,
        documentRecord: DatabaseRecord
    ): Promise<void> {
        log.traceIn( "createDocumentRecord()", uri );

        try {

            await this._transport.post( uri, documentRecord );

            log.traceOut( "createDocumentRecord()", uri );

        } catch( error ) {
            log.warn( "createDocumentRecord()", "Error creating database record", uri, error );
            throw new Error( (error as Error).message );
        }
    }

    async readDocumentRecord(uri: string): Promise<DatabaseRecord | undefined> {
        log.traceIn( "readDocumentRecord()", uri );

        try {
            const result = await this._transport.getOne( uri );
            log.traceOut( "readDocumentRecord()", uri, result != null );
            return result;
        } catch( error ) {
            log.warn( "readDocumentRecord()", "Error reading database record", uri, error );
            throw new Error( (error as Error).message );
        }
    }


    async updateDocumentRecord(
        uri: string,
        documentRecord: DatabaseRecord
    ): Promise<void> {
        log.traceIn( "updateDocumentRecord()", uri );

        try {
            await this._transport.post( uri, documentRecord );
            log.traceOut( "updateDocumentRecord()", uri );
        } catch( error ) {
            log.warn( "updateDocumentRecord()", "Error updating database record", uri, error );
            throw new Error( (error as Error).message );
        }
    }

    async deleteDocumentRecord(uri: string): Promise<boolean> {
        log.traceIn( "deleteDocumentRecord()", uri );

        try {
            const result = await this._transport.delete( uri );
            log.traceOut( "deleteDocumentRecord()", uri, result );
            return result;
        } catch( error ) {
            log.warn( "deleteDocumentRecord()", "Error deleting database record", uri, error );
            throw new Error( (error as Error).message );
        }
    }

    async documentRecords(
        database: Database<DatabaseDocument>,
        databaseFilters?: DatabaseFilter[] ): Promise<Map<string, DatabaseRecord>> {
        
        log.traceIn( "documentRecords()", database.uri() );

        try {
            const result = await this._transport.postQuery( database.uri(), databaseFilters );
            log.traceOut( "documentRecords()", database.uri(), result.size );
            return result;
        } catch( error ) {
            log.warn( "documentRecords()", "Error retrieving database records", database.uri(), error );
            throw new Error( (error as Error).message );
        }
    }

    async database(
        database: Database<DatabaseDocument>
    ): Promise<Map<string, DatabaseDocument>> {
        log.traceIn( "database()", database.uri() );

        try {
            const records = await this._transport.getMany( database.uri() );
            const result = new Map<string, DatabaseDocument>();

            for( const [uri, record] of records ) {
                const document = await this.databaseDocumentFromRecord( uri, record );

                if( document != null ) {
                    result.set( uri, document );
                }
            }

            log.traceOut( "database()", database.uri(), result.size );
            return result;
        } catch( error ) {
            log.warn( "database()", "Error hydrating database records", database.uri(), error );
            throw new Error( (error as Error).message );
        }
    }

    async monitorDatabase(_database: Database<DatabaseDocument>): Promise<void> {
        throw new Error("Client REST database transport is not implemented");
    }

    async releaseDatabase(_database: Database<DatabaseDocument>): Promise<void> {
        throw new Error("Client REST database transport is not implemented");
    }

    async monitorDocuments(
        _collectionDatabase: CollectionDatabase<DatabaseDocument>,
        _documentPaths: string[]
    ): Promise<void> {
        throw new Error("Client REST database transport is not implemented");
    }

    async releaseDocuments(
        _collectionDatabase: CollectionDatabase<DatabaseDocument>,
        _documentPaths: string[]
    ): Promise<void> {
        throw new Error("Client REST database transport is not implemented");
    }

    async releaseAllDocuments(
        _collectionDatabase: CollectionDatabase<DatabaseDocument>
    ): Promise<void> {
        throw new Error("Client REST database transport is not implemented");
    }

    private async databaseDocumentFromRecord(
        uri: string,
        record: DatabaseRecord
    ): Promise<DatabaseDocument | undefined> {
        
        return databaseServiceFactory!.get().databaseFactory.documentFromRecord( uri, record );
    }

    private readonly _transport: ClientRestDatabaseTransport;

}

