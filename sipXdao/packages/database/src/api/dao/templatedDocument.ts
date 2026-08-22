
import { SubdocumentProperty } from "../properties/subdocumentProperty";
import { TemplateProperty } from "../properties/templateProperty";
import { DatabaseDocument } from "./databaseDocument";
import { Template } from "./template";
import { TemplatedProperties } from "./templatedProperties";

export const TemplatedDocumentName = "templatedDocument";

export interface TemplatedDocument extends DatabaseDocument {
    
    templatePath() : string | undefined;    

    templateUri() : string | undefined;    

    readonly template : TemplateProperty<Template<TemplatedDocument>>;

    templatedProperties? : SubdocumentProperty<TemplatedProperties> 

}

