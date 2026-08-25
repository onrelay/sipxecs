import { ConsoleLogger } from "./consoleLogger";
import { Logger } from "./logger";
import { LogLevel } from "./logLevel";


export class LoggerFactory  {

    static logger( name : string, logLevel? : LogLevel ) : Logger {

        if( LoggerFactory._loggers == null ) {
            throw new Error( "Not ready" );
        }

        let logger = LoggerFactory._loggers.get( name );
        if( logger == null ) {

           logger = new ConsoleLogger( name, logLevel ); 

            LoggerFactory._loggers.set( name, logger );
        }
        return logger;
    }

    private static _loggers : Map<string,Logger> = new Map<string,Logger>();
}