import { Database } from "../../api/dao/database";
import { DatabaseDocument } from "../../api/dao/databaseDocument";
import { DatabaseObject } from "../../api/dao/databaseObject";
import { DocumentsProperty } from "../../api/properties/documentsProperty";
import { PropertyTypes, PropertyType } from "../../api/types/propertyType";
import { AbstractDocumentsProperty } from "./abstractDocumentsProperty";

export class ReferencesPropertyImpl<DerivedDocument extends DatabaseDocument> 
    extends AbstractDocumentsProperty<DerivedDocument> implements DocumentsProperty<DerivedDocument> {

 
    constructor( parent : DatabaseObject, 
        onSelectDatabases? : () => (Database<DerivedDocument> | undefined)[],
        reciprocalKey? : keyof DerivedDocument ) {

        super( parent, PropertyTypes.References as PropertyType, onSelectDatabases, reciprocalKey ); 
    }    


}
 