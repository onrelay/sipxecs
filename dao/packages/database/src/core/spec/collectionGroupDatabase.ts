import { DatabaseDocument } from "./databaseDocument";
import { Database } from "./database";
import { DatabaseManager } from "./databaseManager";

export interface CollectionGroupDatabase<DerivedDocument extends DatabaseDocument>  extends Database<DerivedDocument> {

    readonly databaseManager : DatabaseManager,

    readonly allowRootCollection : boolean,

    readonly encrypted : boolean,

} 