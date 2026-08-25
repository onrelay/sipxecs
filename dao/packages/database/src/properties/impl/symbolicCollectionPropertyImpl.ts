import { Database } from "../../core/spec/database";
import { DatabaseDocument } from "../../core/spec/databaseDocument";
import { DatabaseObject } from "../../core/spec/databaseObject";
import { SymbolicCollectionProperty } from "../spec/symbolicCollectionProperty";
import { PropertyTypes, PropertyType } from "../../core/defs/propertyType";
import { AbstractDocumentsProperty } from "./abstractDocumentsProperty";

export class SymbolicCollectionPropertyImpl<DerivedDocument extends DatabaseDocument> 
    extends AbstractDocumentsProperty<DerivedDocument> implements SymbolicCollectionProperty<DerivedDocument> {

    constructor( parent : DatabaseObject, 
        onSelectDatabases? : () => (Database<DerivedDocument> | undefined)[],
        reciprocalKey? : keyof DerivedDocument ) {

        super( parent, PropertyTypes.SymbolicCollection as PropertyType, onSelectDatabases, reciprocalKey ); 
    } 
}


