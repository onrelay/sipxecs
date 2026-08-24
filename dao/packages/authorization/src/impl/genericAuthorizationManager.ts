import { AuthorizationType, authorizationTypeImplies } from "../defs/authorizationType";
import { AuthorizationManager } from "../spec/authorizationManager";
import { Authorization } from "../types/authorization";

export const NamespaceSeparator = ":";

export class GenericAuthorizationManager implements AuthorizationManager {

    constructor( authorization : Authorization ) {

        this.authorization = authorization;
    }

    uri() : string {

        return this.authorization.namespace + NamespaceSeparator + ( this.authorization.key ?? "" ) + 
            NamespaceSeparator + this.authorization.authorizationType;
    }

    authorizes( authorizationType : AuthorizationType, namespace : string, key? : string ) : boolean {

        return this.matchesNamespace( namespace ) &&
            authorizationTypeImplies( this.authorization.authorizationType, authorizationType ) &&
            this.matchesKey( key );
    }

    // A parent namespace also authorizes its nested namespaces, so "A" covers "A:B".
    protected matchesNamespace( namespace : string ) : boolean {

        return this.authorization.namespace === namespace ||
            namespace.startsWith( this.authorization.namespace + NamespaceSeparator );
    }

    // An undefined key grants the whole namespace; subclasses widen this, e.g. for hierarchical paths.
    protected matchesKey( key? : string ) : boolean {

        return this.authorization.key == null || this.authorization.key === key;
    }

    readonly authorization : Authorization
}
