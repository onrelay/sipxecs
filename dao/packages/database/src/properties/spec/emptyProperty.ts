import { DatabaseProperty } from "../../core/spec/databaseProperty";
import { PropertyDescriptor } from "../../core/spec/propertyDescriptor";

export interface EmptyProperty extends DatabaseProperty<undefined> {

    value() : undefined,

    setValue( value : undefined ) : void
}


export interface EmptyPropertyDescriptor extends PropertyDescriptor<EmptyProperty>  { 

}
