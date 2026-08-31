import { DatabaseFilter, DatabaseRecord, HttpOperations, log } from "@dao/database";

export class ClientRestDatabaseTransport {

    constructor( baseUrl: string ) {
        this._baseUrl = baseUrl.replace(/\/$/, "");
    }

    async getOne( uri: string ): Promise<DatabaseRecord | undefined> {
        log.traceIn( "getOne()", uri );

        try {
            const response = await fetch( this.url( uri ) );

            if( response.status === 404 ) {
                log.traceOut( "getOne()", uri, "not found" );
                return undefined;
            }

            if( !response.ok ) {
                throw new Error( `REST GET one failed: ${response.status} ${response.statusText}` );
            }

            const record = await response.json() as DatabaseRecord;
            log.traceOut( "getOne()", uri );
            return record;
        } catch( error ) {
            log.warn( "getOne()", "Error retrieving database record", uri, error );
            throw new Error( (error as Error).message );
        }
    }

    async getMany( uri: string ): Promise<Map<string, DatabaseRecord>> {
        log.traceIn( "getMany()", uri );

        try {
            const response = await fetch( this.url( uri ) );

            if( !response.ok ) {
                throw new Error( `REST GET many failed: ${response.status} ${response.statusText}` );
            }

            const payload = await response.json() as DatabaseRecord[] | Record<string, DatabaseRecord>;
            const records = Array.isArray( payload ) ? payload : Object.values( payload );
            const result = new Map<string, DatabaseRecord>();

            for( const record of records ) {
                const path = record.path;

                if( typeof path !== "string" ) {
                    throw new Error( "Database record is missing path" );
                }

                result.set( path, record );
            }

            log.traceOut( "getMany()", uri, result.size );
            return result;
        } catch( error ) {
            log.warn( "getMany()", "Error retrieving database records", uri, error );
            throw new Error( (error as Error).message );
        }
    }

    async postQuery( uri: string, databaseFilters: DatabaseFilter[] = [] ): Promise<Map<string, DatabaseRecord>> {
        log.traceIn( "postQuery()", uri, databaseFilters );

        try {
            const response = await fetch( this.url( uri ), {
                method: HttpOperations.Post,
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify( databaseFilters )
            } );

            if( !response.ok ) {
                throw new Error( `REST POST query failed: ${response.status} ${response.statusText}` );
            }

            const payload = await response.json() as DatabaseRecord[] | Record<string, DatabaseRecord>;
            const result = new Map<string, DatabaseRecord>();

            if( Array.isArray( payload ) ) {
                for( const record of payload ) {
                    if( typeof record.path !== "string" ) {
                        throw new Error( "Database record is missing path" );
                    }

                    result.set( record.path, record );
                }
            }
            else {
                for( const [recordUri, record] of Object.entries( payload ) ) {
                    result.set( recordUri, record );
                }
            }

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
            const response = await fetch( this.url( uri ), {
                method: HttpOperations.Post,
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify( databaseRecord )
            } );

            if( !response.ok ) {
                throw new Error( `REST POST failed: ${response.status} ${response.statusText}` );
            }

            const result = await response.json() as DatabaseRecord;
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
            const response = await fetch( this.url( uri ), { method: HttpOperations.Delete } );
            const deleted = response.ok;
            log.traceOut( "delete()", uri, deleted );
            return deleted;
        } catch( error ) {
            log.warn( "delete()", "Error deleting database record", uri, error );
            throw new Error( (error as Error).message );
        }
    }

    private url( uri: string ): string {
        return `${this._baseUrl}/${uri.replace(/^\//, "")}`;
    }

    private readonly _baseUrl: string;
}
