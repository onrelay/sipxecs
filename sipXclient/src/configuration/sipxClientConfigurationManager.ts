import { Target, Targets } from "@dao/common";
import { log } from "@dao/common/build/application/application";
import { SipxConfigurationManager } from "@sipxdao";

export class SipxClientConfigurationManager extends SipxConfigurationManager {

    constructor( target: Target ) {

        super( target ); 

        try {

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