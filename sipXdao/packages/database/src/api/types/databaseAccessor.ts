import { DatabaseAccess } from "./databaseAccess";

export interface DatabaseAccessor {

    databaseAccess( databasePath : string ) : DatabaseAccess;
}

 