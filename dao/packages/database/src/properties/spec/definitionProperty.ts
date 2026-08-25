import { DatabaseProperty } from "../../core/spec/databaseProperty";
import { PropertyDescriptor } from "../../core/spec/propertyDescriptor";

export interface DefinitionProperty<Definition extends string> extends DatabaseProperty<Definition> {

    readonly definition : string;

    readonly definitions : {}

    value( ignoreDefault? : boolean ) : Definition | undefined;

    setValue( value : Definition | undefined ) : void;

    defaultValue() : Definition | undefined;

    setDefaultValue( defaultValue : Definition | undefined ): void;

    requiresSelect() : boolean;

    select( params?: { filterValue?: boolean } ) : Promise<Map<string,Definition> | undefined>;

}

export interface DefinitionPropertyDescriptor<Definition extends string> 
    extends PropertyDescriptor<DefinitionProperty<Definition>>  { 

}

