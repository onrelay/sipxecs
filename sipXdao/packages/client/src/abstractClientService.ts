import { AbstractLogger, AbstractService, Logger, LoggerFactory, Monitor, Observation } from "@sipxdao/common";
import { ConfigurationManager } from "@sipxdao/configuration";
import { ClientService } from "./clientService";
import { PersistentState } from "./persistentState/api/persistentState";

export let log : Logger;

export abstract class AbstractClientService extends AbstractService implements ClientService {

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

    readonly name = "client";

    abstract database() : any;

    readonly configurationManager: ConfigurationManager;

    abstract readonly persistentState: PersistentState;


}
