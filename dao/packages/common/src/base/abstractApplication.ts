import { Context } from "../types/context";
import { Application } from "../spec/application";
import { Language } from "../defs/language";
import { Logger } from "../spec/logger";
import { AbstractLogger } from "./abstractLogger";
import { LoggerFactory } from "../impl/loggerFactory";

export const ApplicationConfigurationName = "application";

export let application: AbstractApplication | undefined;

export function getApplication(): AbstractApplication {
    if( application == null ) {
        throw new Error( "Application has not been initialized" );
    }

    return application;
}

export function translate( key : string, namespace? : string, language? : Language ) : string | undefined {

    return getApplication().context.translator.translate( key, { namespace, language } );
}

export let log: Logger;

export abstract class AbstractApplication implements Application {

    constructor( name: string, context : Context ) {

        if( application != null ) {
            throw new Error( "Application already initialized" );
        }
        
        application = this;

        this.name = name;

        this.context = context;
    }

    async init(): Promise<void> {

        AbstractLogger.init( this.context.configuration );
        
        log = LoggerFactory.logger( this.name );
    }

    abstract isInitialized : boolean;

    readonly name : string;

    readonly context : Context;

}