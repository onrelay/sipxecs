import { Environment, Platform, Target } from "@sipxdao/common";
import { AbstractConfigurationManager } from "@sipxdao/configuration";

import applicationConfiguration from "../data/config/application.json"
import databaseConfiguration from "../data/config/database.json"
import loggingConfiguration from "../data/config/logging.json"
import securityConfiguration from "../data/config/security.json"
import { log } from "../abstractSipxService";

export class SipxConfigurationManager extends AbstractConfigurationManager  {

    constructor( application : string, environment : Environment, platform : Platform, target : Target ) {

        super( application, environment, platform, target ); 

        try {

            super.cache( "application", applicationConfiguration );
            super.cache( "database", databaseConfiguration );
            super.cache( "logging", loggingConfiguration );
            super.cache( "security", securityConfiguration );

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