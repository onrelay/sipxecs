
import { DatabaseConverter } from "../spec/databaseConverter";

export abstract class AbstractDatabaseConverter implements DatabaseConverter {

    abstract toDate( data? : any ) : Date | undefined;

    abstract fromDate( date? : Date ) : any | undefined;

    abstract toGeolocation( data? : any ) : any | undefined;

    abstract fromGeolocation( geolocation? : any ) : any | undefined;
    
}
