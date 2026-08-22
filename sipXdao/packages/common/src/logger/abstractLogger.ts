import { Logger } from "./logger";
import { LogLevel } from "./logLevel";

type ConfigurationManagerLike = {
    cached( namespace: string, key: string ): any;
};

const initLogLevel = LogLevel.Warn as LogLevel;

export abstract class AbstractLogger implements Logger {

    static init( configurationManager? : ConfigurationManagerLike ) {

        AbstractLogger._configurationManager = configurationManager;
    }

    constructor( name : string, logLevel? : LogLevel ) {

        this.name = name;
        
        this._logLevel = logLevel;
    }

    logLevel() : LogLevel {

        if( this._logLevel != null ) {
            return this._logLevel;
        }


        if( AbstractLogger._configurationManager != null ) {

            const logLevel = AbstractLogger._configurationManager.cached( 
                "logging", "logLevel." + this.name )
    
            if( logLevel != null ) {            
                this._logLevel = logLevel;
                return logLevel;
            }

            const defaultLogLevel = AbstractLogger._configurationManager.cached( 
                "logging", "defaultLogLevel"  )


            if( defaultLogLevel != null ) {            
                this._logLevel = defaultLogLevel;
                return defaultLogLevel;
            }
            
            
            return defaultLogLevel;
        }

        return initLogLevel; 
    }

    errorEnabled() : boolean {
        return [LogLevel.Error,LogLevel.Warn,LogLevel.Info,LogLevel.Debug,LogLevel.Trace].includes( this.logLevel() );
    }

    warnEnabled() : boolean {
        return [LogLevel.Warn,LogLevel.Info,LogLevel.Debug,LogLevel.Trace].includes( this.logLevel() );
    }

    infoEnabled() : boolean {
        return [LogLevel.Info,LogLevel.Debug,LogLevel.Trace].includes( this.logLevel() );
    }

    debugEnabled() : boolean {
        return [LogLevel.Debug,LogLevel.Trace].includes( this.logLevel() );
    }

    traceEnabled() : boolean {
        return [LogLevel.Trace].includes( this.logLevel() );
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

    protected static _configurationManager? : ConfigurationManagerLike;

}