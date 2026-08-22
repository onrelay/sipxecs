import { Environment, Platform, Target } from "@sipxdao/common";


export interface ConfigurationManager {

    parse( configData : object, key? : string, language? : string ) : any;

    cached( configName : string, key? : string, language? : string ) : any;

    cache( configName : string, configData : object ) : void;

    readonly application : string;

    readonly environment : Environment;

    readonly platform : Platform;

    readonly target : Target;


}

