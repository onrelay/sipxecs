import { Service } from "@sipxdao/common";
import { ConfigurationManager } from "@sipxdao/configuration";
import { KeyManager } from "./keyManager";
import { SymmetricCipher } from "./symmetricCipher";

export interface SecurityService extends Service {

    init() : Promise<void>;

    clearCurrentKeys() : void;

    updateCurrentKeys() : Promise<void>;

    readonly configurationManager: ConfigurationManager;

    readonly keyManager? : KeyManager;
    
    readonly symmetricCipher : SymmetricCipher;
    

}

