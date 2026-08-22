import { KeyVault } from "./keyVault";
import { SymmetricKey } from "./symmetricKey";

export const InitialKeyVersion = 1;

export interface KeyManager {

    createSymmetricKey( id: string, value? : string, labels?: { [k: string]: string } ) : Promise<SymmetricKey | undefined>;

    symmetricKey( id: string ) : Promise<SymmetricKey | undefined>; 

    enableSymmetricKey( id: string, versionNumber? : string ) : Promise<void>;

    disableSymmetricKey( id: string, versionNumber? : string ) : Promise<void>;

    destroySymmetricKey( id: string, versionNumber? : string ) : Promise<void>;

    newValue( keyFormat : KeyFormat ) : string;

    readonly keyVault : KeyVault;
}

 