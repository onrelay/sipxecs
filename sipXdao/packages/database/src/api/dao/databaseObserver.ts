
import { Observable } from "@sipxdao/common";

import { DatabaseDocument } from "./databaseDocument";
import { DatabaseFilter } from "./databaseFilter";
import { DatabaseQuery } from "./databaseQuery";
import { Database } from "./database";
import { DateRange } from "../types/dateRange";
import { ReferenceHandle } from "./referenceHandle";
import { DatabaseSortOrder } from "./databaseSortOrder";

export interface DatabaseObserver<DerivedDocument extends DatabaseDocument> extends Observable {
    
    setDatabaseQuery( databaseQuery?: DatabaseQuery<DerivedDocument>) : boolean;
    databaseQuery() : DatabaseQuery<DerivedDocument>;

    setDatabases( databases : Database<DerivedDocument>[] | undefined ) : boolean;
    databases() : Database<DerivedDocument>[] | undefined;
    defaultDatabase(): Database<DerivedDocument> | undefined;

    setDateRange( dateRange?: DateRange ) : boolean;
    dateRange() : DateRange | undefined;

    setDatabaseFilters( databaseFilters?: Map<string, DatabaseFilter> ) : boolean;
    databaseFilters() : Map<string, DatabaseFilter> | undefined;

    setDatabaseSortOrders( databaseSortOrders?: Map<string, DatabaseSortOrder> ) : boolean;
    databaseSortOrders() : Map<string, DatabaseSortOrder> | undefined;

    setIncludeHistoric( includeHistoric?: boolean ) : boolean;
    includeHistoric() : boolean | undefined;

    setIgnoreDocumentNames( ignoreDocumentNames?: string[] ) : boolean;
    ignoreDocumentNames() : string[] | undefined;

    queryDocumentName(): string | undefined;

    documentNames(): string[] | undefined;

    defaultDocumentName(): string | undefined;

    queryTemplatePath() : string | undefined,

    update() : Promise<boolean>;

    newDocument() : DerivedDocument | undefined;

    referenceDocument() : DerivedDocument | undefined;

    filteredDocuments() : Map<string,DerivedDocument>;

    filteredHandles() : Map<string,ReferenceHandle<DerivedDocument>>;

    documents() : Map<string,DerivedDocument>;

    handles() : Map<string,ReferenceHandle<DerivedDocument>>;

    document( documentPath : string ) : DerivedDocument | undefined;

    handle( documentPath : string ) : ReferenceHandle<DerivedDocument> | undefined;

    count() : number;

    filteredCount() : number;

}

