import { log } from "../../api/dao/abstractDatabaseService";
import { DatabaseDocument } from "../../api/dao/databaseDocument";
import { ReferenceProperty } from "../../api/properties/referenceProperty";
import { OptionsSource } from "../../api/types/optionsSource";

export class OptionsReference<D extends DatabaseDocument, O extends OptionsSource> {  

    constructor( property : ReferenceProperty<D>, optionsReferencePropertyKey : keyof D ) {
        //log.traceInOut( "constructor()", property, optionsReferencePropertyKey );

        this._property = property; 

        this._optionsReferencePropertyKey = optionsReferencePropertyKey as string;
    }

    async optionsReferenceProperty() : Promise<O | undefined> {

        log.traceIn( "optionsReferenceProperty()" );

        try {            
            let databaseDocument;

            const referenceProperty = this._property as ReferenceProperty<D>;

            if( referenceProperty.path() == null ) {
                log.traceOut( "optionsReferenceProperty()", "empty reference" );
                return undefined;
            }

            databaseDocument = await referenceProperty.document() as D;
            

            if( databaseDocument == null ) {
                log.traceOut( "optionsReferenceProperty()", "empty document" );
                return undefined;
            }

            const optionsReferenceProperty = databaseDocument[this._optionsReferencePropertyKey as keyof DatabaseDocument] as any;

            if( optionsReferenceProperty == null ) {
                throw new Error( "invalid options reference property" );
            }

            log.traceOut( "optionsReferenceProperty()", optionsReferenceProperty );
            return optionsReferenceProperty as O;  

        } catch( error ) {

            log.warn( "optionsReferenceProperty()", "Error reading options", error );

            throw new Error( (error as any).message );
        }    
    }

    private readonly _property : ReferenceProperty<D>;

    private readonly _optionsReferencePropertyKey : string;

}