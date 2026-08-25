
import { log } from "../base/abstractDatabaseService";
import { DatabaseDocument } from "../spec/databaseDocument";
import { databaseServiceFactory } from "./databaseServiceFactory";

export class ReferenceHandle<DerivedDocument extends DatabaseDocument> {

    constructor( handle: {

        title? : string,
    
        date? : Date,
    
        path : string,

        uri : string,
    
        databaseDocument?: DerivedDocument,
    
        documentReference?: any
    }) {

        this.referenceHandleTitle = handle.title;

        this.date = handle.date;

        this.path = handle.path;

        this.uri = handle.uri;

        this.databaseDocument = handle.databaseDocument;

        this.documentReference = handle.documentReference;
    }

    toRecord() : Record<string, any> {

        const record = {

            title: this.referenceHandleTitle,

            date: this.date,

            path:  this.path,

            uri: this.uri
                
        } as Record<string, any>;
        
        return record
    }

    toJson() : string {

        return JSON.stringify( this.toRecord() );
    }

    fromJson( json : string ) {

        const handle = JSON.parse( json );

        this.referenceHandleTitle = handle.title;

        this.date = handle.date;

        this.path = handle.path;

        this.uri = handle.uri;

        delete this.databaseDocument;

        delete this.documentReference;
    }

    copy() {
        return new ReferenceHandle( {

            title: this.referenceHandleTitle,

            date: this.date,

            path: this.path,

            uri: this.uri,

            databaseDocument: this.databaseDocument,

            documentReference: this.documentReference
        })
    }

    compareTo( other? : ReferenceHandle<DerivedDocument> ) : number {

        if( other?.path == null ) {
            return 1;
        }

        const pathCompare = this.path.localeCompare( other.path );

        const titleCompare = this.referenceHandleTitle != null && other?.referenceHandleTitle != null ? 
            this.referenceHandleTitle!.localeCompare( other!.referenceHandleTitle! ) : 0;

        return pathCompare !== 0 && titleCompare !== 0 ?
            titleCompare :
            pathCompare;  
    }

    async fetch( force? : boolean) : Promise<DerivedDocument> {

        try {

            if( !!force || this.databaseDocument == null ) {
                
                this.databaseDocument = 
                    await databaseServiceFactory!.get().databaseFactory.documentFromUri( this.path ) as DerivedDocument;

                this.documentReference = this.databaseDocument.documentReference();

                this.date = this.databaseDocument.referenceDateProperty()?.value();

                this.referenceHandleTitle = this.databaseDocument.title.value();
            }
            
            return this.databaseDocument;  

        } catch (error) {

            log.warn("Error reading reference handle", {error} );

            throw new Error( (error as any).message );
        } 
    }

    referenceHandleTitle? : string;  
    
    date? : Date;

    path : string;

    uri : string;

    databaseDocument?: DerivedDocument;

    documentReference?: any;
    
}
