import { Database } from "../../api/dao/database";
import { DatabaseDocument } from "../../api/dao/databaseDocument";
import { DatabaseObject } from "../../api/dao/databaseObject";
import { ReferenceProperty } from "../../api/properties/referenceProperty";
import { PropertyTypes, PropertyType } from "../../api/types/propertyType";
import { AbstractDocumentProperty } from "./abstractDocumentProperty";

export class ReferencePropertyImpl<DerivedDocument extends DatabaseDocument> 
    extends AbstractDocumentProperty<DerivedDocument> implements ReferenceProperty<DerivedDocument> {

    constructor( parent : DatabaseObject, 
        onSelectDatabases? : () => (Database<DerivedDocument> | undefined)[],
        reciprocalKey? : keyof DerivedDocument ) {

        super( parent, PropertyTypes.Reference as PropertyType, onSelectDatabases, reciprocalKey ); 
    }  
     
}
