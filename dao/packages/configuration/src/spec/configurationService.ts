import { Service, Configuration } from "@dao/common";
import { ConfigurationManager } from "./configurationManager";


export interface ConfigurationService extends Service, Configuration {

    parse( configData : object, key? : string, language? : string ) : any;

    load( configName : string, configData : object ) : void;

    readonly configurationManager : ConfigurationManager
}

