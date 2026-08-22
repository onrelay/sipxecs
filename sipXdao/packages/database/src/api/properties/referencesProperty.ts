import { DatabaseDocument } from "../dao/databaseDocument";
import { DocumentsProperty } from "./documentsProperty";


export interface ReferencesProperty<DerivedDocument extends DatabaseDocument> 
    extends DocumentsProperty<DerivedDocument>{

}


