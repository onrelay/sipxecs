import { Logger, Service } from "@dao/common";
import { DatabaseFactory } from "./databaseFactory";
import { DatabaseManager } from "./databaseManager";
import { DatabaseAccessor } from "./databaseAccessor";
import { AuthenticatedEntity } from "@dao/authentication";
import { Entity } from "../../documents/spec/entity";

export const DatabaseServiceName = "databaseService";

export const DatabaseConfigurationName = "database";

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

    authenticatedEntity() : AuthenticatedEntity | undefined;

    authenticatedDatabaseEntity() : Entity | undefined;
        
    readonly databaseFactory : DatabaseFactory;

    readonly databaseManager : DatabaseManager;

    readonly databaseAccessor : DatabaseAccessor;

}

