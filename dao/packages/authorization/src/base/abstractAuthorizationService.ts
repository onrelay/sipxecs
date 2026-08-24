import { AbstractService, Context, Logger, LoggerFactory, Monitor, Observation } from "@dao/common";
import { AuthorizationType } from "../defs/authorizationType";
import { NamespaceSeparator } from "../impl/genericAuthorizationManager";
import { AuthorizationManager } from "../spec/authorizationManager";
import { AuthorizationService, AuthorizationServiceName } from "../spec/authorizationService";

export let log : Logger;

export abstract class AbstractAuthorizationService extends AbstractService implements AuthorizationService {

    constructor( context : Context ) {

        super( context );

        try {
            log = LoggerFactory.logger( this.name );

        } catch (error) {

            throw new Error( (error as any).message ); 
        }
    }


    async init() : Promise<void> {

        log.traceIn("init()" );

        try {

            this.isInitialized = true;

            log.traceOut("init()" );

        } catch (error) {
            log.warn("init()", "Error initializing authorization service", error);

            throw new Error( (error as any).message );
        }
    }

    // Supersets are namespace prefixes, so walk up the chain instead of scanning every authorization.
    isAuthorized( authorizationType : AuthorizationType, namespace : string, key? : string ) : boolean {

        const namespaceElements = namespace.split( NamespaceSeparator );

        for( let i = namespaceElements.length; i > 0; i-- ) {

            const authorizations = this.authorizationManagers.get(
                namespaceElements.slice( 0, i ).join( NamespaceSeparator ) );

            if( authorizations?.some( authorization =>
                    authorization.authorizes( authorizationType, namespace, key ) ) ) {

                return true;
            }
        }

        return false;
    }

    updateAuthorizationManagers( authorizationManagers? : AuthorizationManager[] ) : void {

        log.traceIn("updateAuthorizations()" );

        try {
            this.authorizationManagers.clear();

            if( authorizationManagers == null ) {

                log.traceOut("updateAuthorizations()", "no authorizations" );
                return;
            }

            for( const authorizationManager of authorizationManagers ) {

                const namespaceAuthorizations = this.authorizationManagers.get( authorizationManager.authorization.namespace );

                if( namespaceAuthorizations != null ) {

                    namespaceAuthorizations.push( authorizationManager );
                }
                else {
                    this.authorizationManagers.set( authorizationManager.authorization.namespace, [ authorizationManager ] );
                }
            }

            log.traceOut("updateAuthorizations()", this.authorizationManagers.size );

        } catch (error) {
            log.warn("updateAuthorizations()", "Error updating authorizations", error);

            throw new Error( (error as any).message );
        }
    }

    clearAuthorizationManagers() : void {

        this.authorizationManagers.clear();
    }

    protected async monitor( newMonitor : Monitor ): Promise<void> {}

    protected async release( observationFilter?: Observation[], objectIdsFilter?: string[] ): Promise<void> {}

    readonly name = AuthorizationServiceName;

    readonly authorizationManagers = new Map<string,AuthorizationManager[]>();

    isInitialized = false;
}
