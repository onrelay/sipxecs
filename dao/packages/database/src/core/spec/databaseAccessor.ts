import { DatabaseAccess } from "../impl/databaseAccess";

export interface DatabaseAccessor {

    databaseAccess( databasePath : string ) : DatabaseAccess;
}

 