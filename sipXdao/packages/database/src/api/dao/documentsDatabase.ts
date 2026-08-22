import { DatabaseDocument } from "./databaseDocument";
import { Database } from "./database";

export interface DocumentsDatabase<DerivedDocument extends DatabaseDocument>  extends Database<DerivedDocument> {

}  