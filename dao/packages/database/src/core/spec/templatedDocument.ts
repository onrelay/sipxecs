
import { SubdocumentProperty } from "../../properties/spec/subdocumentProperty";
import { TemplateProperty } from "../../properties/spec/templateProperty";
import { DatabaseDocument } from "./databaseDocument";
import { Template } from "../../documents/spec/template";
import { TemplatedProperties } from "./templatedProperties";

export const TemplatedDocumentName = "templatedDocument";

export interface TemplatedDocument extends DatabaseDocument {
    
    templatePath() : string | undefined;    

    templateUri() : string | undefined;    

    readonly template : TemplateProperty<Template<TemplatedDocument>>;

    templatedProperties? : SubdocumentProperty<TemplatedProperties> 

}

