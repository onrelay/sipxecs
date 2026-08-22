import { DatabaseAccess } from "../types/databaseAccess";
import { DatabaseAccessor } from "../types/databaseAccessor";

export abstract class AbstractDatabaseAccessor implements DatabaseAccessor {

    abstract databaseAccess( databasePath : string ) : DatabaseAccess;
}
