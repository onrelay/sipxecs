import { AbstractDatabaseProperty } from "../../core/base/abstractDatabaseProperty";
import { DatabaseObject } from "../../core/spec/databaseObject";
import { EmptyProperty } from "../spec/emptyProperty";
import { PropertyType, PropertyTypes } from "../../core/defs/propertyType";


export class EmptyPropertyImpl
    extends AbstractDatabaseProperty<undefined> implements EmptyProperty {

    constructor( parent : DatabaseObject ) {
        super( parent, PropertyTypes.Empty as PropertyType ); 
    }

    value() : undefined {
        return undefined;
    }

    setValue( value: undefined ): void {
    }

    fromRecord( data: Record<string, any>): void {}

    async toRecord( data: Record<string, any>, force? : boolean ) : Promise<void> {}

    compareTo( other : EmptyProperty ) : number {

        return 0;
    }


    compareValue( value : undefined ) : number {
        return 0;
    }

    includes( other : EmptyProperty ) : boolean {
        return true;
    }

    includesValue( value : undefined ) : boolean {
        return true;
    }
}