
import { Context, Environments, Platforms, Target, Targets } from "@dao/common";
import { configurationServiceFactory, ConfigurationServiceFactory } from "@dao/configuration";
import { DatabasePlatforms, databaseServiceFactory, DatabaseServiceFactory, GenericDatabaseFactory, GenericDatabaseService, log } from "@dao/database";
import { SipxApplicationName, AbstractSipxApplication, SipxRestDatabaseManager, sipxRestDatabaseName, SipxTranslator } from "@sipxdao";
import { SipxServerConfigurationManager } from "../configuration/sipxServerConfigurationManager";

const target = ( process.env.TARGET ?? Targets.Production ) as Target;

const serverUrl = "localhost";

export class SipxServerApplication extends AbstractSipxApplication {

    constructor() {

        super( SipxApplicationName, {

            environment: Environments.Client,

            platform: Platforms.Linux,

            target: target,

            configuration: new SipxServerConfigurationManager( target ),

            translator: new SipxTranslator()

        } as Context );

        ConfigurationServiceFactory.create( this.context );

        DatabaseServiceFactory.create( 
            new GenericDatabaseService( this.context )
        );

        this.context.translator.load();

        this.init();
    }

    async init(): Promise<void> {

        try {
            await super.init();

            await configurationServiceFactory!.get().init();

            await databaseServiceFactory!.get().init();

            const sipxRestBaseUrl = "https://" + 
                this.context.configuration.config( "sipxRest", "host") + 
                ":" +
                this.context.configuration.config( "sipxRest", "port") + 
                this.context.configuration.config( "sipxRest", "path");

            await databaseServiceFactory!.get().addDatabaseFactory(
                new GenericDatabaseFactory(
                    DatabasePlatforms.Rest,
                    sipxRestDatabaseName,
                    new SipxRestDatabaseManager( {
                        baseUrl: sipxRestBaseUrl
                    }))
            )

            log.traceInOut( "init()");

        } catch( error ) {

            log.warn( "Error initializing sipx client application", error );
            
            throw new Error( "Error initializing sipx client application" ); 
        }

        this.isInitialized = true;
    }

    isInitialized = false;
}