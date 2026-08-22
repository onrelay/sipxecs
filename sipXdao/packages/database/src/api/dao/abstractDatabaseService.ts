import { AbstractService, Logger, LoggerFactory, Monitor, Observable, Observation, Observations } from "@sipxdao/common";
import { DatabaseService } from "./databaseService";
import { databaseServiceFactory} from "./databaseServiceFactory";
import { DatabaseManager } from "./databaseManager";
import { DatabaseFactory } from "./databaseFactory";
import { DatabaseAccessor } from "../types/databaseAccessor";
import { ConfigurationManager } from "@sipxdao/configuration";
import { Entity } from "./entity";
import { AuthenticatedEntity, authenticationServiceFactory, AuthenticationServiceFactory } from "@sipxdao/authentication";

export let log : Logger;

export class AbstractDatabaseService extends AbstractService implements DatabaseService  {

    constructor( 
        configurationManager : ConfigurationManager,
        databaseManager : DatabaseManager, 
        databaseFactory : DatabaseFactory,
        databaseAccessor : DatabaseAccessor ) { 

        super( {
            application: configurationManager.application,
            environment: configurationManager.environment,
            platform: configurationManager.platform,
            target: configurationManager.target
        } );

        //log.traceIn( "constructor()");

        try {

            log = LoggerFactory.logger( this.name ); 
            
            this.databaseManager = databaseManager;

            this.databaseFactory = databaseFactory;

            this.databaseAccessor = databaseAccessor; 

            this.onNotifyAuthenticationUpdated = this.onNotifyAuthenticationUpdated.bind(this);

            //log.traceOut( "constructor()");

        } catch( error ) {
            log.warn( "constructor()", "Error initializing database service", error );

            throw new Error( (error as any).message );
        }
    }

    async init() : Promise<void> {

        log.traceInOut("init()" );

        try {

            if (authenticationServiceFactory?.get() != null) {
                
                await authenticationServiceFactory!.get()!.subscribe({
                    observer: this,
                    onNotify: this.onNotifyAuthenticationUpdated
                } as Monitor as Monitor
                );
            }

            this.isInitialized = true;

        } catch (error) {
            log.warn("init()", "Error initializing database service", error);

            throw new Error( (error as any).message );
        }
    }

    onNotifyAuthenticationUpdated = async (observable: Observable,
        observation: Observation,
        objectId: string | null | undefined,
        object: object | null | undefined): Promise<void> => {

        try {
            log.traceIn("onNotifyAuthenticationUpdated()", Observations[observation], {objectId});

            const authenticatedEntity = object as AuthenticatedEntity;

            const authenticatedDatabaseEntity = authenticatedEntity == null ? undefined :
                await this.entityWithAuthenticationId(
                    authenticatedEntity.authenticatedEntityCollectionName,
                    authenticatedEntity.authenticationId
                );

            if( this._authenticatedDatabaseEntity != null && 
                !databaseServiceFactory!.get().databaseFactory.equalUris( 
                    this._authenticatedDatabaseEntity.uri(),
                    authenticatedDatabaseEntity?.uri() ) ) {

                await this._authenticatedDatabaseEntity.unsubscribe( this );
            }

            if( authenticatedDatabaseEntity != null && 
                !databaseServiceFactory!.get().databaseFactory.equalUris( 
                    authenticatedDatabaseEntity.uri(),
                    this._authenticatedDatabaseEntity?.uri() ) ) {
                    
                await authenticatedDatabaseEntity.subscribe( {
                    observer: this,
                    onNotify: this.onNotifyAuthenticatedEntityUpdated
                    } as Monitor as Monitor 
                );
            }

            this._authenticatedEntity = authenticatedEntity;
            this._authenticatedDatabaseEntity = authenticatedDatabaseEntity;

            log.traceOut("onNotifyAuthenticationUpdated()");

        } catch (error) {
            log.warn("Error receiving authentication notification", error);

            log.traceOut("onNotifyAuthenticationUpdated()", error);
        }
    }

    onNotifyAuthenticatedEntityUpdated = async (observable: Observable,
        observation: Observation,
        objectId: string | null | undefined,
        object: object | null | undefined): Promise<void> => {

        try {
            log.traceIn("onNotifyAuthenticatedEntityUpdated()", Observations[observation], {objectId});

            const authenticatedDatabaseEntity = object != null ? object as Entity : undefined;

            if( this._authenticatedDatabaseEntity != null && authenticatedDatabaseEntity != null &&
                databaseServiceFactory!.get().databaseFactory.equalUris( 
                    this._authenticatedDatabaseEntity.uri(),
                    authenticatedDatabaseEntity?.uri() ) ) {

                await this._authenticatedDatabaseEntity.copyFrom( authenticatedDatabaseEntity );
            }

            log.traceOut("onNotifyAuthenticatedEntityUpdated()");

        } catch (error) {
            log.warn("Error receiving current user update", error);

            log.traceOut("onNotifyAuthenticatedUserUpdated()", error);
        }
    }

    authenticatedEntity() : AuthenticatedEntity | undefined {

        return this._authenticatedEntity;
    }

    authenticatedDatabaseEntity() : Entity | undefined {

        return this._authenticatedDatabaseEntity;
    }


    protected async entityWithAuthenticationId( 
        entityCollectionName : string, 
        authenticationId : string ) : Promise<Entity | undefined> {

        log.traceIn( "entityWithAuthenticationId()", authenticationId );

        const result = await this.databaseFactory.databaseManager.documentWithProperty( 
            this.databaseFactory.collectionGroupDatabaseFromCollectionName( entityCollectionName )!,
            "authenticationId", authenticationId ) as Entity;

        log.traceOut( "entityWithAuthenticationId()", result != null ? result.uri() : undefined  );
        return result;
    }

    readonly name = "database";

    isInitialized = false;

    readonly databaseFactory : DatabaseFactory;

    readonly databaseManager : DatabaseManager;

    readonly databaseAccessor : DatabaseAccessor;

    private _authenticatedEntity? : AuthenticatedEntity;

    private _authenticatedDatabaseEntity? : Entity;
}
