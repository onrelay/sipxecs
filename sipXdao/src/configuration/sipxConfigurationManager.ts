import { Environment, Platform, Target } from "@dao/common";
import { AbstractConfigurationManager } from "@dao/configuration";

import applicationConfiguration from "../data/config/application.json"
import databaseConfiguration from "../data/config/database.json"
import loggingConfiguration from "../data/config/logging.json"
import securityConfiguration from "../data/config/security.json"

import { log } from "../abstractSipxService";

export class SipxConfigurationManager extends AbstractConfigurationManager  {

    constructor() {

        super(); 

        try {

            super.load( "application", applicationConfiguration );
            super.load( "database", databaseConfiguration );
            super.load( "logging", loggingConfiguration );
            super.load( "security", securityConfiguration );

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