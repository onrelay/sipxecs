import { Context, Environments, log, Platforms, Target, Targets } from "@dao/common";
import { authenticationServiceFactory, AuthenticationServiceFactory } from "@dao/authentication";
import { configurationServiceFactory, ConfigurationServiceFactory } from "@dao/configuration";
import { authorizationServiceFactory, AuthorizationServiceFactory, GenericAuthorizationService } from "@dao/authorization";
import { GenericDatabaseFactory, GenericDatabaseService, DatabasePlatforms, DatabaseServiceFactory, databaseServiceFactory } from "@dao/database";
import { SipxApplicationName, AbstractSipxApplication, SipxRestDatabaseManager, sipxRestDatabaseName, SipxTranslator } from "@sipxdao";
import { SipxClientAuthenticationService } from "../authentication/sipxClientAuthenticationService";
import { SipxClientConfigurationManager } from "../configuration/sipxClientConfigurationManager";

const target = ( import.meta.env.VITE_TARGET ?? Targets.Production ) as Target;

export const serverUrl = window.location.origin;

export class SipxClientApplication extends AbstractSipxApplication {

    constructor() {

        super( SipxApplicationName, {

            environment: Environments.Client,

            platform: Platforms.Linux,

            target: target,

            configuration: new SipxClientConfigurationManager( target ),

            translator: new SipxTranslator()

        } as Context );

        ConfigurationServiceFactory.create( this.context );

        AuthorizationServiceFactory.create( 
            new GenericAuthorizationService( this.context ) );

        DatabaseServiceFactory.create( 
            new GenericDatabaseService( this.context )
        );

        AuthenticationServiceFactory.create( 
            new SipxClientAuthenticationService( this.context ) );

        this.context.translator.load();

        this.init();
    }

    async init(): Promise<void> {

        try {
            await super.init();

            await configurationServiceFactory!.get().init();

            await authorizationServiceFactory!.get().init();

            await databaseServiceFactory!.get().init();

            await databaseServiceFactory!.get().addDatabaseFactory(
                new GenericDatabaseFactory(
                    DatabasePlatforms.Rest,
                    sipxRestDatabaseName,
                    new SipxRestDatabaseManager( {
                        baseUrl: serverUrl
                    }))
            )

            await authenticationServiceFactory!.get().init();

            log.traceInOut( "init()");

        } catch( error ) {

            log.warn( "Error initializing sipx client application", error );
            
            throw new Error( "Error initializing sipx client application" ); 
        }

        this.isInitialized = true;
    }

    isInitialized = false;
}
