import { LogLevel } from "./logLevel";

export interface Logger {

    readonly name : string,
    
    logLevel() : LogLevel,

    errorEnabled() : boolean,

    error( message?: any, ...optionalParams: any[] ) : void,

    warnEnabled() : boolean,

    warn( message?: any, ...optionalParams: any[] ) : void,

    infoEnabled() : boolean,

    info( message?: any, ...optionalParams: any[] ) : void,

    debugEnabled() : boolean,

    debug( message?: any, ...optionalParams: any[] ) : void,

    traceEnabled() : boolean,

    traceIn( message?: any, ...optionalParams: any[] ) : void,

    traceOut( message?: any, ...optionalParams: any[] ) : void,

    traceInOut( message?: any, ...optionalParams: any[] ) : void,

}