import { Logger, Service } from "@sipxdao/common";
import { DatabaseFactory } from "./databaseFactory";
import { DatabaseManager } from "./databaseManager";
import { DatabaseAccessor } from "../types/databaseAccessor";
import { Entity } from "./entity";
import { AuthenticatedEntity } from "@sipxdao/authentication";

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

