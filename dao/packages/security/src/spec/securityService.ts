import { Service } from "@dao/common";
import { KeyManager } from "./keyManager";
import { SymmetricCipher } from "./symmetricCipher";
import { SymmetricKey } from "../types/symmetricKey";

export const SecurityServiceName = "securityService";
export const SecurityConfigurationName = "security";

export interface SecurityService extends Service {

    init() : Promise<void>;

    clearCurrentKeys() : void;

    updateCurrentKeys( authId : string, defaultKey : SymmetricKey | null, key : SymmetricKey | null) : Promise<void>;
    
    readonly keyManager? : KeyManager;
    
    readonly symmetricCipher : SymmetricCipher;
}

