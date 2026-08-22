import { ServiceFactory } from "@sipxdao/common";
import { DatabaseService } from "./databaseService";

export let databaseServiceFactory : DatabaseServiceFactory | undefined;

export class DatabaseServiceFactory extends ServiceFactory<DatabaseService> {

    static create( databaseService : DatabaseService ) : void {

        databaseServiceFactory = new DatabaseServiceFactory();

        databaseServiceFactory.init( databaseService );
    }
}