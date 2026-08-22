import { log } from "./abstractSecurityService";
import { KeyFormats } from "./keyFormat";
import { KeyManager } from "./keyManager";
import { KeyVault } from "./keyVault";
import { SymmetricKey } from "./symmetricKey";

export abstract class AbstractKeyManager implements KeyManager {

    abstract symmetricKey( id: string ) : Promise<SymmetricKey | undefined>;

    abstract createSymmetricKey( id: string, value? : string, labels?: { [k: string]: string } ) : Promise<SymmetricKey | undefined>;

    abstract enableSymmetricKey( id: string, versionNumber? : string ) : Promise<void>;

    abstract disableSymmetricKey( id: string, versionNumber? : string ) : Promise<void>;

    abstract destroySymmetricKey( id: string, versionNumber? : string ) : Promise<void>;

    abstract readonly keyVault : KeyVault;

    newValue( keyFormat : KeyFormat ) : string { 

        //log.traceIn( "newValue()")
        try {
            let keyLength : number; 

            switch( keyFormat )
            {
                case KeyFormats.AES256:
                    keyLength = 256 / 8;
                    break;

                default:
                    throw new Error( "Unrecognized key format: " + keyFormat );
            }

            let value = "";

            while(value.length < keyLength) {

                value += Math.random().toString(36).substring(2);
            } 
            value = value.substring(0, keyLength);  

            //log.traceOut( "newValue()", {payload});
            return value;
                
        } catch( error ) {

            log.warn( "newValue()", error );

            throw new Error( (error as any).message ); 
        }
    }
}
