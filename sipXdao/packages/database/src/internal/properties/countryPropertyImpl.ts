import { DatabaseObject } from "../../api/dao/databaseObject";
import { CountryProperty } from "../../api/properties/countryProperty";
import { PropertyTypes, PropertyType } from "../../api/types/propertyType";
import { AbstractBasicProperty } from "./abstractBasicProperty";

export class CountryPropertyImpl extends AbstractBasicProperty<string> implements CountryProperty {

    constructor( parent : DatabaseObject, defaultValue? : string ) {
        super( parent, PropertyTypes.Country as PropertyType, defaultValue );  
    }

    compareTo( other : CountryProperty ) : number {

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
}