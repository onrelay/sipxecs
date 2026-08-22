import { DatabaseObject } from "../../api/dao/databaseObject";
import { LongTextProperty } from "../../api/properties/LongTextProperty";
import { PropertyTypes, PropertyType } from "../../api/types/propertyType";
import { AbstractBasicProperty } from "./abstractBasicProperty";

export class LongTextPropertyImpl extends AbstractBasicProperty<string> implements LongTextProperty {

    constructor( parent : DatabaseObject, defaultValue? : string ) {
        super( parent, PropertyTypes.LongText as PropertyType, defaultValue );  

    }

    compareTo( other : LongTextProperty ) : number {

        return this.compareValue( other.value() );
    }
 
     compareValue( otherValue : string | undefined ) : number { 
 
         const value = this.value();
 
         if( value == null && otherValue == null ) {
             return 0;
         }
 
         if( value != null && otherValue == null ) {
             return 1;
         }
 
         if( value == null && otherValue != null ) {
             return -1;
         }
 
         return this.value()!.toString().localeCompare( otherValue! );
    }

    requiresSelect() : boolean { 
        return false;
    }

}