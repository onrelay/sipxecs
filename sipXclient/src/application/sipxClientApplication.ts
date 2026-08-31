import { Environment, Environments, Language, Languages, Logger, LoggerFactory, Platform, Platforms, Target, Targets, Translator } from "@dao/common";
import { log } from "@dao/common/build/application/application";
import { AbstractSipxApplication, SipxTranslator } from "@sipxdao";
import { SipxClientConfigurationManager } from "../configuration/sipxClientConfigurationManager";

export const SipxClientApplicationName = "sipxClient";

const target = ( import.meta.env.VITE_TARGET ?? Targets.Production ) as Target;

export class SipxClientApplication extends AbstractSipxApplication {

    constructor() {

        super( {
            name: SipxClientApplicationName,

            environment: Environments.Client as Environment,

            platform: Platforms.Linux as Platform,

            target: target,

            configuration: new SipxClientConfigurationManager( target )

        } );

        try {

            this.translator = new SipxTranslator();

        } catch( error ) {

            log.warn( "Error starting sipx client application", error );
            
            throw new Error( "Error constructing config" ); 
        }
    }

    readonly translator : Translator;
}
