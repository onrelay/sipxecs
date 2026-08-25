import { SymmetricKey } from "../types/symmetricKey";


export interface SymmetricCipher {

    encrypt( data : any ) : string | undefined;

    decrypt( encryptedJson : string ) : any | undefined;

    isEncrypted( data : any ) : boolean;

    setDefaultKey( defaultKey : SymmetricKey | null | undefined ) : void;

    defaultKey() : SymmetricKey | null | undefined;

    setKey( key : SymmetricKey | null | undefined ) : void;

    key() : SymmetricKey | null | undefined;

}
