import { Database } from "../../core/spec/database";
import { DatabaseDocument } from "../../core/spec/databaseDocument";
import { DatabaseObject } from "../../core/spec/databaseObject";
import { SymbolicOwnersProperty } from "../spec/symbolicOwnersProperty";
import { PropertyTypes, PropertyType } from "../../core/defs/propertyType";
import { AbstractDocumentsProperty } from "./abstractDocumentsProperty";

export class SymbolicOwnersPropertyImpl<DerivedDocument extends DatabaseDocument> 
    extends AbstractDocumentsProperty<DerivedDocument> implements SymbolicOwnersProperty<DerivedDocument> {

    constructor( parent : DatabaseObject, 
        onSelectDatabases? : () => (Database<DerivedDocument> | undefined)[],
        reciprocalKey? : keyof DerivedDocument ) {

        super( parent, PropertyTypes.SymbolicOwners as PropertyType, onSelectDatabases, reciprocalKey ); 
    } 
}


