import { DatabaseManager } from "../../api/dao/databaseManager";
import { DatabaseDocument } from "./databaseDocument";

export interface ConfigurationDatabaseManager extends DatabaseManager {
    
    loadConfigDocument( data : any ) : Promise<DatabaseDocument>;

    configDocument( databaseDocumentPath : string ) : Promise<DatabaseDocument | undefined>;
}