import { Service, Configuration, Language } from "@dao/common";
import { ConfigurationManager } from "./configurationManager";


export interface ConfigurationService extends Service, Configuration {

    parse( configData : object, key? : string, language? : Language ) : any;

    load( configName : string, configData : any, language? : Language ) : void;

    readonly configurationManager : ConfigurationManager
}

