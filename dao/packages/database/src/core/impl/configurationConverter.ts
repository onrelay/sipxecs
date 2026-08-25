import { AbstractDatabaseConverter } from "../base/abstractDatabaseConverter";


export class BasicDatabaseConverter extends AbstractDatabaseConverter {

    toDate( dateText? : string ) : Date | undefined {

        if( dateText == null ) {
            return undefined;
        }
        const time = Date.parse( dateText );

        if( isNaN(time) ) {
            return undefined;
        }
        
        const date = new Date();

        date.setTime( time );

        return date;
    }

    fromDate( date? : Date ) : string | undefined {

        if( date == null ) {
            return undefined;
        }

        return date.toUTCString();
    }

    toGeolocation( geolocation? : any ) : any | undefined {
        return geolocation;
    }

    fromGeolocation( geolocation? : any ) : any | undefined {

        return geolocation;
    }
    
}
