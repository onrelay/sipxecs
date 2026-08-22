import { AbstractLogger, AbstractService, Logger, LoggerFactory } from "@sipxdao/common";
import { SipxService } from "./sipxService";
import { ConfigurationManager } from "@sipxdao/configuration";

export let log : Logger;

export abstract class AbstractSipxService extends AbstractService implements SipxService {

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

    readonly name = "server";

    readonly configurationManager: ConfigurationManager;

}


