import { DatabaseDocument } from "../dao/databaseDocument";
import { DocumentsProperty } from "./documentsProperty";


export interface SymbolicOwnersProperty<DerivedDocument extends DatabaseDocument> 
    extends DocumentsProperty<DerivedDocument>{

}


