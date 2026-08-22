import { Service } from "@sipxdao/common";
import { ConfigurationManager } from "./configurationManager";


export interface ConfigurationService extends Service {

    parse( configData : object, key? : string, language? : string ) : any;

    cached( configName : string, key? : string, language? : string ) : any;

    cache( configName : string, configData : object ) : void;

    readonly configurationManager : ConfigurationManager
}

