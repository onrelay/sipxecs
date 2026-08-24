import { Context } from "@dao/common";
import { AbstractService, Environment, Environments, Logger, LoggerFactory } from "@dao/common";
import { KeyManager } from "../spec/keyManager";
import { SecurityConfigurationName, SecurityService, SecurityServiceName } from "../spec/securityService";
import { SymmetricCipher } from "../spec/symmetricCipher";
import { SymmetricKey } from "../types/symmetricKey";

export let log : Logger;

export abstract class AbstractSecurityService extends AbstractService implements SecurityService {

    constructor( context : Context ) {

        super( context );

        try {

        } catch (error) {

            throw new Error( (error as any).message ); 
        }
    }

    async init() : Promise<void> {

        try {

            log = LoggerFactory.logger( this.name ); 

            this.isInitialized = true;

            log.traceInOut("init()" );

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


    async updateCurrentKeys( authId : string, defaultKey : SymmetricKey | null, key : SymmetricKey | null) : Promise<void> {
        
        try {
            //log.traceIn( "updateCurrentKeys()" );

            if( this.symmetricCipher.defaultKey() === undefined ) {

                let useDefaultKey;

                if( defaultKey != null ) {

                    useDefaultKey = defaultKey;
                }
                else {

                    const defaultKeyId = this.context.configuration.config(
                        SecurityConfigurationName, "defaultKeyId")!;
    
                    useDefaultKey = await this.keyManager?.symmetricKey( defaultKeyId );
                }

                this.symmetricCipher.setDefaultKey( useDefaultKey == null ? null : useDefaultKey );

                log.debug( "updateCurrentKeys()", "updated default", useDefaultKey == null ? null : useDefaultKey.id) ;
            }

            //log.debug( "updateCurrentKeys()", {organizationId} );

            if( authId != null ) {

                const currentKey = this.symmetricCipher.key();

                //log.debug( "updateCurrentKeys()", "key", key?.id );

                if( currentKey === undefined || (currentKey != null && currentKey.id !== authId ) ) { 

                    let useKey;

                    if( key != null) {

                        useKey = key;
                    }
                    else {
                        useKey = await this.keyManager?.symmetricKey( authId );
                    }

                    this.symmetricCipher.setKey( useKey == null ? null : useKey );

                    log.debug( "updateKeys()", "updated", useKey == null ? null : useKey.id ) ;
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

    readonly keyManager? : KeyManager;

    abstract readonly symmetricCipher : SymmetricCipher;

}

