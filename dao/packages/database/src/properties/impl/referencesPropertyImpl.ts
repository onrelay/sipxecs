import { Database } from "../../core/spec/database";
import { DatabaseDocument } from "../../core/spec/databaseDocument";
import { DatabaseObject } from "../../core/spec/databaseObject";
import { DocumentsProperty } from "../spec/documentsProperty";
import { PropertyTypes, PropertyType } from "../../core/defs/propertyType";
import { AbstractDocumentsProperty } from "./abstractDocumentsProperty";

export class ReferencesPropertyImpl<DerivedDocument extends DatabaseDocument> 
    extends AbstractDocumentsProperty<DerivedDocument> implements DocumentsProperty<DerivedDocument> {

 
    constructor( parent : DatabaseObject, 
        onSelectDatabases? : () => (Database<DerivedDocument> | undefined)[],
        reciprocalKey? : keyof DerivedDocument ) {

        super( parent, PropertyTypes.References as PropertyType, onSelectDatabases, reciprocalKey ); 
    }    


}
 