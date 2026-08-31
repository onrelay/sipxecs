import { ApplicationConfigurationName, Environment, LoggingConfigurationName, Platform, Target, Targets } from "@dao/common";
import { AbstractConfigurationManager } from "@dao/configuration";
import { log, CollectionsConfigurationName, DatabaseConfigurationName } from "@dao/database";
import { SecurityConfigurationName } from "@dao/security";

import applicationConfiguration from "../data/config/application.json";
import databaseConfiguration from "../data/config/database.json";
import loggingConfiguration from "../data/config/logging.json";
import securityConfiguration from "../data/config/security.json";
import collectionsConfiguration from "../data/config/collections.json";

export class SipxConfigurationManager extends AbstractConfigurationManager {

    constructor( target: Target ) {

        super( target ); 

        try {
            super.load( ApplicationConfigurationName, applicationConfiguration );
            super.load( DatabaseConfigurationName, databaseConfiguration );
            super.load( LoggingConfigurationName, loggingConfiguration );
            super.load( SecurityConfigurationName, securityConfiguration );
            super.load( CollectionsConfigurationName, collectionsConfiguration );

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