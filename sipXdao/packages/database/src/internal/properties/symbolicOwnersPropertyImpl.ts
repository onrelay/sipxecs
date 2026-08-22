import { Database } from "../../api/dao/database";
import { DatabaseDocument } from "../../api/dao/databaseDocument";
import { DatabaseObject } from "../../api/dao/databaseObject";
import { SymbolicOwnersProperty } from "../../api/properties/symbolicOwnersProperty";
import { PropertyTypes, PropertyType } from "../../api/types/propertyType";
import { AbstractDocumentsProperty } from "./abstractDocumentsProperty";

export class SymbolicOwnersPropertyImpl<DerivedDocument extends DatabaseDocument> 
    extends AbstractDocumentsProperty<DerivedDocument> implements SymbolicOwnersProperty<DerivedDocument> {

    constructor( parent : DatabaseObject, 
        onSelectDatabases? : () => (Database<DerivedDocument> | undefined)[],
        reciprocalKey? : keyof DerivedDocument ) {

        super( parent, PropertyTypes.SymbolicOwners as PropertyType, onSelectDatabases, reciprocalKey ); 
    } 
}


