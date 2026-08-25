import { DatabaseDocument } from "../spec/databaseDocument"
import { DatabaseFilter } from "./databaseFilter"
import { Database } from "../spec/database"
import { DatabaseSortOrder } from "./databaseSortOrder"
import { DateRange } from "../types/dateRange"

export type DatabaseQuery<DerivedDocument extends DatabaseDocument> = {

    databases?: Database<DerivedDocument>[],

    ignoreDocumentNames?: string[],

    dateRange?: DateRange,

    databaseFilters?: Map<string, DatabaseFilter>,

    databaseSortOrders?: Map<string, DatabaseSortOrder>,

    includeHistoric?: boolean

}

