

export interface DatabaseConverter {

    toDate( data? : any ) : Date | undefined;

    fromDate( date? : Date ) : any | undefined;

    toGeolocation( data? : any ) : any | undefined;

    fromGeolocation( geolocation? : any ) : any | undefined;
    
}
