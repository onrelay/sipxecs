import { InitialKeyVersion, KeyFormatName, KeyFormats, KeyStatus, KeyStatuses, KeyStatusName, KeyTypeName, KeyTypes, KeyVault, KeyVaultName, KeyVaults } from "@sipxdao/security";
import { GenericDatabaseDocument } from "../../api/dao/genericDatabaseDocument";
import { CollectionDatabase } from "../../api/dao/collectionDatabase";
import { Entity, EntityDocumentName } from "../../api/dao/entity";
import { Key, KeyDocumentName } from "../../api/dao/key";
import { DefinitionProperty } from "../../api/properties/definitionProperty";
import { LongTextProperty } from "../../api/properties/LongTextProperty";
import { OwnerProperty } from "../../api/properties/ownerProperty";
import { TextProperty } from "../../api/properties/textProperty";
import { OwnerPropertyImpl } from "../properties/ownerPropertyImpl";
import { NumberProperty } from "../../api/properties/numberProperty";
import { LongTextPropertyImpl } from "../properties/longTextPropertyImpl";
import { TextType, TextTypes } from "../../api/types/textType";
import { TextPropertyImpl } from "../properties/textPropertyImpl";
import { DefinitionPropertyImpl } from "../properties/definitionPropertyImpl";
import { NumberPropertyImpl } from "../properties/numberPropertyImpl";
import { log } from "../../api/dao/abstractDatabaseService";

export class KeyImpl extends GenericDatabaseDocument implements Key {  

    constructor( keyCollection : CollectionDatabase<Key>, ownerCollectionName : string, documentPath? : string  ) {   

        super( KeyDocumentName, keyCollection, documentPath );

        try {
            this.owner = new OwnerPropertyImpl<Entity>( this, ownerCollectionName, EntityDocumentName );

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


    readonly owner: OwnerProperty<Entity>;

    readonly publicKey : LongTextProperty;

    readonly secretKey : TextProperty;  // Only for temporary storage

    readonly keyVault : DefinitionProperty<KeyVault>;

    readonly version : NumberProperty;

    readonly keyStatus : DefinitionProperty<KeyStatus>;

    readonly keyType : DefinitionProperty<KeyType>;

    readonly keyFormat : DefinitionProperty<KeyFormat>;

}
