import { AbstractDatabaseAccessor } from "../base/abstractDatabaseAccessor";
import { DatabaseAccess } from "./databaseAccess";

export class UnrestrictedDatabaseAccessor extends AbstractDatabaseAccessor { 

    databaseAccess( databasePath : string ) : DatabaseAccess {

        return DatabaseAccess.allowAll()
    }

}
