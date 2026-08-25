import { AbstractObservable, Monitor, Observable, Observation, Observations, Translator } from "@dao/common";
import { DatabaseDocument } from "../spec/databaseDocument";
import { DatabaseQuery } from "../types/databaseQuery";
import { DatabaseObserver } from "../spec/databaseObserver";
import { Database } from "../spec/database";
import { DateRange } from "../types/dateRange";
import { DatabaseFilter } from "../types/databaseFilter";
import { DatabaseSortOrder } from "../types/databaseSortOrder";
import { ReferenceHandle } from "./referenceHandle";
import { SortOrientations } from "../defs/sortOrientation";
import { log } from "../base/abstractDatabaseService";
import { configurationServiceFactory } from "@dao/configuration";
import { databaseServiceFactory } from "./databaseServiceFactory";

export class DatabaseObserverImpl<DerivedDocument extends DatabaseDocument> 
    extends AbstractObservable implements DatabaseObserver<DerivedDocument> {

    constructor( databaseQuery?: DatabaseQuery<DerivedDocument> ) { 

        super();

        this.databases = this.databases.bind(this);
        this.defaultDatabase = this.defaultDatabase.bind(this);
        this.queryDocumentName = this.queryDocumentName.bind(this);
        this.queryTemplatePath = this.queryTemplatePath.bind(this);

        this.filteredDocuments = this.filteredDocuments.bind(this);
        this.filteredHandles = this.filteredHandles.bind(this);
        this.documents = this.documents.bind(this);
        this.handles = this.handles.bind(this);

        this.newDocument = this.newDocument.bind(this);
        this.referenceDocument = this.referenceDocument.bind(this);

        this.update = this.update.bind(this);
        this.onNotify = this.onNotify.bind(this);

        this.setDatabaseQuery = this.setDatabaseQuery.bind(this);
        this.databaseQuery = this.databaseQuery.bind(this);

        this.setDatabases = this.setDatabases.bind(this);
        this.databases = this.databases.bind(this);

        this.setDateRange = this.setDateRange.bind(this);
        this.dateRange = this.dateRange.bind(this);

        this.setDatabaseFilters = this.setDatabaseFilters.bind(this);
        this.databaseFilters = this.databaseFilters.bind(this);

        this.setDatabaseSortOrders = this.setDatabaseSortOrders.bind(this);
        this.databaseSortOrders = this.databaseSortOrders.bind(this);

        this.setIncludeHistoric = this.setIncludeHistoric.bind(this);
        this.includeHistoric = this.includeHistoric.bind(this);

        this.setIgnoreDocumentNames = this.setIgnoreDocumentNames.bind(this);
        this.ignoreDocumentNames = this.ignoreDocumentNames.bind(this);

        this.subscribe = this.subscribe.bind(this);
        this.release = this.release.bind(this);
        this.refreshSubscription = this.refreshSubscription.bind(this);
        this.startSubscription = this.startSubscription.bind(this);
        this.endSubscription = this.endSubscription.bind(this);
        this.sortAndFilterDocuments = this.sortAndFilterDocuments.bind(this);

        this.setDatabaseQuery( databaseQuery );

        //log.traceInOut("constructor()",);
    }


    setDatabaseQuery( databaseQuery?: DatabaseQuery<DerivedDocument>) : boolean {

        //log.traceIn("setDatabaseQuery()" );

        try {

            let changed = false;

            changed = this.setDatabases( databaseQuery?.databases as Database<DerivedDocument>[] ) || changed;
            
            changed = this.setIgnoreDocumentNames( databaseQuery?.ignoreDocumentNames ) || changed;

            changed = this.setDateRange( databaseQuery?.dateRange ) || changed;

            changed = this.setDatabaseFilters( databaseQuery?.databaseFilters ) || changed;

            changed = this.setDatabaseSortOrders( databaseQuery?.databaseSortOrders ) || changed;

            changed = this.setIncludeHistoric( databaseQuery?.includeHistoric ) || changed;

            //log.traceOut("setDatabaseQuery()", {changed} );

            return changed;

        } catch (error) {

            log.warn("setDatabaseQuery()", "Error setting database query", error);

            throw new Error( (error as any).message );
        }
    }

    databaseQuery() : DatabaseQuery<DerivedDocument> {
        return this._databaseQuery;
    }

    databases() : Database<DerivedDocument>[] | undefined {
        return this._databaseQuery.databases as Database<DerivedDocument>[]; 
    }

    setDatabases( databases : Database<DerivedDocument>[] | undefined ) : boolean {

        //log.traceIn("setDatabases()" );

        try {

            if( databases == null && this._databaseQuery.databases == null ) {

                this._pendingRefreshSubscription = false;

                this._pendingSortAndFilter = false;

                //log.traceOut("setDatabases()", "Emtpy databases" );
                return false;
            }

            if( databases == null && this._databaseQuery.databases != null ) {

                delete this._databaseQuery.databases;

                this._pendingRefreshSubscription = true;

                this._pendingSortAndFilter = true;

                //log.traceOut("setDatabases()", "Removed databases" );
                return true;
            }

            if( databases != null && this._databaseQuery.databases == null ) {

                this._databaseQuery.databases = databases;

                this._pendingRefreshSubscription = true;

                this._pendingSortAndFilter = true;

                this._referenceDocument = this.newDocument();

                //log.traceOut("setDatabases()", "Added databases" );
                return true;
            }

            if ( databases!.length !== this._databaseQuery.databases?.length ||
                databases![0].defaultDocumentName() !== this._databaseQuery.databases![0].defaultDocumentName() ) { 

                this._databaseQuery.databases = databases; 

                this._pendingRefreshSubscription = true;

                this._pendingSortAndFilter = true;

                this._referenceDocument = this.newDocument();

                //log.traceOut("setDatabases()", "Updated databases" );
                return true;
            }

            //log.traceOut("setDatabases()", "No change" );
            return false;

        } catch (error) {
            log.warn("setDatabases()", "Error setting databases ", error);

            throw new Error( (error as any).message );
        }
    }

    defaultDatabase(): Database<DerivedDocument> | undefined {

        if (this._databaseQuery.databases == null ||
            this._databaseQuery.databases.length === 0) {
            return undefined
        }

        return this._databaseQuery.databases[0] as Database<DerivedDocument>;
    }


    setDateRange( dateRange?: DateRange ) : boolean {

        //log.traceIn("setDateRange()", {dateRange});

        try {
            if( JSON.stringify( dateRange ) === JSON.stringify( this._databaseQuery.dateRange ) ) {

                //log.traceOut("setDateRange()", "no change");
                return false;
            }

            if( this._referenceDocument != null && this._databaseQuery.dateRange != null ) {

                this._referenceDocument.startDate.setValue( undefined );

                this._referenceDocument.endDate.setValue( undefined );
            }

            this._databaseQuery.dateRange = dateRange;

            if( this._referenceDocument != null && dateRange != null ) {

                this._referenceDocument.startDate.setValue( dateRange?.from );

                this._referenceDocument.endDate.setValue( dateRange?.to );
            }

            this._pendingSortAndFilter = true;

            //log.traceOut("setDateRange()", "Updated");
            return true;

        } catch (error) {
            log.warn("setDateRange()", "Error setting date range ", error);

            throw new Error( (error as any).message );
        }
    }

    dateRange() : DateRange | undefined {
        return this._databaseQuery.dateRange;
    }

    setDatabaseFilters( databaseFilters?: Map<string, DatabaseFilter>) : boolean {

        //log.traceIn("setDatabaseFilters()", {databaseFilters});

        try {

            if (JSON.stringify(databaseFilters == null ? "" : Array.from(databaseFilters.values())) ===
                JSON.stringify(this._databaseQuery.databaseFilters == null ? "" : Array.from(this._databaseQuery.databaseFilters.values()))) {

                //log.traceOut("setDatabaseFilters()", "No change");
                return false;
            }

            if( this._referenceDocument != null &&
                this._databaseQuery.databaseFilters != null ) {

                for( const existingDatabaseFilter of this._databaseQuery.databaseFilters.values() ) {

                    const filterProperty = this._referenceDocument.property( existingDatabaseFilter.property );

                    if( filterProperty != null ) {
                        filterProperty.setValue( undefined );
                    }
                }
            }

            this._databaseQuery.databaseFilters = databaseFilters;

            if( this._referenceDocument != null && databaseFilters != null ) {

                for( const databaseFilter of databaseFilters.values() ) {

                    const filterProperty = this._referenceDocument.property( databaseFilter.property );

                    if( filterProperty != null ) {
                        filterProperty.setValue( databaseFilter.value );
                    }
                }
            }

            this._pendingSortAndFilter = true;

            //log.traceOut("setDatabaseFilters()", "Updated");
            return true;

        } catch (error) {
            log.warn("setDatabaseFilters()", "Error setting database filters ", error);

            throw new Error( (error as any).message );
        }
    }

    databaseFilters() : Map<string, DatabaseFilter> | undefined {
        return this._databaseQuery.databaseFilters;
    }

    setDatabaseSortOrders( databaseSortOrders?: Map<string, DatabaseSortOrder>) : boolean {

        //log.traceIn("setDatabaseSortOrders()", {databaseSortOrders});

        try {

            if (JSON.stringify(databaseSortOrders == null ? "" : Array.from(databaseSortOrders.values())) ===
                JSON.stringify(this._databaseQuery.databaseSortOrders == null ? "" : Array.from(this._databaseQuery.databaseSortOrders.values()))) {

                //log.traceOut("setDatabaseSortOrders()", "No change");
                return false;
            }

            this._databaseQuery.databaseSortOrders = databaseSortOrders;

            this._pendingSortAndFilter = true;

            //log.traceOut("setDatabaseSortOrders()", "Updated");
            return true;

        } catch (error) {
            log.warn("setDatabaseSortOrders()", "Error setting database sort orders ", error);

            throw new Error( (error as any).message );
        }
    }

    databaseSortOrders() : Map<string, DatabaseSortOrder> | undefined {
        return this._databaseQuery.databaseSortOrders;
    }

    setIncludeHistoric( includeHistoric?: boolean ) : boolean {

        //log.traceIn("setIncludeHistoric()", {includeHistoric});

        try {
            if( includeHistoric === this._databaseQuery.includeHistoric ) {

                //log.traceOut("setIncludeHistoric()", "no change");
                return false;
            }

            this._databaseQuery.includeHistoric = includeHistoric;

            this._pendingSortAndFilter = true;

            //log.traceOut("setIncludeHistoric()", "Updated");
            return true;

        } catch (error) {
            log.warn("setDatabaseFilters()", "Error setting include historic ", error);

            throw new Error( (error as any).message );
        }
    }

    includeHistoric() : boolean | undefined {
        return this._databaseQuery.includeHistoric;
    }

    setIgnoreDocumentNames( excludedDocumentNames?: string[] ) : boolean {

        //log.traceIn("setIgnoreDocumentNames()", {ignoreDocumentNames: excludedDocumentNames});

        try {
            if( JSON.stringify( excludedDocumentNames ) === JSON.stringify( this._databaseQuery.ignoreDocumentNames ) ) {

                //log.traceOut("setIgnoreDocumentNames()", "no change"); 
                return false;
            }

            this._databaseQuery.ignoreDocumentNames = excludedDocumentNames;

            this._pendingSortAndFilter = true;

            //log.traceOut("setIgnoreDocumentNames()", "Updated");
            return true;

        } catch (error) {
            log.warn("setIgnoreDocumentNames()", "Error setting ignore document names ", error);

            throw new Error( (error as any).message );
        }
    }

    ignoreDocumentNames() : string[] | undefined {
        return this._databaseQuery.ignoreDocumentNames;
    }

    queryDocumentName(): string | undefined {

        return this.defaultDatabase()?.queryDocumentName();
    }

    documentNames(): string[] | undefined {

        return this.defaultDatabase()?.documentNames();
    }

    defaultDocumentName(): string | undefined {

        return this.defaultDatabase()?.defaultDocumentName(); 
    }

    queryTemplatePath(): string | undefined {

        return this.defaultDatabase()?.queryTemplatePath();
    }


    newDocument() : DerivedDocument | undefined {
        return this.defaultDatabase()?.newDocument(); 
    }
    
    referenceDocument() : DerivedDocument | undefined {

        return this._referenceDocument;
    }


    filteredDocuments() : Map<string,DerivedDocument> {

        const result = new Map<string,DerivedDocument>();

        this._filteredHandles.forEach( (handle, path) => {
            result.set( path, handle.databaseDocument! );
        });

        return result;
    }

    documents() : Map<string,DerivedDocument> {
        const result = new Map<string,DerivedDocument>();

        this._handles.forEach( (handle, path) => {
            result.set( path, handle.databaseDocument! );
        });

        return result;    
    }

    document( documentPath : string ) : DerivedDocument | undefined {
        return this._handles.get( documentPath )?.databaseDocument;
    }

    filteredHandles() : Map<string,ReferenceHandle<DerivedDocument>> {
        return new Map<string,ReferenceHandle<DerivedDocument>>( [...this._filteredHandles] );
    }

    handles() : Map<string,ReferenceHandle<DerivedDocument>> {
        return new Map<string,ReferenceHandle<DerivedDocument>>( [...this._handles] ); 
    }

    handle( documentPath : string ) : ReferenceHandle<DerivedDocument> | undefined {
        return this._handles.get( documentPath );
    }

    count() : number {
        return this._handles.size;
    }

    filteredCount() : number {
        return this._filteredHandles.size;
    }

    async update() : Promise<boolean> {

        //log.traceIn("update()");

        try { 
            if( this._pendingRefreshSubscription ) {

                this._pendingRefreshSubscription = false;

                this._pendingSortAndFilter = false;

                await this.refreshSubscription();

                //log.traceOut("update()", "refreshed subscription");
                return true;
            }

            if( this._pendingSortAndFilter ) {

                this._pendingSortAndFilter = false;

                await this.sortAndFilterDocuments( Observations.Update as Observation );

                //log.traceOut("update()", "Updated database filter");
                return true;
            }

            //log.traceIn("update()", "No update needed" );
            return false;

        } catch (error) {

            log.warn("update()", error);

            throw new Error((error as any).message);
        }
    }

    protected async monitor( newMonitor : Monitor ): Promise<void> {

        //log.traceIn("monitor()");

        try {

            if( this._releaseTimeoutId != null) {

                clearTimeout( this._releaseTimeoutId );

                delete this._releaseTimeoutId;
            }

            await this.update();

            if( newMonitor.onNotify != null ) {

                await newMonitor.onNotify( this, Observations.Create as Observation );
            }

            //log.traceOut( "monitor()" );

        } catch (error) {
            log.warn("Error starting monitoring", error);

            throw new Error( (error as any).message );
        }
    }


    protected async release(): Promise<void> {

        //log.traceIn("release()");

        try {
            if (this._releaseTimeoutId != null) {
                //log.traceOut("release()", "Already being released");
                return;
            }

            const cacheReleaseSeconds = +configurationServiceFactory!.get().config(
                "database", "cacheReleaseSeconds")!;

            if (isNaN(cacheReleaseSeconds)) {
                throw new Error("Invalid cache release timeout: " + cacheReleaseSeconds);
            }

            this._releaseTimeoutId = setTimeout(this.timedRelease, cacheReleaseSeconds * 1000 );

            //log.traceOut("release()", "Set timeout", cacheReleaseSeconds * 1000);

        } catch (error) {

            log.warn("Error releasing database observer", error);

            throw new Error( (error as any).message );
        }
    }

    private timedRelease = async (): Promise<void> => {

        //log.traceIn("timedRelease()");

        try {

            delete this._releaseTimeoutId;

            if( !super.hasObservers() ) {

                this._pendingRefreshSubscription = true;

                await this.endSubscription();
            }

            //log.traceOut("timedRelease()");

        } catch (error) {

            log.warn("Error releasing database observer ", error);
        }
    }

    private async refreshSubscription() {

        log.traceIn("refreshSubscription()");

        try {
            if( this._databaseQuery.databases == null || this._databaseQuery.databases.length === 0) {
                log.traceOut("refreshSubscription()", "no databases");
                return;
            }

            if( this._subscribedDatabases == null  || this._subscribedDatabases.length === 0 ) {

                await this.startSubscription();

                log.traceOut("refreshSubscription()", "no previous subscriptions");
                return;
            }

            for (const subscribedDatabase of this._subscribedDatabases) {

                if( !this._databaseQuery.databases.includes( subscribedDatabase as Database<DerivedDocument> ) ) {
                    
                    await subscribedDatabase.unsubscribe(this);

                    const index = this._subscribedDatabases.indexOf( subscribedDatabase );
              
                    this._subscribedDatabases.splice( index, 1 );
                    
                }
            }

            const monitor = {
                observer: this,
                onNotify: this.onNotify
            } as Monitor;
            
            for (const database of this._databaseQuery.databases ) {

                if( !this._subscribedDatabases.includes( database as Database<DerivedDocument> ) ) {
                    
                    await database.subscribe(monitor);

                    this._subscribedDatabases.push( database as Database<DerivedDocument> );
                }
            }
            
            log.traceOut("startSubscription()");

        } catch (error) {
            log.warn("startSubscription()", "Error refreshing", error);

            log.traceOut("startSubscription()", "error");
        }
    }



    private async startSubscription() {

        log.traceIn("startSubscription()");

        try {
            if( this._subscribedDatabases != null ) {
                log.traceOut("startSubscription()", "already subscribed");
                return;
            }
            if( this._databaseQuery.databases == null || this._databaseQuery.databases.length === 0) {
                log.traceOut("startSubscription()", "no databases");
                return;
            }

            const monitor = {
                observer: this,
                onNotify: this.onNotify
            } as Monitor;
            
            this._subscribedDatabases = [];

            for (const database of this._databaseQuery.databases ) {

                await database.subscribe(monitor);

                this._subscribedDatabases.push( database as Database<DerivedDocument> );
            }
            
            log.traceOut("startSubscription()");

        } catch (error) {
            log.warn("startSubscription()", "Error refreshing", error);

            log.traceOut("startSubscription()", "error");
        }
    }


    private async endSubscription() {

        log.traceIn("endSubscription()");

        try {

            if( this._subscribedDatabases == null || this._subscribedDatabases.length === 0 ) {
                log.traceOut("endSubscription()", "not subscribed");
                return;
            }

            for( const subscribedDatabase of this._subscribedDatabases ) {

                await subscribedDatabase.unsubscribe(this);

            }

            delete this._subscribedDatabases;

            this._filteredHandles = new Map<string,ReferenceHandle<DerivedDocument>>();

            this._handles = new Map<string,ReferenceHandle<DerivedDocument>>();

            log.traceOut("endSubscription()");

        } catch (error) {
            log.warn("endSubscription()", "Error cleaning up subscription", error);

            log.traceOut("endSubscription()", "error");
        }
    }


    private onNotify = async (
        observable: Observable,
        observation: Observation,
        objectId?: string,
        object?: object ): Promise<void> => {

        try {
            log.traceIn("onNotify()", Observations[observation], objectId);

            let documentPath;
            let databaseDocument;

            switch (observation) {

                case Observations.Create:
                case Observations.Update:
                    {
                        if (databaseServiceFactory!.get().databaseFactory.isUriDatabase(objectId!)) {

                            const initialResult = object as Map<string, DerivedDocument>;

                            if (initialResult != null) {

                                for (const databaseDocument of initialResult.values()) {

                                    const handle = databaseDocument.referenceHandle() as ReferenceHandle<DerivedDocument>;

                                    handle.databaseDocument = databaseDocument;

                                    if (this._databaseQuery.ignoreDocumentNames == null ||
                                        !this._databaseQuery.ignoreDocumentNames.includes(databaseDocument.recordName())) {

                                        this._handles!.set(handle.path, handle,  );
                                    }
                                }
                            }
                        }
                        else {
                            documentPath = objectId!;

                            databaseDocument = object as DerivedDocument;

                            const handle = databaseDocument.referenceHandle() as ReferenceHandle<DerivedDocument>;

                            handle.databaseDocument = databaseDocument;

                            if (this._databaseQuery.ignoreDocumentNames == null ||
                                !this._databaseQuery.ignoreDocumentNames.includes(databaseDocument.recordName())) {

                                this._handles!.set(handle.path, handle);
                            }
                        }
                        break; 
                    }
                case Observations.Delete:
                    {
                        documentPath = objectId!;

                        databaseDocument = object as DerivedDocument;

                        this._handles!.delete(documentPath); 

                        break;
                    }
                default:
                    throw new Error("Unrecognized observation: " + observation);
            }

            await this.sortAndFilterDocuments( observation, documentPath, databaseDocument );

            log.traceOut("onNotify()");

        } catch (error) {

            log.warn("onNotify()", error);

            throw new Error((error as any).message);
        }
    }

    private async sortAndFilterDocuments( 
        observation: Observation, 
        documentPath? : string, 
        databaseDocument? : DerivedDocument,
        translator? : Translator ) {

        log.traceIn("sortAndFilterDocuments()");

        try { 

            if( this._databaseQuery.databaseSortOrders != null ) {

                log.debug("sortAndFilterDocuments()", "sorting", this._databaseQuery.databaseSortOrders );

                this._handles = new Map([...this._handles].sort((entryA, entryB) => {

                    const databaseDocumentA = entryA[1].databaseDocument!;

                    const databaseDocumentB = entryB[1].databaseDocument!;

                    for( const databaseSortOrder of this._databaseQuery.databaseSortOrders!.values() ) {

                        const propertyA = databaseDocumentA.property( databaseSortOrder.property )!;

                        const propertyB = databaseDocumentB.property( databaseSortOrder.property )!;

                        const compare = propertyA.compareTo( propertyB, translator?.translate );

                        if( compare !== 0 ) {

                            switch( databaseSortOrder.orientation ) {

                                case SortOrientations.Ascending:

                                    return compare;
                                
                                case SortOrientations.Descending:

                                    return -compare;
                            }
                        }
                    }
                    return 0;
                  })); 
            }

            this._filteredHandles = new Map<string,ReferenceHandle<DerivedDocument>>();

            for (const handle of this._handles!.values()!) {

                const databaseDocument = handle.databaseDocument!;

                if (!databaseDocument.matchFilter( {
                    from: this._databaseQuery.dateRange?.from, 
                    to: this._databaseQuery.dateRange?.to, 
                    matchHistoric: this._databaseQuery.includeHistoric, 
                    databaseFilters: this._databaseQuery.databaseFilters })) { 

                    //log.debug("sortAndFilterDocuments()", "Document property filtered", databaseDocument);
                    continue;
                }

                this._filteredHandles.set( handle.path, handle );
            }

            this._pendingSortAndFilter = false;

            await super.notify( observation, documentPath, databaseDocument );

            log.traceOut("sortAndFilterDocuments()", this._filteredHandles.size );

        } catch (error) {
            log.warn( "sortAndFilterDocuments", "Error filtering documents", error);

            throw new Error( (error as any).message );
        }
    }


    private _databaseQuery = {} as DatabaseQuery<DerivedDocument>;

    private _handles = new Map<string,ReferenceHandle<DerivedDocument>>();

    private _filteredHandles = new Map<string, ReferenceHandle<DerivedDocument>>();

    private _subscribedDatabases? : Database<DerivedDocument>[];

    private _pendingSortAndFilter = false;

    private _pendingRefreshSubscription = false;

    private _releaseTimeoutId? : number;

    private _referenceDocument? : DerivedDocument;

}
