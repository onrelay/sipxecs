import { CollectionDatabase } from "../../core/spec/collectionDatabase";
import { User } from "../spec/user";
import { DefinitionProperty } from "../../properties/spec/definitionProperty";
import { DefinitionPropertyImpl } from "../../properties/impl/definitionPropertyImpl";
import { log } from "../../core/base/abstractDatabaseService";
import { CountryPropertyImpl } from "../../properties/impl/countryPropertyImpl";
import { CountryProperty } from "../../properties/spec/countryProperty";
import { Language, LanguageName, Languages } from "@dao/common";
import { AbstractEntity } from "./abstractEntity";

export abstract class AbstractUser extends AbstractEntity implements User {  

    constructor( 
        userDocumentName : string,
        userCollection : CollectionDatabase<User>, 
        documentPath? : string  ) {   

        super( userDocumentName, userCollection, documentPath );

        try {
            this.country = new CountryPropertyImpl( this );
            
            this.language = new DefinitionPropertyImpl<Language>( 
                this, LanguageName, Languages );

            //log.traceInOut( "constructor()", KeysCollection ); 

        } catch( error ) {

            log.warn( "constructor()", "Error initializing key", error );
            
            throw new Error( (error as any).message );
        }
    }

    async onCreate() : Promise<void> {

        //log.traceIn( "onCreate()" );

        try {

            await super.onCreate();
    
            //log.traceOut( "onCreate()" );
  
        } catch( error ) {
            
            log.warn( "onCreate()", "Error handling created notification", error );
  
            throw new Error( (error as any).message );
        }
    }

    async onUpdate() : Promise<void> {

        //log.traceIn( "onUpdate()" );

        try {

            await super.onUpdate();
    
            //log.traceOut( "onUpdate()" );
  
        } catch( error ) {
            
            log.warn( "onUpdated()", "Error handling updated notification", error );
  
            throw new Error( (error as any).message );
        }
    }

    async onDelete() : Promise<void> {

        //log.traceIn( "onDelete()" );

        try {

            await super.onDelete();
    
            //log.traceOut( "onDelete()" );
  
        } catch( error ) {
            
            log.warn( "onDelete()", "Error handling deleted notification", error );
  
            throw new Error( (error as any).message );
        }
    }
    
    readonly country : CountryProperty;

    readonly language : DefinitionProperty<Language>;    
}
