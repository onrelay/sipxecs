import { Service } from "@dao/common"
import { AuthorizationType } from "../defs/authorizationType";
import { AuthorizationManager } from "./authorizationManager";

export const AuthorizationServiceName = "authorization";

export interface AuthorizationService extends Service {

    init() : Promise<void>;

    isAuthorized( authorizationType : AuthorizationType, namespace : string, key? : string ) : boolean;

    updateAuthorizationManagers( authorizationManagers? : AuthorizationManager[] ) : void;

    clearAuthorizationManagers() : void;

    readonly authorizationManagers : Map<string,AuthorizationManager[]>;
}
