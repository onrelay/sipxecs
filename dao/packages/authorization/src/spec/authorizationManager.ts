import { AuthorizationType } from "../defs/authorizationType";
import { Authorization } from "../types/authorization";

export interface AuthorizationManager {

    uri() : string;

    authorizes( authorizationType : AuthorizationType, namespace : string, key? : string ) : boolean;

    readonly authorization : Authorization
}
