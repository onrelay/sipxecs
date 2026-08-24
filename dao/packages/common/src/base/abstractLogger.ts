import { Configuration } from "../types/configuration";
import { DefaultLogLevelConfigurationKey, Logger, LoggingConfigurationName, LogLevelConfigurationKey } from "../spec/logger";
import { LogLevel, LogLevels } from "../defs/logLevel";

const initLogLevel = LogLevels.Warn;

export abstract class AbstractLogger implements Logger {

    static init( configuration? : Configuration ) {

        AbstractLogger._configuration = configuration;
    }

    constructor( name : string, logLevel? : LogLevel ) {

        this.name = name;
        
        this._logLevel = logLevel;
    }

    logLevel() : LogLevel {

        if( this._logLevel != null ) {
            return this._logLevel;
        }


        if( AbstractLogger._configuration != null ) {

            const logLevel = AbstractLogger._configuration.config( 
                LoggingConfigurationName, LogLevelConfigurationKey + "." + this.name )
    
            if( logLevel != null ) {            
                this._logLevel = logLevel;
                return logLevel;
            }

            const defaultLogLevel = AbstractLogger._configuration.config( 
                LoggingConfigurationName, DefaultLogLevelConfigurationKey  )


            if( defaultLogLevel != null ) {            
                this._logLevel = defaultLogLevel;
                return defaultLogLevel;
            }
            
            
            return defaultLogLevel;
        }

        return initLogLevel; 
    }

    errorEnabled() : boolean {
        return [LogLevels.Error,LogLevels.Warn,LogLevels.Info,LogLevels.Debug,LogLevels.Trace].includes( this.logLevel() );
    }

    warnEnabled() : boolean {
        return ( [LogLevels.Warn,LogLevels.Info,LogLevels.Debug,LogLevels.Trace] as LogLevel[] ).includes( this.logLevel() );
    }

    infoEnabled() : boolean {
        return ( [LogLevels.Info,LogLevels.Debug,LogLevels.Trace] as LogLevel[] ).includes( this.logLevel() );
    }

    debugEnabled() : boolean {
        return ( [LogLevels.Debug,LogLevels.Trace] as LogLevel[] ).includes( this.logLevel() );
    }

    traceEnabled() : boolean {
        return ( [LogLevels.Trace] as LogLevel[] ).includes( this.logLevel() );
    }


    abstract error( message?: any, ...optionalParams: any[] ) : void;

    abstract warn( message?: any, ...optionalParams: any[] ) : void;

    abstract info( message?: any, ...optionalParams: any[] ) : void;

    abstract debug( message?: any, ...optionalParams: any[] ) : void;

    abstract traceIn( message?: any, ...optionalParams: any[] ) : void;

    abstract traceOut( message?: any, ...optionalParams: any[] ) : void;

    abstract traceInOut( message?: any, ...optionalParams: any[] ) : void;

    protected static ignore( message?: any, ...optionalParams: any[] ) : void {}

    protected timestamp = () : string => {
        const date = new Date();
        return date.toISOString();
    }

    readonly name : string;

    protected _logLevel? : LogLevel;

    protected static _configuration? : Configuration;

}