import { DatabaseProperty } from "../dao/databaseProperty";
import { PropertyDescriptor } from "../dao/propertyDescriptor";

export interface BasicProperty<BasicType extends string | number | boolean> 
    extends DatabaseProperty<BasicType> {

    value( ignoreDefault? : boolean ) : BasicType | undefined;

    setValue( value : BasicType | undefined ) : void;

    defaultValue() : BasicType | undefined;

    setDefaultValue( defaultValue : BasicType | undefined ): void; 
}


export interface BasicPropertyDescriptor<BasicType extends string | number | boolean> 
    extends PropertyDescriptor<BasicProperty<BasicType>>  { 

}



