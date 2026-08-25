import { GenericDatabaseDocument } from "../../core/impl/genericDatabaseDocument";
import { CollectionDatabase } from "../../core/spec/collectionDatabase";
import { User, UserDocumentName } from "../spec/user";
import { Key, KeyDocumentName } from "../spec/key";
import { DefinitionProperty } from "../../properties/spec/definitionProperty";
import { OwnerProperty } from "../../properties/spec/ownerProperty";
import { OwnerPropertyImpl } from "../../properties/impl/ownerPropertyImpl";
import { DefinitionPropertyImpl } from "../../properties/impl/definitionPropertyImpl";
import { log } from "../../core/base/abstractDatabaseService";
import { Change } from "../spec/change";
import { DatabaseDocument } from "../../core/spec/databaseDocument";
import { ReferenceProperty } from "../../properties/spec/referenceProperty";
import { ChangeType, ChangeTypeName, ChangeTypes } from "../../core/defs/changeType";
import { MapProperty } from "../../properties/spec/mapProperty";
import { ReferencePropertyImpl } from "../../properties/impl/referencePropertyImpl";
import { MapPropertyImpl } from "../../properties/impl/mapPropertyImpl";
import { PropertyType, PropertyTypes } from "../../core/defs/propertyType";

export class ChangeImpl extends GenericDatabaseDocument implements Change {  

    constructor( keyCollection : CollectionDatabase<Key>, ownerCollectionName : string, documentPath? : string  ) {   

        super( KeyDocumentName, keyCollection, documentPath );

        try {
            this.changedDocument = new OwnerPropertyImpl<User>( this, ownerCollectionName, UserDocumentName );

            this.changedBy = new ReferencePropertyImpl<User>( this );
            
            this.changeType = new DefinitionPropertyImpl<ChangeType>(
                this, 
                ChangeTypeName, 
                ChangeTypes );

            this.changed = new MapPropertyImpl<any>( this, PropertyTypes.Map as PropertyType );

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

    readonly changedDocument: OwnerProperty<DatabaseDocument>;
    
    readonly changedBy : ReferenceProperty<User>;

    readonly changeType : DefinitionProperty<ChangeType>;

    readonly changed : MapProperty<any>;


}
