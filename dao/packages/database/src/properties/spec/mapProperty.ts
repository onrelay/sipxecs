import { DatabaseProperty } from "../../core/spec/databaseProperty";

export interface MapProperty<Data extends Object> extends DatabaseProperty<Map<string,Data>> {

    value() : Map<string,Data> | undefined,

    setValue( value : Map<string,Data> | undefined ) : void,

    setEntry( key: string, entry: Data ) : void,

    removeEntry( key: string ) : boolean;

    minEntries? : number;

    maxEntries? : number; 
}

