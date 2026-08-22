import { AbstractService, Logger, LoggerFactory } from "@sipxdao/common";
import { SecurityService } from "./securityService";
import { ConfigurationManager, configurationServiceFactory } from "@sipxdao/configuration";
import { KeyManager } from "./keyManager";
import { SymmetricCipherImpl } from "../internal/symmetricCipherImpl";
import { AuthenticatedEntity, authenticationServiceFactory } from "@sipxdao/authentication";
import { Environments } from "@sipxdao/common/src/service/environment";

export let log : Logger;

export abstract class AbstractSecurityService extends AbstractService implements SecurityService {

    constructor( configurationManager : ConfigurationManager) {

        super( {
            application: configurationManager.application,
            environment: configurationManager.environment,
            platform: configurationManager.platform,
            target: configurationManager.target
        } );

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

                if( this.configurationManager.environment === Environments.Client ) {
                    defaultKey = authenticatedEntity?.claims.get("defaultKey");
                }
                else {
                    const defaultKeyId = configurationServiceFactory!.get().cached(
                        "security", "defaultKeyId")!;
    
                    defaultKey = await this.keyManager?.symmetricKey( defaultKeyId );
                }

                this.symmetricCipher.setDefaultKey( defaultKey == null ? null : defaultKey );

                log.debug( "updateCurrentKeys()", "updated default", defaultKey == null ? null : defaultKey.id) ;
            }

            const authenticationId = authenticatedEntity?.authenticationId;

            //log.debug( "updateCurrentKeys()", {organizationId} );

            if( authenticationId != null ) {

                const key = this.symmetricCipher.key();

                //log.debug( "updateCurrentKeys()", "key", key?.id );

                if( key === undefined || (key != null && key.id !== authenticationId ) ) { 

                    let key;

                    if( this.configurationManager.environment === Environments.Client ) {

                        key = authenticatedEntity?.claims.get("key");
                    }
                    else {
                        key = await this.keyManager?.symmetricKey( authenticationId );
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

    readonly name = "security";

    readonly configurationManager: ConfigurationManager;

    readonly keyManager? : KeyManager;

    readonly symmetricCipher = new SymmetricCipherImpl();

}


