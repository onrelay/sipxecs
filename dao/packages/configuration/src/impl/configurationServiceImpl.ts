import { AbstractService, Logger, LoggerFactory, Monitor, Observation } from "@dao/common";
import { ConfigurationService } from "../spec/configurationService";
import { ConfigurationManager } from "../spec/configurationManager";

export let log : Logger;

export class ConfigurationServiceImpl extends AbstractService implements ConfigurationService {

    constructor( configurationManager : ConfigurationManager ) { 

        super();
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

    
    config( configName : string, key? : string, language? : string ) : any {  

        return this.configurationManager.config( configName, key, language );
    }

    load( configName : string, configData : object ) : void {

        this.configurationManager.load( configName, configData );
    }


    protected async monitor( newMonitor : Monitor ): Promise<void> {}

    protected async release(observationFilter?: Observation[], objectIdsFilter?: string[]): Promise<void> {}

    readonly name = "configuration";

    readonly configurationManager: ConfigurationManager;

    isInitialized = false;

}
