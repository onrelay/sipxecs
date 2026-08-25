import { DatabaseObject } from "../../core/spec/databaseObject";
import { BooleanProperty } from "../spec/booleanProperty";
import { PropertyType, PropertyTypes } from "../../core/defs/propertyType";
import { AbstractBasicProperty } from "./abstractBasicProperty";

export class BooleanPropertyImpl extends AbstractBasicProperty<boolean> implements BooleanProperty {

    constructor( parent : DatabaseObject, type? : PropertyType, defaultValue? : boolean )
    {
        super( parent, type === PropertyTypes.Boolean || type === PropertyTypes.Confirmation ? type : 
            PropertyTypes.Boolean as PropertyType, 
            defaultValue );

        if( type != null && type !== PropertyTypes.Boolean && type !== PropertyTypes.Confirmation ) {
            throw new Error("invalid type for boolean: " + type );
        }
    }

   compareTo( other : BooleanProperty ) : number {

        return this.compareValue( other.value() );
    }

    compareValue( otherValue : boolean | undefined ) : number {

        const value = this.value();

        if( value === otherValue ) { 
            return 0;
        }

        return !!value ? 1 : -1;
    }  
}