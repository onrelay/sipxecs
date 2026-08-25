import { Database } from "../../core/spec/database";
import { DatabaseDocument } from "../../core/spec/databaseDocument";
import { DatabaseObject } from "../../core/spec/databaseObject";
import { ReferenceProperty } from "../spec/referenceProperty";
import { PropertyTypes, PropertyType } from "../../core/defs/propertyType";
import { AbstractDocumentProperty } from "./abstractDocumentProperty";

export class ReferencePropertyImpl<DerivedDocument extends DatabaseDocument> 
    extends AbstractDocumentProperty<DerivedDocument> implements ReferenceProperty<DerivedDocument> {

    constructor( parent : DatabaseObject, 
        onSelectDatabases? : () => (Database<DerivedDocument> | undefined)[],
        reciprocalKey? : keyof DerivedDocument ) {

        super( parent, PropertyTypes.Reference as PropertyType, onSelectDatabases, reciprocalKey ); 
    }  
     
}
