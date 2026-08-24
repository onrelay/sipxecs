import { Logger, Service } from "@dao/common";
import { DatabaseFactory } from "./databaseFactory";
import { DatabaseManager } from "./databaseManager";
import { Entity } from "../../documents/spec/entity";
import { DatabasePlatform } from "../defs/databasePlatform";

export const DatabaseServiceName = "database";

export const DatabasesConfigurationName = "databases";

export const CollectionGroupPathSuffix = "-group";

export const NewObjectId = "new";

export const TemplatePathKey = "template";

export const IdSuffix = "Id";
export const IdsSuffix = "Ids";

export const OwnerIds = "ownerIds";

export const ChangesCollection = "changes";

export const TemplatesCollection = "templates";


export interface DatabaseService extends Service {

    init() : Promise<void>;

    updateCurrentEntity( 
        entityCollectionName : string, 
        authId : string ) : Promise<Entity | undefined>; 

    setCurrentEntity( entity? : Entity) : Promise<void>;

    currentEntity() : Entity | undefined;

    updateCurrentEntityKeys() : Promise<void>;

    clearCurrentEntityKeys() : Promise<void>;

    addDatabaseFactory( databaseFactory : DatabaseFactory ) : Promise<void>;

    removeDatabaseFactory(  
        databasePlatform : DatabasePlatform, 
        databaseName : string ) : Promise<boolean>;

    databaseFactory( 
        databasePlatform : DatabasePlatform, 
        databaseName : string ) : DatabaseFactory | undefined;

    databasePrefix( databasePlatform : DatabasePlatform, databaseName : string ) : string;
}

