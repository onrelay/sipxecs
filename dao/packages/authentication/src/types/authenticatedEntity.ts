import { Authorization } from "@dao/authorization";
import { AuthenticatedEntityType } from "../defs/authenticatedEntityType";
import { AuthenticationMethod } from "../defs/authenticationMethod";
import { AuthenticationClaim } from "./authenticationClaim";

export type AuthenticatedEntity = { 

    authorizations: Authorization[];  

    claims: Map<string,AuthenticationClaim>; 

    authenticationMethod: AuthenticationMethod;

    authenticatedEntityType : AuthenticatedEntityType;

    authenticatedEntityCollectionName : string;

    authId : string;
    
};
