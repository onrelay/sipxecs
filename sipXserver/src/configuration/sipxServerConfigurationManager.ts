import { LoggingConfigurationName, Target, Targets } from "@dao/common";
import { log } from "@dao/common";
import { SipxConfigurationManager } from "@sipxdao";

import loggingConfiguration from "../data/config/logging.json";

export class SipxServerConfigurationManager extends SipxConfigurationManager {

    constructor( target: Target ) {

        super( target ); 

        try {
            super.load( LoggingConfigurationName, loggingConfiguration );

        } catch( error ) {

            //log.warn( "Error loading config cache", error );
            
            throw new Error( "Error constructing config" ); 
        }
    }

    async loadDocuments() : Promise<boolean> {
        log.traceIn( "loadDocuments()" ); 

        try {

            log.traceOut("loadDocuments()" );
            return true;
            
        } catch( error ) {

            log.warn( "Error loading documents", error );
            
            throw new Error( "Error loading documents" );
        }
    }
}