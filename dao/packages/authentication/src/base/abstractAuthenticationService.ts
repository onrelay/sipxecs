import { AbstractService, Context, Environments, Logger, LoggerFactory, Monitor, Observation } from "@dao/common";
import { AuthorizationManager, authorizationServiceFactory, GenericAuthorizationManager } from "@dao/authorization";
import { AuthenticationService, AuthenticationServiceName } from "../spec/authenticationService";
import { AuthenticatedEntity } from "../types/authenticatedEntity";

export let log : Logger;

export abstract class AbstractAuthenticationService extends AbstractService implements AuthenticationService {

    constructor( context : Context ) {

        super( context );

        try {

        } catch (error) {

            throw new Error( (error as any).message ); 
        }
    }


    async init() : Promise<void> {

        try {
            log = LoggerFactory.logger( this.name );

            this.isInitialized = true;

            log.traceInOut("init()" );

        } catch (error) {
            log.warn("init()", "Error initializing authentication service", error);

            throw new Error( (error as any).message );
        }
    }

    authenticatedEntity() : AuthenticatedEntity | undefined {
        return this._authenticatedEntity;
    }


    // The authorization service holds the authorizations of the currently authenticated entity.
    protected updateAuthorizations() : void {

        const authorizationManagers = [] as AuthorizationManager[];

        if( this._authenticatedEntity?.authorizations != null ) {

            for( const authorization of this._authenticatedEntity.authorizations ) {

                authorizationManagers.push( new GenericAuthorizationManager( authorization ));
            }
        }

        authorizationServiceFactory?.get().updateAuthorizationManagers( 
            authorizationManagers );
    }

    isInitialized = false;

    readonly name = AuthenticationServiceName;

    private _authenticatedEntity? : AuthenticatedEntity;

}
