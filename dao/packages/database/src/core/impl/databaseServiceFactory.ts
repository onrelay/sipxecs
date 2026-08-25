import { ServiceFactory } from "@dao/common";
import { DatabaseService } from "../spec/databaseService";

export let databaseServiceFactory : DatabaseServiceFactory | undefined;

export class DatabaseServiceFactory extends ServiceFactory<DatabaseService> {

    static create( databaseService : DatabaseService ) : void {

        databaseServiceFactory = new DatabaseServiceFactory();

        databaseServiceFactory.init( databaseService );
    }
}