import { AuthenticatedEntityType } from "./authenticatedEntityType";
import { AuthenticationMethod } from "./authenticationMethod";

export type AuthenticatedEntity = { 

    scopes: string[];  

    claims: Map<string,any>; 

    authenticationMethod: AuthenticationMethod;

    authenticatedEntityType : AuthenticatedEntityType;

    authenticatedEntityCollectionName : string;

    authenticationId : string;
    
};