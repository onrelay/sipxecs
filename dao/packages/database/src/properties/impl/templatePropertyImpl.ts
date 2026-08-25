import { DatabaseDocument } from "../../core/spec/databaseDocument";
import { DatabaseObject } from "../../core/spec/databaseObject";
import { Template } from "../../documents/spec/template";
import { TemplatedDocument } from "../../core/spec/templatedDocument";
import { TemplateProperty } from "../spec/templateProperty";
import { PropertyTypes, PropertyType } from "../../core/defs/propertyType";
import { AbstractDocumentProperty } from "./abstractDocumentProperty";

export class TemplatePropertyImpl<T extends Template<TemplatedDocument>> 
    extends AbstractDocumentProperty<T> implements TemplateProperty<T> {

    constructor( parent : DatabaseObject ) {

        super( parent, PropertyTypes.Template as PropertyType, undefined, "instances" ); 
    }  
     
}
