import { AbstractService, Logger, LoggerFactory, Monitor, Observation } from "@sipxdao/common";
import { ConfigurationService } from "../api/configurationService";
import { ConfigurationManager } from "../api/configurationManager";

export let log : Logger;

export class ConfigurationServiceImpl extends AbstractService implements ConfigurationService {

    constructor( configurationManager : ConfigurationManager ) { 

        super( {
            application: configurationManager.application,
            environment: configurationManager.environment,
            platform: configurationManager.platform,
            target: configurationManager.target
        } );
        //log.traceInOut("constructor()", target );

        try {

            log = LoggerFactory.logger(this.name);

            this.configurationManager = configurationManager;

        } catch (error) {
            //log.warn("constructor()", "Error constructing configuration service", error);

            throw new Error( (error as any).message );
        }
    }


    async init() : Promise<void> {

        log.traceInOut("init()" );

        try {

            this.isInitialized = true;

        } catch (error) {
            log.warn("init()", "Error initializing configuration service", error);

            throw new Error( (error as any).message );
        }
    }

    parse( configData : object, key? : string, language? : string ) : any {

        return this.configurationManager.parse( configData, key, language );

    }

    
    cached( configName : string, key? : string, language? : string ) : any {  

        return this.configurationManager.cached( configName, key, language );
    }

    cache( configName : string, configData : object ) : void {

        this.configurationManager.cache( configName, configData );
    }


    protected async monitor( newMonitor : Monitor ): Promise<void> {}

    protected async release(observationFilter?: Observation[], objectIdsFilter?: string[]): Promise<void> {}

    readonly name = "configuration";

    readonly configurationManager: ConfigurationManager;

    isInitialized = false;

}
