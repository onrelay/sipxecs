import { DatabaseAccess } from "../impl/databaseAccess";
import { DatabaseAccessor } from "../spec/databaseAccessor";

export abstract class AbstractDatabaseAccessor implements DatabaseAccessor {

    abstract databaseAccess( databasePath : string ) : DatabaseAccess;
}
