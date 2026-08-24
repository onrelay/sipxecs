import { AbstractService, Context, Logger, LoggerFactory, Monitor, Observable, Observation, Observations } from "@dao/common";
import { DatabaseService, DatabaseServiceName } from "../spec/databaseService";
import { DatabaseFactory } from "../spec/databaseFactory";
import { Entity } from "../../documents/spec/entity";
import { DatabasePlatform } from "../defs/databasePlatform";

export let log : Logger;

export class GenericDatabaseService extends AbstractService implements DatabaseService  {

    constructor( 
        context : Context,
        databaseFactories? : DatabaseFactory[] ) { 

        super( context );

        //log.traceIn( "constructor()");

        try {

            if( databaseFactories != null ) {
                for( const databaseFactory of databaseFactories ) {

                    this._databaseFactories.set( databaseFactory.databasePrefix(), databaseFactory );
                }
            }
            
            //log.traceOut( "constructor()");

        } catch( error ) {
            log.warn( "constructor()", "Error initializing database service", error );

            throw new Error( (error as any).message );
        }
    }

    async init() : Promise<void> {

        try {

            log = LoggerFactory.logger( this.name ); 

            this.isInitialized = true;

            log.traceInOut("init()" );

        } catch (error) {
            log.warn("init()", "Error initializing database service", error);

            throw new Error( (error as any).message );
        }
    }

    async addDatabaseFactory( databaseFactory : DatabaseFactory ) : Promise<void> {

        log.traceIn( "addDatabaseFactory()", databaseFactory.databasePlatform, databaseFactory.databaseName );

        try {
            this._databaseFactories.set( 
                this.databasePrefix( databaseFactory.databasePlatform, databaseFactory.databaseName ),
                databaseFactory );

            log.traceOut( "addDatabaseFactory()" );

        } catch( error ) {
            log.warn( "addDatabaseFactory()", "Error adding database factory", error );

            throw new Error( (error as any).message );
        }
    }

    async removeDatabaseFactory(  
        databasePlatform : DatabasePlatform, 
        databaseName : string ) : Promise<boolean> {

        log.traceIn( "removeDatabaseFactory()", databasePlatform, databaseName );

        try {

            const databaseFactory = this._databaseFactories.get( 
                this.databasePrefix( databasePlatform, databaseName ) );

            if( databaseFactory == null ) {
                log.traceOut( "removeDatabaseFactory()", "not found" );
                return false;
            }

            await databaseFactory.databaseManager.clearAll()

            log.traceOut( "removeDatabaseFactory()", true );
            return true;

        } catch( error ) {
            log.warn( "addDatabaseFactory()", "Error adding database factory", error );

            throw new Error( (error as any).message );
        }
    }

    databaseFactory( databasePlatform : DatabasePlatform, databaseName : string ) : DatabaseFactory | undefined {

        return this._databaseFactories.get( 
            this.databasePrefix( databasePlatform, databaseName ) );
    }

    databasePrefix( databasePlatform : DatabasePlatform, databaseName : string ) {
        return "/" + databasePlatform + "/" + databaseName;
    }

    async updateCurrentEntity( 
        entityCollectionName : string, 
        authId : string ) : Promise<Entity | undefined> {

        log.traceIn( "updateCurrentEntity()", authId );

        try {/*
            const databaseManager = 
                this.databaseFactory.databaseManagerFromCollectionName( 
                    entityCollectionName);

            const entity = await databaseManager.documentWithProperty( 
                this.databaseFactory.collectionGroupDatabaseFromCollectionName( entityCollectionName )!,
                "authId", authId ) as Entity;

            this.setCurrentEntity( entity );

            log.traceOut( "updateCurrentEntity()", entity != null ? entity.uri() : undefined  );
            return entity;
            */
           return undefined;

        } catch (error) {
            log.warn("updateCurrentEntity()", "Error querying entity with auth ID: " + authId, error);

            this.setCurrentEntity( undefined );

            throw new Error( (error as any).message );
        }
    }

    async setCurrentEntity( entity? : Entity) : Promise<void> {

        log.traceIn( "setCurrentEntity()", entity );

        try {
            if( entity == null && this._currentEntity == null ) {

                log.traceOut( "setCurrentEntity()", "no change, already cleared" );
                return;
            }

            if( entity == null && this._currentEntity != null ) {

                delete this._currentEntity;

                await this.clearCurrentEntityKeys();

                log.traceOut( "setCurrentEntity()", "cleared current entity" );
                return;
            }

            if( entity!.id.value() == null ) {

                throw new Error( "Current entity must have a valid document ID");
            }

            if( entity != null &&  this._currentEntity != null  ) {

                this._currentEntity = entity;

                await this.updateCurrentEntityKeys();
                log.traceOut( "setCurrentEntity()", "new current entity" );
                return;
            }

            if( entity!.id.value()! !== this._currentEntity!.id.value() ) {

                this._currentEntity = entity;

                await this.updateCurrentEntityKeys();
                log.traceOut( "setCurrentEntity()", "updated current entity" );
                return;
            }

            log.traceOut( "setCurrentEntity()", "No change to current entity" );

        } catch (error) {
            log.warn("init()", "Error setting current entity", error);

            await this.clearCurrentEntityKeys();
            delete this._currentEntity;

            throw new Error( (error as any).message );
        }

    }

    currentEntity() : Entity | undefined {

        return this._currentEntity;
    }

    
    async clearCurrentEntityKeys() : Promise<void> {
    }

    async updateCurrentEntityKeys() : Promise<void> {
        /*
        try {
            //log.traceIn( "updateAuthenticatedEntityKeys()" );

            if( this.symmetricCipher.defaultKey() === undefined ) {

                let defaultKey;

                if( application!.environment === Environments.Client ) {
                    defaultKey = this.authenticatedEntity?.claims.get("defaultKey")?.value;
                }
                else {
                    const defaultKeyId = this.context.configuration.config(
                        SecurityConfigurationName, "defaultKeyId")!;
    
                    defaultKey = await this.keyManager?.symmetricKey( defaultKeyId );
                }

                this.symmetricCipher.setDefaultKey( defaultKey == null ? null : defaultKey );

                log.debug( "updateCurrentKeys()", "updated default", defaultKey == null ? null : defaultKey.id) ;
            }

            const authId = this.authenticatedEntity?.authId;

            //log.debug( "updateCurrentKeys()", {organizationId} );

            if( authId != null ) {

                const key = this.symmetricCipher.key();

                //log.debug( "updateCurrentKeys()", "key", key?.id );

                if( key === undefined || (key != null && key.id !== authId ) ) { 

                    let key;

                    if( this.context.environment === Environments.Client ) {

                        key = this.authenticatedEntity?.claims.get("key")?.value;
                    }
                    else {
                        key = await this.keyManager?.symmetricKey( authId );
                    }

                    this.symmetricCipher.setKey( key == null ? null : key );

                    log.debug( "updateKeys()", "updated", key == null ? null : key.id ) ;
                }
            }

            //log.traceOut( "updateAuthenticatedEntityKeys()" ) ; 

        } catch (error) {
            log.warn( "updateKeys()", "Failed to update cipher keys", error ) ;

            this.symmetricCipher.setDefaultKey( null );

            this.symmetricCipher.setKey( null );

            throw new Error( (error as any).message );
        }
        */
    }



    readonly name = DatabaseServiceName;

    isInitialized = false;

    readonly _databaseFactories = new Map<string,DatabaseFactory>();

    private _currentEntity? : Entity;

}
