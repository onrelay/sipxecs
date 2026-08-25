import { AbstractLogger } from "../logger/abstractLogger";
import { Logger } from "../logger/logger";
import { LoggerFactory } from "../logger/loggerFactory";
import { Environment } from "./environment";
import { Platform } from "./platform";
import { Target } from "./target";
import { Translator } from "../translator/translator";
import { Configuration } from "../types/configuration";

export let application: Application | undefined;

export function getApplication(): Application {
    if( application == null ) {
        throw new Error( "Application has not been initialized" );
    }

    return application;
}

export let log: Logger;

export abstract class Application {

    constructor( params: {
            name: string,
            environment: Environment,
            platform: Platform,
            target: Target,
            configuration: Configuration
            } ) {

        if( application != null ) {
            throw new Error( "Application already initialized" );
        }

        this.name = params.name;
        this.environment = params.environment;
        this.platform = params.platform;
        this.environment = params.environment;
        this.target = params.target;
        this.configuration = params.configuration;
        application = this;
    }

    async init(): Promise<void> {
        AbstractLogger.init( this.configuration );
        log = LoggerFactory.logger( this.name );
    }

    abstract readonly translator: Translator;

    readonly configuration: Configuration;

    readonly name : string;

    readonly environment : Environment;

    readonly platform : Platform;

    readonly target : Target;
}
