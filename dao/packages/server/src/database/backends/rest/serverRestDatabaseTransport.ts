import { IncomingMessage, ServerResponse } from "node:http";
import { createServer, Server } from "node:https";
import { DatabaseFilter, DatabaseManager, DatabaseRecord, databaseServiceFactory, HttpOperations, log } from "@dao/database";
import { ConfigurationManager } from "@dao/configuration";

export class ServerRestDatabaseTransport {

    constructor( configurationManager: ConfigurationManager, databaseManager: DatabaseManager ) {
        this._configurationManager = configurationManager;
        this._databaseManager = databaseManager;
    }

    async start(): Promise<void> {
        const key = this._configurationManager.config( "https", "key" );
        const cert = this._configurationManager.config( "https", "cert" );
        const port = Number( this._configurationManager.config( "https", "port" ) );

        if( key == null || cert == null || !Number.isInteger( port ) ) {
            throw new Error( "Incomplete HTTPS configuration" );
        }

        this._server = createServer( { key, cert }, this.handleRequest );

        await new Promise<void>( (resolve, reject) => {
            this._server!.once( "error", reject );
            this._server!.listen( port, resolve );
        } );
    }

    async stop(): Promise<void> {
        if( this._server == null ) {
            return;
        }

        await new Promise<void>( (resolve, reject) => {
            this._server!.close( error => error == null ? resolve() : reject( error ) );
        } );

        delete this._server;
    }

    async getOne( uri: string ): Promise<DatabaseRecord | undefined> {
        log.traceIn( "getOne()", uri );

        try {
            const documentId = databaseServiceFactory!.get().databaseFactory.documentId( uri );

            if( documentId === "new" ) {
                const collectionDatabase = databaseServiceFactory!.get().databaseFactory.collectionFromUri( uri );

                if( collectionDatabase != null ) {
                    const newId = await this._databaseManager.newDocumentRecordId( collectionDatabase );
                    const newUri = uri.replace( /\/new(\?|$)/, `/${newId}$1` );
                    const newPath = databaseServiceFactory!.get().databaseFactory.uriToPath( newUri )!;
                    log.traceOut( "getOne()", uri, "new", newPath );
                    return { path: newPath };
                }
            }

            const record = await this._databaseManager.readDocumentRecord( uri );
            log.traceOut( "getOne()", uri, record != null );
            return record;
        } catch( error ) {
            log.warn( "getOne()", "Error retrieving database record", uri, error );
            throw new Error( (error as Error).message );
        }
    }

    async getMany( uri: string ): Promise<Map<string, DatabaseRecord>> {
        log.traceIn( "getMany()", uri );

        try {
            const database = databaseServiceFactory!.get().databaseFactory.databaseFromUri( uri );

            if( database == null ) {
                throw new Error( "Invalid database URI: " + uri );
            }

            const result = await this._databaseManager.documentRecords( database );
            log.traceOut( "getMany()", uri, result.size );
            return result;
        } catch( error ) {
            log.warn( "getMany()", "Error retrieving database records", uri, error );
            throw new Error( (error as Error).message );
        }
    }

    async postQuery(
        uri: string,
        databaseFilters: DatabaseFilter[] = []
    ): Promise<Map<string, DatabaseRecord>> {
        log.traceIn( "postQuery()", uri, databaseFilters );

        try {
            const database = databaseServiceFactory!.get().databaseFactory.databaseFromUri( uri );

            if( database == null ) {
                throw new Error( "Invalid database URI: " + uri );
            }

            const result = await this._databaseManager.documentRecords( database, databaseFilters );
            log.traceOut( "postQuery()", uri, result.size );
            return result;
        } catch( error ) {
            log.warn( "postQuery()", "Error querying database records", uri, databaseFilters, error );
            throw new Error( (error as Error).message );
        }
    }

    async post( uri: string, databaseRecord: DatabaseRecord ): Promise<DatabaseRecord> {
        log.traceIn( "post()", uri );

        try {
            const existing = await this._databaseManager.readDocumentRecord( uri );

            if( existing != null ) {
                await this._databaseManager.updateDocumentRecord( uri, databaseRecord );
            }
            else {
                await this._databaseManager.createDocumentRecord( uri, databaseRecord );
            }

            const result = await this._databaseManager.readDocumentRecord( uri ) ?? databaseRecord;
            log.traceOut( "post()", uri );
            return result;
        } catch( error ) {
            log.warn( "post()", "Error posting database record", uri, error );
            throw new Error( (error as Error).message );
        }
    }

    async delete( uri: string ): Promise<boolean> {
        log.traceIn( "delete()", uri );

        try {
            const result = await this._databaseManager.deleteDocumentRecord( uri );
            log.traceOut( "delete()", uri, result );
            return result;
        } catch( error ) {
            log.warn( "delete()", "Error deleting database record", uri, error );
            throw new Error( (error as Error).message );
        }
    }

    private handleRequest = async ( request: IncomingMessage, response: ServerResponse ): Promise<void> => {
        const uri = request.url;

        if( uri == null ) {
            response.statusCode = 400;
            response.end( "Missing database URI" );
            return;
        }

        log.traceIn( "handleRequest()", request.method, uri );

        try {
            const isDocument = databaseServiceFactory!.get().databaseFactory.isUriDocument( uri );
            let result: DatabaseRecord | DatabaseRecord[] | Map<string, DatabaseRecord> | boolean | undefined;

            if( request.method === HttpOperations.Get ) {
                result = isDocument
                    ? await this.getOne( uri )
                    : await this.getMany( uri );
            }
            else if( request.method === HttpOperations.Post ) {
                const requestBody = await this.readRequestBody( request );

                result = isDocument
                    ? await this.post( uri, requestBody as DatabaseRecord )
                    : await this.postQuery( uri, requestBody as DatabaseFilter[] );
            }
            else if( request.method === HttpOperations.Delete ) {
                result = await this.delete( uri );
            }
            else {
                response.statusCode = 501;
                response.end( "REST method is not implemented" );
                return;
            }

            if( result == null || result === false ) {
                response.statusCode = 404;
                response.end();
                return;
            }

            response.statusCode = 200;
            response.setHeader( "Content-Type", "application/json" );

            if( result instanceof Map ) {
                response.end( JSON.stringify( Object.fromEntries( result ) ) );
            }
            else {
                response.end( JSON.stringify( result ) );
            }

            log.traceOut( "handleRequest()", request.method, uri );
        } catch( error ) {
            log.warn( "handleRequest()", "Error handling REST request", request.method, uri, error );
            response.statusCode = 500;
            response.end( (error as Error).message );
        }
    };

    private readRequestBody( request: IncomingMessage ): Promise<DatabaseRecord | DatabaseFilter[]> {
        return new Promise( (resolve, reject) => {
            let body = "";

            request.setEncoding( "utf8" );
            request.on( "data", chunk => body += chunk );
            request.on( "end", () => {
                try {
                    resolve( body.length === 0 ? {} : JSON.parse( body ) );
                } catch( error ) {
                    reject( new Error( "Invalid JSON request body" ) );
                }
            } );
            request.on( "error", reject );
        } );
    }

    private readonly _configurationManager: ConfigurationManager;

    private readonly _databaseManager: DatabaseManager;

    private _server?: Server;
}
