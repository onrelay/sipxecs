import { DatabaseDocument } from "../../api/dao/databaseDocument";
import { DatabaseObject } from "../../api/dao/databaseObject";
import { Template } from "../../api/dao/template";
import { TemplatedDocument } from "../../api/dao/templatedDocument";
import { TemplateProperty } from "../../api/properties/templateProperty";
import { PropertyTypes, PropertyType } from "../../api/types/propertyType";
import { AbstractDocumentProperty } from "./abstractDocumentProperty";

export class TemplatePropertyImpl<T extends Template<TemplatedDocument>> 
    extends AbstractDocumentProperty<T> implements TemplateProperty<T> {

    constructor( parent : DatabaseObject ) {

        super( parent, PropertyTypes.Template as PropertyType, undefined, "instances" ); 
    }  
     
}
