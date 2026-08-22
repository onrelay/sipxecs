import { ConfigurationManager } from "@sipxdao/configuration"

import { AbstractLogger, AbstractService, Translator } from "@sipxdao/common"
import { Logger } from "@sipxdao/common"
import { LoggerFactory } from "@sipxdao/common"
import { Monitor } from "@sipxdao/common"
import { Observation } from "@sipxdao/common"

import { ApplicationService } from "./applicationService";

export let log : Logger;

export abstract class AbstractApplicationService extends AbstractService implements ApplicationService {

    constructor( configurationManager : ConfigurationManager) {

        super( {
            application: configurationManager.application,
            environment: configurationManager.environment,
            platform: configurationManager.platform,
            target: configurationManager.target
        } );
        
        //log.traceInOut("constructor()", target );

        try {

            this.configurationManager = configurationManager;

            log.info("constructor()", "Application logger created", {log}); 
        
        } catch (error) {
            log.warn("constructor()", "Error constructing application service", error);

            throw new Error( (error as any).message ); 
        }
    }


    async init() : Promise<void> {

        log.traceIn("init()" );

        try {

            AbstractLogger.init( this.configurationManager ); 

            log = LoggerFactory.logger(this.name);  

            log.traceOut("init()" );

        } catch (error) {
            log.warn("init()", "Error initializing application service", error);

            throw new Error( (error as any).message );
        }
    }

    readonly name = "application";

    protected async monitor( newMonitor : Monitor ): Promise<void> {}

    protected async release(observationFilter?: Observation[], objectIdsFilter?: string[]): Promise<void> {}

    abstract readonly translator: Translator;

    readonly configurationManager : ConfigurationManager


}
