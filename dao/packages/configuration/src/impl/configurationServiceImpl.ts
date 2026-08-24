import { AbstractService, Context, Logger, LoggerFactory, Monitor, Observation } from "@dao/common";
import { ConfigurationService, ConfigurationServiceName } from "../spec/configurationService";
import { ConfigurationManager } from "../spec/configurationManager";

export let log : Logger;

export class ConfigurationServiceImpl extends AbstractService implements ConfigurationService {

    constructor( context : Context ) { 

        super( context );
        //log.traceInOut("constructor()", target );

        try {

            this.configurationManager = context.configuration as ConfigurationManager;

        } catch (error) {
            //log.warn("constructor()", "Error constructing configuration service", error);

            throw new Error( (error as any).message );
        }
    }


    async init() : Promise<void> {

        try {
            log = LoggerFactory.logger(this.name);

            this.isInitialized = true;

            log.traceInOut("init()" );

        } catch (error) {
            log.warn("init()", "Error initializing configuration service", error);

            throw new Error( (error as any).message );
        }
    }

    parse( configData : object, key? : string, language? : string ) : any {

        return this.configurationManager.parse( configData, key, language );

    }

    
    config( configName : string, key? : string, language? : string ) : any {  

        return this.configurationManager.config( configName, key, language );
    }

    load( configName : string, configData : object, language? : string ) : void {

        this.configurationManager.load( configName, configData, language );
    }


    protected async monitor( newMonitor : Monitor ): Promise<void> {}

    protected async release(observationFilter?: Observation[], objectIdsFilter?: string[]): Promise<void> {}

    readonly name = ConfigurationServiceName;

    readonly configurationManager: ConfigurationManager;

    isInitialized = false;

}
