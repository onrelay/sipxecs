import { AbstractDatabaseConverter } from "../../../../database/src/api/dao/abstractDatabaseConverter";
import { log } from "../../../../database/src/api/dao/abstractDatabaseService";
import { Timestamp } from "mongodb";

export class MongoConverter extends AbstractDatabaseConverter {

    constructor() {

        super();
    }

    toDate( timestamp : any ) : Date | undefined {

        try {
            if( timestamp == null ) {
                return undefined;
            }

            // The mongodb driver automatically converts BSON Dates to JS Date objects.
            // This method handles cases where the data might be a BSON Timestamp or a JS Date already.
            if (timestamp instanceof Date) {
                return timestamp;
            }

            if (timestamp instanceof Timestamp) {
                return timestamp.toDate();
            }

            // Handle legacy Firebase-like timestamp objects if they exist in your data
            if (typeof timestamp === 'object' && timestamp.seconds != null && timestamp.nanoseconds != null) {
                return new Date(timestamp.seconds * 1000 + timestamp.nanoseconds / 1000000);
            }

            log.warn( "toDate()", "Received an unrecognized timestamp format.", {timestamp} );
            return undefined;

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

            // The mongodb driver handles JS Date objects automatically.
            // Simply return the Date object, and the driver will convert it to a BSON Date.
            return date;

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
            // Convert from GeoJSON format which is idiomatic for MongoDB
            if (geoPoint.type === 'Point' && Array.isArray(geoPoint.coordinates)) {
                return { lat: geoPoint.coordinates[1], lon: geoPoint.coordinates[0] };
            }
            // Fallback for other formats if necessary, but log a warning.
            log.warn("toGeolocation()", "Non-GeoJSON format encountered. For geospatial queries, store data as GeoJSON: { type: 'Point', coordinates: [lon, lat] }", {geoPoint});
            if (geoPoint.latitude != null && geoPoint.longitude != null) {
                return { lat: geoPoint.latitude, lon: geoPoint.longitude };
            }
            return undefined;

        } catch( error ) {
            log.warn( "toGeolocation()", "Error converting geoPoint:", {geoPoint} )
            return undefined;
        }
    }

    fromGeolocation( geolocation : any ) : any | undefined {

        try {
            if (geolocation == null || geolocation.lat == null || geolocation.lon == null) {
                return undefined;
            }

            // Convert to GeoJSON Point format for optimal storage and querying in MongoDB
            return {
                type: "Point",
                coordinates: [geolocation.lon, geolocation.lat]
            };

        } catch( error ) {
            log.warn( "fromGeolocation()", "Error converting geolocation:", {geolocation} )
            return undefined;
        }
    }
}
