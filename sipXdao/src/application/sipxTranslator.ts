import { Language } from "@dao/common";
import { ConfigurationTranslator } from "@dao/configuration";

import applicationConfiguration from "../data/config/application.json";

export class SipxTranslator extends ConfigurationTranslator {

    constructor() {
        super( applicationConfiguration.defaultLanguage as Language );

        try {
            // super.loadTranslations( );

        } catch( error ) {

            //log.warn( "Error loading config cache", error );
            
            throw new Error( "Error constructing config" ); 
        }
    }
}