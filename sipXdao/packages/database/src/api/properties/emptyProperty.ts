import { DatabaseProperty } from "../dao/databaseProperty";
import { PropertyDescriptor } from "../dao/propertyDescriptor";

export interface EmptyProperty extends DatabaseProperty<undefined> {

    value() : undefined,

    setValue( value : undefined ) : void
}


export interface EmptyPropertyDescriptor extends PropertyDescriptor<EmptyProperty>  { 

}
