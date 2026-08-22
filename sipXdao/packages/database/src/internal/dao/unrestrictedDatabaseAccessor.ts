import { AbstractDatabaseAccessor } from "../../api/dao/abstractDatabaseAccessor";
import { DatabaseAccess } from "../../api/types/databaseAccess";

export class UnrestrictedDatabaseAccessor extends AbstractDatabaseAccessor { 

    databaseAccess( databasePath : string ) : DatabaseAccess {

        return DatabaseAccess.allowAll()
    }

}
