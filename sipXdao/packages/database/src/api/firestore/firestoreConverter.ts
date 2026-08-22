import { AbstractDatabaseConverter } from "../dao/abstractDatabaseConverter";
import { log } from "../dao/abstractDatabaseService";

export class FirestoreConverter extends AbstractDatabaseConverter {

    constructor( timestampType : any, firebase : any ) {

        super();

        this._timestampType = timestampType;

        this._firebase = firebase;
    }

    toDate( timestamp : any ) : Date | undefined {

        try {
            if( timestamp == null ) {
                return undefined;
            }

            const timestampObject = new this._timestampType( 
                timestamp.seconds != null ? timestamp.seconds : 
                    timestamp._seconds != null ? timestamp._seconds :
                        0, 
                timestamp.nanoseconds != null ? timestamp.nanoseconds : 
                    timestamp._nanoseconds != null ? timestamp._nanoseconds :
                    0 );

            return timestampObject.toDate();

        } catch( error ) {
            log.warn( "toDate()", "Error converting timestamp:", (error as any).message, {timestamp} )
            return undefined;
        }
    }

    fromDate( date : Date ) : any | undefined {

        try {
            if( date == null || date.getTime() === 0 ) {
                return undefined;
            }

            return this._timestampType.fromDate( date ); 

        } catch( error ) {
            log.warn( "fromDate()", "Error converting date: ", (error as any).message, {date} )
            return undefined;
        }
    }    

    toGeolocation( geoPoint : any ) : any | undefined {

        try {
            if( geoPoint == null ) {
                return undefined;
            }
            return { lat: geoPoint.latitude, lon: geoPoint.longitude };

        } catch( error ) {
            log.warn( "toGeolocation()", "Error converting geoPoint:", {geoPoint} )
            return undefined;
        }
    }

    fromGeolocation( geolocation : any ) : any | undefined {

        try {
            if( geolocation == null ) {
                return undefined;
            }

            return this._firebase.firestore.GeoPoint( geolocation.lat, geolocation.lon );

        } catch( error ) {
            log.warn( "fromGeolocation()", "Error converting geolocation:", {geolocation} )
            return undefined;
        }
    } 
    
    private _timestampType : any;

    private readonly _firebase : any;

}
