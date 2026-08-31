import { Target, Configuration } from "@dao/common";


export interface ConfigurationManager extends Configuration {

    parse( configData : object, key? : string, language? : string ) : any;

    load( configName : string, configData : object, language? : string ) : void;

    readonly target : Target;
}

