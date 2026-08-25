import { DatabaseProperty } from "../../core/spec/databaseProperty";
import { OptionsSource } from "../../core/spec/optionsSource";
import { PropertyDescriptor } from "../../core/spec/propertyDescriptor";

export interface DefinitionsProperty<Definition extends string> extends DatabaseProperty<Definition[]>, OptionsSource {

    readonly definition : string,

    readonly definitions : {},

    values( ignoreDefault? : boolean ) : Definition[] | undefined;

    setValues( values : Definition[] | undefined  ) : void;

    defaultValue() : Definition[] | undefined;

    setDefaultValue( defaultValues : Definition[] | undefined ): void;

    clearValues() : void

    addValue( value : Definition ) : void,

    removeValue( value : Definition ) : boolean,

    requiresSelect() : boolean,

    options() : Promise<Map<string,Definition>  | undefined>,

    select( params?: { filterValues?: boolean } ) : Promise<Map<string,Definition> | undefined>,

    minEntries? : number;

    maxEntries? : number;
}

export interface DefinitionsPropertyDescriptor<Definition extends string> 
    extends PropertyDescriptor<DefinitionsProperty<Definition>>  { 

}



