import { DatabaseProperty } from "../../core/spec/databaseProperty";
import { OptionsSource } from "../../core/spec/optionsSource";

export interface TextsProperty extends DatabaseProperty<string[]>, OptionsSource {

    values() : string[] | undefined,

    setValues( values : string[] | undefined ) : void,

    clearValues() : void

    addValue( value : string ) : void,

    removeValue( value : string ) : boolean,

    requiresSelect() : boolean,

    options() : Promise<Map<string,string>  | undefined>, 

    select( params? : { filterValues? : boolean } ) : Promise<Map<string,string> | undefined>,

    minEntries? : number;

    maxEntries? : number;
}

