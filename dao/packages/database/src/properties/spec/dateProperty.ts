import { DatabaseProperty } from "../../core/spec/databaseProperty";
import { PropertyDescriptor } from "../../core/spec/propertyDescriptor";

export interface DateProperty extends DatabaseProperty<Date> {

    date() : Date | undefined;

    setDate( value : Date | undefined ) : void;

    setDefaultDate( defaultDate : Date ) : void;
}

export interface DatePropertyDescriptor extends PropertyDescriptor<DateProperty>  { 

}

