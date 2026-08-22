import { DatabaseProperty } from "../dao/databaseProperty";
import { PropertyDescriptor } from "../dao/propertyDescriptor";

export interface DateProperty extends DatabaseProperty<Date> {

    date() : Date | undefined;

    setDate( value : Date | undefined ) : void;

    setDefaultDate( defaultDate : Date ) : void;
}

export interface DatePropertyDescriptor extends PropertyDescriptor<DateProperty>  { 

}

