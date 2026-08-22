
import { DatabaseDocument } from "../dao/databaseDocument";
import { DocumentProperty } from "./documentProperty";

export interface ReferenceProperty<DerivedDocument extends DatabaseDocument> extends DocumentProperty<DerivedDocument> {

}

