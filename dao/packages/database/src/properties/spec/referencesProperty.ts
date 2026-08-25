import { DatabaseDocument } from "../../core/spec/databaseDocument";
import { DocumentsProperty } from "./documentsProperty";


export interface ReferencesProperty<DerivedDocument extends DatabaseDocument> 
    extends DocumentsProperty<DerivedDocument>{

}


