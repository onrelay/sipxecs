import { AbstractService, Logger, Monitor, Observation } from "@dao/common";
import { ConfigurationManager } from "@dao/configuration";
import { AuthenticationService } from "../spec/authenticationService";

export let log : Logger;

export abstract class AbstractAuthenticationService extends AbstractService implements AuthenticationService {

    constructor( configurationManager : ConfigurationManager) {

        super();
        //log.traceInOut("constructor()", target );

        try {

            log.info("constructor()", "Authentication logger created", {log}); 
        
        } catch (error) {
            log.warn("constructor()", "Error constructing application service", error);

            throw new Error( (error as any).message ); 
        }
    }


    async init() : Promise<void> {

        log.traceIn("init()" );

        try {

            log.traceOut("init()" );

        } catch (error) {
            log.warn("init()", "Error initializing application service", error);

            throw new Error( (error as any).message );
        }
    }

    readonly name = "authentication";

}
