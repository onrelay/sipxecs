import { DatabaseDocument } from "../../core/spec/databaseDocument";
import { DocumentsProperty } from "./documentsProperty";


export interface SymbolicCollectionProperty<DerivedDocument extends DatabaseDocument> 
    extends DocumentsProperty<DerivedDocument>{

}


