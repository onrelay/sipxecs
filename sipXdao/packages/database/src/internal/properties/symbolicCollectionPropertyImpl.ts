import { Database } from "../../api/dao/database";
import { DatabaseDocument } from "../../api/dao/databaseDocument";
import { DatabaseObject } from "../../api/dao/databaseObject";
import { SymbolicCollectionProperty } from "../../api/properties/symbolicCollectionProperty";
import { PropertyTypes, PropertyType } from "../../api/types/propertyType";
import { AbstractDocumentsProperty } from "./abstractDocumentsProperty";

export class SymbolicCollectionPropertyImpl<DerivedDocument extends DatabaseDocument> 
    extends AbstractDocumentsProperty<DerivedDocument> implements SymbolicCollectionProperty<DerivedDocument> {

    constructor( parent : DatabaseObject, 
        onSelectDatabases? : () => (Database<DerivedDocument> | undefined)[],
        reciprocalKey? : keyof DerivedDocument ) {

        super( parent, PropertyTypes.SymbolicCollection as PropertyType, onSelectDatabases, reciprocalKey ); 
    } 
}


