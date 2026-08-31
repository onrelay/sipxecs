import {
    AbstractDatabaseManager,
    BasicDatabaseConverter,
    CollectionDatabase,
    Database,
    DatabaseDocument,
    DatabaseFilter,
    DatabaseRecord,
    databaseServiceFactory,
    log,
    XmlDatabaseDocument,
} from "@dao/database";

export class SipxRestDatabaseManager extends AbstractDatabaseManager {

    constructor( params: {
        baseUrl: string;
        clientEncryption?: boolean;
    } ) {
        super( {
            clientEncryption: params.clientEncryption ?? false,
            converter: new BasicDatabaseConverter(),
        } );

        this._baseUrl = params.baseUrl.replace( /\/$/, "" );
    }

    documentReference( uri: string ): string {
        return uri;
    }

    documentUri( documentReference: unknown ): string | undefined {
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

            const record = await this.readDocumentRecord( uri );
            const result = record == null ? undefined : [uri, record] as [string, DatabaseRecord];

            log.traceOut( "documentRecord()", uri, result != null );
            return result;
        } catch( error ) {
            log.warn( "documentRecord()", "Error retrieving database record", documentReference, error );
            throw new Error( (error as Error).message );
        }
    }

    async newDocumentRecordId( _collectionDatabase: CollectionDatabase<DatabaseDocument> ): Promise<string> {
        log.traceIn( "newDocumentRecordId()" );
        const newId = Date.now().toString();
        log.traceOut( "newDocumentRecordId()", newId );
        return newId;
    }

    async readDocumentRecord( uri: string ): Promise<DatabaseRecord | undefined> {
        log.traceIn( "readDocumentRecord()", uri );

        try {
            const restPath = this.restPathFromUri( uri );
            const documentId = databaseServiceFactory!.get().databaseFactory.documentId( uri );

            if( !restPath || documentId == null || documentId === "new" ) {
                log.traceOut( "readDocumentRecord()", uri, "invalid id or collection" );
                return undefined;
            }

            const url = `${this._baseUrl}/rest/${restPath}/${documentId}`;
            const response = await fetch( url, {
                headers: { "Accept": "application/json, application/xml, text/xml" }
            } );

            if( response.status === 404 ) {
                log.traceOut( "readDocumentRecord()", uri, "404 not found" );
                return undefined;
            }

            if( !response.ok ) {
                throw new Error( `sipX REST read failed: ${response.status} ${response.statusText}` );
            }

            const text = await response.text();
            const record = await this.parseXmlToRecord( uri, text );

            log.traceOut( "readDocumentRecord()", uri, record != null );
            return record;
        } catch( error ) {
            log.warn( "readDocumentRecord()", "Error reading sipX record", uri, error );
            throw new Error( (error as Error).message );
        }
    }

    async documentRecords(
        database: Database<DatabaseDocument>,
        _databaseFilters?: DatabaseFilter[]
    ): Promise<Map<string, DatabaseRecord>> {
        log.traceIn( "documentRecords()", database.uri() );

        try {
            const restPath = this.restPathFromDatabase( database );
            const url = `${this._baseUrl}/rest/${restPath}`;
            const response = await fetch( url, {
                headers: { "Accept": "application/json, application/xml, text/xml" }
            } );

            if( !response.ok ) {
                throw new Error( `sipX REST list records failed: ${response.status} ${response.statusText}` );
            }

            const text = await response.text();
            const result = await this.parseXmlsToRecordMap( database, text );

            log.traceOut( "documentRecords()", database.uri(), result.size );
            return result;
        } catch( error ) {
            log.warn( "documentRecords()", "Error querying sipX records", database.uri(), error );
            throw new Error( (error as Error).message );
        }
    }

    async createDocumentRecord( uri: string, documentRecord: DatabaseRecord ): Promise<void> {
        log.traceIn( "createDocumentRecord()", uri );

        try {
            const restPath = this.restPathFromUri( uri );
            const xml = this.recordToXml( uri, documentRecord );
            const url = `${this._baseUrl}/rest/${restPath}`;

            const response = await fetch( url, {
                method: "PUT",
                headers: { "Content-Type": "application/xml" },
                body: xml
            } );

            if( !response.ok ) {
                throw new Error( `sipX REST create record failed: ${response.status} ${response.statusText}` );
            }

            log.traceOut( "createDocumentRecord()", uri );
        } catch( error ) {
            log.warn( "createDocumentRecord()", "Error creating sipX record", uri, error );
            throw new Error( (error as Error).message );
        }
    }

    async updateDocumentRecord( uri: string, documentRecord: DatabaseRecord ): Promise<void> {
        log.traceIn( "updateDocumentRecord()", uri );

        try {
            const restPath = this.restPathFromUri( uri );
            const documentId = databaseServiceFactory!.get().databaseFactory.documentId( uri );
            const xml = this.recordToXml( uri, documentRecord );

            const url = documentId != null
                ? `${this._baseUrl}/rest/${restPath}/${documentId}`
                : `${this._baseUrl}/rest/${restPath}`;

            const response = await fetch( url, {
                method: "PUT",
                headers: { "Content-Type": "application/xml" },
                body: xml
            } );

            if( !response.ok ) {
                throw new Error( `sipX REST update record failed: ${response.status} ${response.statusText}` );
            }

            log.traceOut( "updateDocumentRecord()", uri );
        } catch( error ) {
            log.warn( "updateDocumentRecord()", "Error updating sipX record", uri, error );
            throw new Error( (error as Error).message );
        }
    }

    async deleteDocumentRecord( uri: string ): Promise<boolean> {
        log.traceIn( "deleteDocumentRecord()", uri );

        try {
            const restPath = this.restPathFromUri( uri );
            const documentId = databaseServiceFactory!.get().databaseFactory.documentId( uri );

            if( !restPath || documentId == null ) {
                throw new Error( "Missing collection or document ID for delete" );
            }

            const url = `${this._baseUrl}/rest/${restPath}/${documentId}`;
            const response = await fetch( url, { method: "DELETE" } );

            const deleted = response.ok;
            log.traceOut( "deleteDocumentRecord()", uri, deleted );
            return deleted;
        } catch( error ) {
            log.warn( "deleteDocumentRecord()", "Error deleting sipX record", uri, error );
            throw new Error( (error as Error).message );
        }
    }

    async database(
        database: Database<DatabaseDocument>
    ): Promise<Map<string, DatabaseDocument>> {
        log.traceIn( "database()", database.uri() );

        try {
            const records = await this.documentRecords( database );
            const result = new Map<string, DatabaseDocument>();

            for( const [uri, record] of records ) {
                const doc = await databaseServiceFactory!.get().databaseFactory.documentFromRecord( uri, record );

                if( doc != null ) {
                    result.set( uri, doc );
                }
            }

            log.traceOut( "database()", database.uri(), result.size );
            return result;
        } catch( error ) {
            log.warn( "database()", "Error hydrating sipX database records", database.uri(), error );
            throw new Error( (error as Error).message );
        }
    }

    async monitorDatabase( _database: Database<DatabaseDocument> ): Promise<void> {
        throw new Error( "Sipx REST database monitoring is not implemented" );
    }

    async releaseDatabase( _database: Database<DatabaseDocument> ): Promise<void> {
        throw new Error( "Sipx REST database monitoring is not implemented" );
    }

    async monitorDocuments(
        _collectionDatabase: CollectionDatabase<DatabaseDocument>,
        _uris: string[]
    ): Promise<void> {
        throw new Error( "Sipx REST database monitoring is not implemented" );
    }

    async releaseDocuments(
        _collectionDatabase: CollectionDatabase<DatabaseDocument>,
        _uris: string[]
    ): Promise<void> {
        throw new Error( "Sipx REST database monitoring is not implemented" );
    }

    async releaseAllDocuments(
        _collectionDatabase: CollectionDatabase<DatabaseDocument>
    ): Promise<void> {
        throw new Error( "Sipx REST database monitoring is not implemented" );
    }

    private async parseXmlToRecord( uri: string, responseText: string ): Promise<DatabaseRecord> {
        log.traceIn( "parseXmlToRecord()", uri );

        try {
            const document = databaseServiceFactory!.get().databaseFactory.newDocumentFromUri( uri );

            if( document == null || typeof (document as any).fromXml !== "function" ) {
                throw new Error( "Document does not implement XmlDatabaseDocument for URI: " + uri );
            }

            const xmlDocument = document as unknown as XmlDatabaseDocument;
            xmlDocument.fromXml( responseText );

            const record = await xmlDocument.toRecord();

            log.traceOut( "parseXmlToRecord()", uri );
            return record;
        } catch( error ) {
            log.warn( "parseXmlToRecord()", "Error parsing XML to record", uri, error );
            throw new Error( (error as Error).message );
        }
    }

    private async parseXmlsToRecordMap( database: Database<DatabaseDocument>, responseText: string ): Promise<Map<string, DatabaseRecord>> {
        log.traceIn( "parseXmlsToRecordMap()", database.uri() );

        try {
            const result = new Map<string, DatabaseRecord>();
            const itemTag = this.restPathFromDatabase( database );

            const itemRegex = new RegExp( `<${itemTag}>[\\s\\S]*?</${itemTag}>`, "g" );
            const itemBlocks = responseText.match( itemRegex ) ?? [];

            for( const itemXml of itemBlocks ) {
                const record = await this.parseXmlToRecord( database.path() + "/new", itemXml );

                if( typeof record.path === "string" ) {
                    result.set( record.path, record );
                }
            }

            log.traceOut( "parseXmlsToRecordMap()", database.uri(), result.size );
            return result;
        } catch( error ) {
            log.warn( "parseXmlsToRecordMap()", "Error parsing sipX XML list", database.uri(), error );
            throw new Error( (error as Error).message );
        }
    }

    private recordToXml( uri: string, record: DatabaseRecord ): string {
        log.traceIn( "recordToXml()", uri );

        try {
            const document = databaseServiceFactory!.get().databaseFactory.newDocumentFromUri( uri );

            if( document == null || typeof (document as any).toXml !== "function" ) {
                throw new Error( "Document does not implement XmlDatabaseDocument for URI: " + uri );
            }

            const xmlDocument = document as unknown as XmlDatabaseDocument;
            xmlDocument.fromRecord( record );

            const xml = xmlDocument.toXml();

            log.traceOut( "recordToXml()", uri );
            return xml;
        } catch( error ) {
            log.warn( "recordToXml()", "Error converting record to XML", uri, error );
            throw new Error( (error as Error).message );
        }
    }

    private restPathFromUri( uri: string ): string {
        const collectionName = databaseServiceFactory!.get().databaseFactory.collectionNameFromUri( uri );
        return this.restPathFromCollectionName( collectionName );
    }

    private restPathFromDatabase( database: Database<DatabaseDocument> ): string {
        return this.restPathFromCollectionName( database.collectionName() );
    }

    private restPathFromCollectionName( collectionName?: string ): string {
        switch( collectionName ) {
            case "users":
                return "user";
            default:
                return collectionName ?? "";
        }
    }

    private readonly _baseUrl: string;
}
