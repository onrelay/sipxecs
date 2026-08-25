import { AuthenticatedEntityType } from "../defs/authenticatedEntityType";
import { AuthenticationMethod } from "../defs/authenticationMethod";

export type AuthenticatedEntity = { 

    scopes: string[];  

    claims: Map<string,any>; 

    authenticationMethod: AuthenticationMethod;

    authenticatedEntityType : AuthenticatedEntityType;

    authenticatedEntityCollectionName : string;

    authenticationId : string;
    
};