import { Observation, Observations } from "@sipxdao/common";
import { AbstractDatabaseManager, CollectionDatabase, Database, DatabaseDocument, databaseServiceFactory, DatabaseTypes, log, DatabaseFilter, BasicDatabaseConverter } from "@sipxdao/database";

/**
 * This class provides a client-side bridge to a backend REST API,
 * conforming to the AbstractDatabaseManager interface. It mimics the behavior
 * of a direct database connection by translating database
 * operations into HTTP requests.
 */
export class ClientApiDatabaseManager extends AbstractDatabaseManager {

    constructor() {
        super({
            clientEncryption: false, // Encryption is a server-side concern for this bridge
            converter: new BasicDatabaseConverter()
        });
        log.info("ClientApiDatabaseManager", "constructor() - Using REST API Bridge for database access.");
    }

    private buildUrl(uri: string, action?: string): string {
        const encodedUri = btoa(uri);
        let url = `/api/db?uri=${encodeURIComponent(encodedUri)}`;
        if (action) {
            url += `&action=${action}`;
        }
        return url;
    }

    async monitorDatabase(database: Database<DatabaseDocument>): Promise<void> {
        log.traceIn(`(${database.collectionName()})`, database.databaseType, "monitorDatabase()");

        if (!database.databaseAccess().allowRead) {
            throw new Error(`No read access to collection ${database.collectionName()}`);
        }

        const monitorMap = database.databaseType === DatabaseTypes.Collection
            ? this._collectionMonitors
            : this._collectionGroupMonitors;

        if (monitorMap.has(database.uri())) {
            log.traceOut(`(${database.collectionName()})`, "monitorDatabase()", "Already monitoring database");
            return;
        }

        // Placeholder to prevent re-entry.
        monitorMap.set(database.uri(), null);

        // TODO: Replace with a real-time subscription mechanism (e.g., WebSockets or SSE).
        // This implementation does a one-time fetch, which does not provide real-time updates.
        try {
            const abortController = new AbortController();
            const response = await fetch(this.buildUrl(database.uri()), { signal: abortController.signal });
            
            if (monitorMap.get(database.uri()) === undefined) {
                // We have been released while waiting for fetch
                abortController.abort();
                return;
            }

            if (!response.ok) {
                throw new Error(`Failed to fetch data for ${database.collectionName()}: ${response.statusText}`);
            }

            const records: any[] = await response.json();
            const result = new Map<string, DatabaseDocument>();
            const databaseObserver = this.observer(database);

            for (const record of records) {
                // Assuming the API returns documents with an 'id' field.
                const documentPath = `${database.path()}/${record.id}`;
                const databaseDocument = await databaseServiceFactory!.get().databaseFactory.documentFromRecord(
                    documentPath,
                    record
                ) as DatabaseDocument;

                if (databaseDocument) {
                    result.set(databaseDocument.uri(), databaseDocument);
                } else {
                    log.warn(`(${database.collectionName()})`, "monitorDatabase()", "could not read document from record", { record });
                }
            }

            if (databaseObserver?.onNotify) {
                databaseObserver.onNotify(
                    this,
                    Observations.Create as Observation,
                    database.uri(),
                    result
                );
            }

            // In a real WebSocket/SSE implementation, you would store the connection
            // object here to be closed in `releaseDatabase`.
            monitorMap.set(database.uri(), abortController);

            log.traceOut(`(${database.collectionName()})`, "monitorDatabase()");

        } catch (error) {
            if ((error as Error).name !== 'AbortError') {
                log.warn(`(${database.collectionName()})`, "monitorDatabase()", "Error monitoring collection", error);
                monitorMap.delete(database.uri()); // Clean up on error.
                throw error;
            }
        }
    }

    async releaseDatabase(database: Database<DatabaseDocument>): Promise<void> {
        log.traceIn(`(${database.collectionName()})`, "releaseDatabase()");

        const monitorMap = database.databaseType === DatabaseTypes.Collection
            ? this._collectionMonitors
            : this._collectionGroupMonitors;

        const monitor = monitorMap.get(database.uri());

        if (monitor) {
            // If using fetch with AbortController
            if (monitor instanceof AbortController) {
                monitor.abort();
            }
            // If using WebSockets, it would be monitor.close()
        }
        
        monitorMap.delete(database.uri());

        log.traceOut(`(${database.collectionName()})`, "releaseDatabase()");
    }

    async monitorDocuments(collectionDatabase: CollectionDatabase<DatabaseDocument>, documentPaths: string[]): Promise<void> {
        log.traceIn(`(${collectionDatabase.collectionName()})`, "monitorDocuments()", { count: documentPaths.length });
        await Promise.all(documentPaths.map(docPath => this.monitorDocument(collectionDatabase, docPath)));
        log.traceOut(`(${collectionDatabase.collectionName()})`, "monitorDocuments()");
    }

    async monitorDocument(collectionDatabase: CollectionDatabase<DatabaseDocument>, documentPath: string): Promise<void> {
        log.traceIn(`(${collectionDatabase.collectionName()})`, "monitorDocument()", documentPath);

        const databaseDocument = collectionDatabase.newDocument(documentPath);
        if (!databaseDocument.databaseAccess().allowRead) {
            throw new Error(`No read access to document ${documentPath}`);
        }

        let collectionMonitors = this._documentMonitors.get(collectionDatabase.uri());
        if (!collectionMonitors) {
            collectionMonitors = new Map();
            this._documentMonitors.set(collectionDatabase.uri(), collectionMonitors);
        }

        if (collectionMonitors.has(documentPath)) {
            log.traceOut(`(${collectionDatabase.collectionName()})`, "Already monitoring document", documentPath);
            return;
        }

        // Placeholder to prevent re-entry.
        collectionMonitors.set(documentPath, null);

        // TODO: Replace with a real-time subscription (WebSocket/SSE).
        try {
            const abortController = new AbortController();
            const response = await fetch(this.buildUrl(documentPath), { signal: abortController.signal });

            if (collectionMonitors.get(documentPath) === undefined) {
                // released while waiting
                abortController.abort();
                return;
            }

            if (!response.ok) {
                if (response.status === 404) {
                    await collectionDatabase.notify(Observations.Delete as Observation, documentPath, undefined);
                    return;
                }
                throw new Error(`Failed to fetch document ${documentPath}: ${response.statusText}`);
            }

            const record = await response.json();
            const doc = await databaseServiceFactory!.get().databaseFactory.documentFromRecord(
                documentPath,
                record
            ) as DatabaseDocument;

            if (!doc) {
                throw new Error(`Could not parse document data: ${documentPath}`);
            }

            await collectionDatabase.notify(Observations.Create as Observation, documentPath, doc);

            collectionMonitors.set(documentPath, abortController);
            log.traceOut(`(${collectionDatabase.collectionName()})`, "monitorDocument()", documentPath);

        } catch (error) {
            if ((error as Error).name !== 'AbortError') {
                log.warn(`(${collectionDatabase.collectionName()})`, "monitorDocument()", "Error monitoring document", error);
                collectionMonitors.delete(documentPath);
            }
            throw error;
        }
    }
    
    async releaseDocuments(collectionDatabase: CollectionDatabase<DatabaseDocument>, documentPaths: string[]): Promise<void> {
        log.traceIn(`(${collectionDatabase.collectionName()})`, "releaseDocuments()", { count: documentPaths.length });
        const collectionMonitors = this._documentMonitors.get(collectionDatabase.uri());
        if (collectionMonitors) {
            for (const docPath of documentPaths) {
                const monitor = collectionMonitors.get(docPath);
                if (monitor instanceof AbortController) {
                    monitor.abort();
                }
                collectionMonitors.delete(docPath);
            }
        }
        log.traceOut(`(${collectionDatabase.collectionName()})`, "releaseDocuments()");
    }

    async releaseAllDocuments(collectionDatabase: CollectionDatabase<DatabaseDocument>): Promise<void> {
        log.traceIn(`(${collectionDatabase.collectionName()})`, "releaseAllDocuments()");
        const collectionMonitors = this._documentMonitors.get(collectionDatabase.uri());
        if (collectionMonitors) {
            collectionMonitors.forEach(monitor => {
                if (monitor instanceof AbortController) {
                    monitor.abort();
                }
            });
            this._documentMonitors.delete(collectionDatabase.uri());
        }
        log.traceOut(`(${collectionDatabase.collectionName()})`, "releaseAllDocuments()");
    }

    async documentRecords(database: Database<DatabaseDocument>, databaseFilters?: DatabaseFilter[]): Promise<Map<string, Record<string, any>>> {
        log.traceIn(`(${database.collectionName()})`, "documentRecords()");
        try {
            const response = await fetch(this.buildUrl(database.uri(), 'records'), {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ filters: databaseFilters })
            });
            if (!response.ok) throw new Error(response.statusText);
            const data = await response.json();
            const result = new Map<string, Record<string, any>>();
            Object.entries(data).forEach(([path, record]) => result.set(path, record as Record<string, any>));
            return result;
        } catch (error) {
            log.warn("Error fetching document records", error);
            throw error;
        }
    }

    documentReference(uri: string): any {
        return { uri };
    }

    documentUri(documentReference: any): string | undefined {
        return documentReference?.uri;
    }

    async documentRecord(documentReference: any): Promise<[string, Record<string, any>] | undefined> {
        const record = await this.readDocumentRecord(documentReference.uri);
        return record ? [documentReference.uri, record] : undefined;
    }

    async newDocumentRecordId(collectionDatabase: CollectionDatabase<DatabaseDocument>): Promise<string> {
        const response = await fetch(this.buildUrl(collectionDatabase.uri(), 'newId'));
        const { id } = await response.json();
        return id;
    }

    async createDocumentRecord(uri: string, documentRecord: Record<string, any>): Promise<void> {
        await fetch(this.buildUrl(uri), {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(documentRecord)
        });
    }

    async readDocumentRecord(uri: string): Promise<Record<string, any> | undefined> {
        const response = await fetch(this.buildUrl(uri));
        if (response.status === 404) return undefined;
        return await response.json();
    }

    async updateDocumentRecord(uri: string, documentRecord: Record<string, any>): Promise<void> {
        await fetch(this.buildUrl(uri), {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(documentRecord)
        });
    }

    async deleteDocumentRecord(uri: string): Promise<boolean> {
        const response = await fetch(this.buildUrl(uri), { method: 'DELETE' });
        return response.ok;
    }

    async database(database: Database<DatabaseDocument>): Promise<Map<string, DatabaseDocument>> {
        log.traceIn(`(${database.collectionName()})`, "database()");
        try {
            const response = await fetch(this.buildUrl(database.uri()));
            if (!response.ok) throw new Error(response.statusText);
            const records: any[] = await response.json();
            const result = new Map<string, DatabaseDocument>();

            await Promise.all(records.map(async (record) => {
                const documentPath = record.path || `${database.path()}/${record.id}`;
                const doc = await databaseServiceFactory!.get().databaseFactory.documentFromRecord(
                    documentPath,
                    record
                ) as DatabaseDocument;
                if (doc) result.set(documentPath, doc);
            }));

            return result;
        } catch (error) {
            log.warn("Error fetching database", error);
            throw error;
        }
    }
}