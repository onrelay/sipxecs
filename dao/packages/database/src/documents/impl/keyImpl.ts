import { InitialKeyVersion, KeyFormatName, KeyFormats, KeyStatus, KeyStatuses, KeyStatusName, KeyTypeName, KeyTypes, KeyVault, KeyVaultName, KeyVaults } from "@dao/security";
import { GenericDatabaseDocument } from "../../core/impl/genericDatabaseDocument";
import { CollectionDatabase } from "../../core/spec/collectionDatabase";
import { User, UserDocumentName } from "../spec/user";
import { Key, KeyDocumentName } from "../spec/key";
import { DefinitionProperty } from "../../properties/spec/definitionProperty";
import { LongTextProperty } from "../../properties/spec/LongTextProperty";
import { OwnerProperty } from "../../properties/spec/ownerProperty";
import { TextProperty } from "../../properties/spec/textProperty";
import { OwnerPropertyImpl } from "../../properties/impl/ownerPropertyImpl";
import { NumberProperty } from "../../properties/spec/numberProperty";
import { LongTextPropertyImpl } from "../../properties/impl/longTextPropertyImpl";
import { TextType, TextTypes } from "../../core/defs/textType";
import { TextPropertyImpl } from "../../properties/impl/textPropertyImpl";
import { DefinitionPropertyImpl } from "../../properties/impl/definitionPropertyImpl";
import { NumberPropertyImpl } from "../../properties/impl/numberPropertyImpl";
import { log } from "../../core/base/abstractDatabaseService";

export class KeyImpl extends GenericDatabaseDocument implements Key {  

    constructor( keyCollection : CollectionDatabase<Key>, ownerCollectionName : string, documentPath? : string  ) {   

        super( KeyDocumentName, keyCollection, documentPath );

        try {
            this.owner = new OwnerPropertyImpl<User>( this, ownerCollectionName, UserDocumentName );

            this.publicKey = new LongTextPropertyImpl( this ); 

            this.secretKey = new TextPropertyImpl( this, TextTypes.Password as TextType ); 

            this.keyVault = new DefinitionPropertyImpl<KeyVault>( 
                this, 
                KeyVaultName, 
                KeyVaults );

            this.version = new NumberPropertyImpl( this, InitialKeyVersion, InitialKeyVersion );  

            this.keyType = new DefinitionPropertyImpl<KeyType>(
                this, 
                KeyTypeName, 
                KeyTypes );

            this.keyFormat = new DefinitionPropertyImpl<KeyFormat>(
                this, 
                KeyFormatName, 
                KeyFormats );

            this.keyStatus = new DefinitionPropertyImpl<KeyStatus>( 
                this, 
                KeyStatusName, 
                KeyStatuses );


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


    readonly owner: OwnerProperty<User>;

    readonly publicKey : LongTextProperty;

    readonly secretKey : TextProperty;  // Only for temporary storage

    readonly keyVault : DefinitionProperty<KeyVault>;

    readonly version : NumberProperty;

    readonly keyStatus : DefinitionProperty<KeyStatus>;

    readonly keyType : DefinitionProperty<KeyType>;

    readonly keyFormat : DefinitionProperty<KeyFormat>;

}
