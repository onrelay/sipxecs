import { AbstractService, application, Environments, Logger, LoggerFactory } from "@dao/common";
import { ConfigurationManager, configurationServiceFactory } from "@dao/configuration";
import { authenticationServiceFactory } from "@dao/authentication";
import { KeyManager } from "../spec/keyManager";
import { SecurityConfigurationName, SecurityService, SecurityServiceName } from "../spec/securityService";
import { SymmetricCipher } from "../spec/symmetricCipher";

export let log : Logger;

export abstract class AbstractSecurityService extends AbstractService implements SecurityService {

    constructor( configurationManager : ConfigurationManager) {

        super();

        //log.traceInOut("constructor()", target );

        try {
            log = LoggerFactory.logger( this.name ); 

            this.configurationManager = configurationManager;

            log.info("constructor()", "Application logger created", {log}); 
        
        } catch (error) {
            log.warn("constructor()", "Error constructing application service", error);

            throw new Error( (error as any).message ); 
        }
    }


    async init() : Promise<void> {

        log.traceIn("init()" );

        try {

            log.traceOut("init()" );

        } catch (error) {
            log.warn("init()", "Error initializing application service", error);

            throw new Error( (error as any).message );
        }
    }

    clearCurrentKeys() : void {

        //log.traceIn( "clearCurrentKeys()" );

        this.symmetricCipher.setDefaultKey( undefined );

        this.symmetricCipher.setKey( undefined );

        //log.traceOut( "clearCurrentKeys()" );

    }


    async updateCurrentKeys() : Promise<void> {
        
        try {
            //log.traceIn( "updateCurrentKeys()" );

            const authenticatedEntity = authenticationServiceFactory?.get().authenticatedEntity;

            if( this.symmetricCipher.defaultKey() === undefined ) {

                let defaultKey;

                if( application!.environment === Environments.Client ) {
                    defaultKey = authenticatedEntity?.claims.get("defaultKey");
                }
                else {
                    const defaultKeyId = configurationServiceFactory!.get().config(
                        SecurityConfigurationName, "defaultKeyId")!;
    
                    defaultKey = await this.keyManager?.symmetricKey( defaultKeyId );
                }

                this.symmetricCipher.setDefaultKey( defaultKey == null ? null : defaultKey );

                log.debug( "updateCurrentKeys()", "updated default", defaultKey == null ? null : defaultKey.id) ;
            }

            const authId = authenticatedEntity?.authId;

            //log.debug( "updateCurrentKeys()", {organizationId} );

            if( authId != null ) {

                const key = this.symmetricCipher.key();

                //log.debug( "updateCurrentKeys()", "key", key?.id );

                if( key === undefined || (key != null && key.id !== authId ) ) { 

                    let key;

                    if( application!.environment === Environments.Client ) {

                        key = authenticatedEntity?.claims.get("key");
                    }
                    else {
                        key = await this.keyManager?.symmetricKey( authId );
                    }

                    this.symmetricCipher.setKey( key == null ? null : key );

                    log.debug( "updateKeys()", "updated", key == null ? null : key.id ) ;
                }
            }

            //log.traceOut( "updateCurrentKeys()" ) ; 

        } catch (error) {
            log.warn( "updateKeys()", "Failed to update cipher keys", error ) ;

            this.symmetricCipher.setDefaultKey( null );

            this.symmetricCipher.setKey( null );

            throw new Error( (error as any).message );
        }
    }

    readonly name = SecurityServiceName;

    readonly configurationManager: ConfigurationManager;

    readonly keyManager? : KeyManager;

    abstract readonly symmetricCipher : SymmetricCipher;

}

