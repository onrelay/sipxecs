import { ApplicationConfigurationName } from "@dao/common";
import { Environment, LoggingConfigurationName, Platform, Target, Targets } from "@dao/common";
import { AbstractConfigurationManager } from "@dao/configuration";
import { log, DatabasesConfigurationName } from "@dao/database";
import { SecurityConfigurationName } from "@dao/security";

import applicationConfiguration from "../data/config/application.json";
import databasesConfiguration from "../data/config/databases/databases.json";
import securityConfiguration from "../data/config/security.json";

import restSipxConfiguration from "../data/config/databases/rest.sipx.json";
import mongoEntityConfiguration from "../data/config/databases/mongo.entity.json";

export class SipxConfigurationManager extends AbstractConfigurationManager {

    constructor( target: Target ) {

        super( target ); 

        try {
            super.load( ApplicationConfigurationName, applicationConfiguration );
            super.load( SecurityConfigurationName, securityConfiguration );

            super.load( DatabasesConfigurationName, databasesConfiguration );
            super.load( "rest.sipx", restSipxConfiguration );
            super.load( "mongo.entity", mongoEntityConfiguration );


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