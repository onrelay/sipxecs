import { AbstractService, Logger, LoggerFactory, Monitor, Observable, Observation, Observations } from "@dao/common";
import { DatabaseService, DatabaseServiceName } from "../spec/databaseService";
import { databaseServiceFactory} from "../impl/databaseServiceFactory";
import { DatabaseManager } from "../spec/databaseManager";
import { DatabaseFactory } from "../spec/databaseFactory";
import { DatabaseAccessor } from "../spec/databaseAccessor";
import { ConfigurationManager } from "@dao/configuration";
import { AuthenticatedEntity, authenticationServiceFactory, AuthenticationServiceFactory } from "@dao/authentication";
import { Entity } from "../../documents/spec/entity";

export let log : Logger;

export class AbstractDatabaseService extends AbstractService implements DatabaseService  {

    constructor( 
        configurationManager : ConfigurationManager,
        databaseManager : DatabaseManager, 
        databaseFactory : DatabaseFactory,
        databaseAccessor : DatabaseAccessor ) { 

        super();

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
                await this.entityWithAuthId(
                    authenticatedEntity.authenticatedEntityCollectionName,
                    authenticatedEntity.authId
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


    protected async entityWithAuthId( 
        entityCollectionName : string, 
        authId : string ) : Promise<Entity | undefined> {

        log.traceIn( "entityWithAuthId()", authId );

        const result = await this.databaseFactory.databaseManager.documentWithProperty( 
            this.databaseFactory.collectionGroupDatabaseFromCollectionName( entityCollectionName )!,
            "authId", authId ) as Entity;

        log.traceOut( "entityWithAuthId()", result != null ? result.uri() : undefined  );
        return result;
    }

    readonly name = DatabaseServiceName;

    isInitialized = false;

    readonly databaseFactory : DatabaseFactory;

    readonly databaseManager : DatabaseManager;

    readonly databaseAccessor : DatabaseAccessor;

    private _authenticatedEntity? : AuthenticatedEntity;

    private _authenticatedDatabaseEntity? : Entity;
}
