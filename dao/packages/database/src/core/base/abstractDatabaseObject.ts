import { DatabaseRecord } from "../types/databaseRecord";
import { AbstractObservable } from "@dao/common";
import { DatabaseObject } from "../spec/databaseObject";
import { PropertiesSelector } from "../types/propertiesSelector";
import { PropertyType, PropertyTypes } from "../defs/propertyType";
import { TemplateProperty } from "../../properties/spec/templateProperty";
import { Template } from "../../documents/spec/template";
import { DatabaseDocument } from "../spec/databaseDocument";
import { TemplatedProperties } from "../spec/templatedProperties";
import { SubdocumentProperty } from "../../properties/spec/subdocumentProperty";
import { DatabaseAccess } from "../impl/databaseAccess";
import { CollectionProperty } from "../../properties/spec/collectionProperty";
import { CollectionGroupDatabase } from "../spec/collectionGroupDatabase";
import { CollectionDatabase } from "../spec/collectionDatabase";
import { Database } from "../spec/database";
import { ReferenceHandle } from "../impl/referenceHandle";
import { SubdocumentsProperty } from "../../properties/spec/subdocumentsProperty";
import { DatabaseSubdocument } from "../spec/databaseSubdocument";
import { TextPropertyImpl } from "../../properties/impl/textPropertyImpl";
import { AbstractDatabaseProperty } from "./abstractDatabaseProperty";
import { DatabaseProperty } from "../spec/databaseProperty";
import { TextProperty } from "../../properties/spec/textProperty";
import { log } from "./abstractDatabaseService";
import { TemplatedDocument } from "../spec/templatedDocument";
import { databaseServiceFactory } from "../impl/databaseServiceFactory";
import { GenericDatabaseSubdocument } from "../impl/genericDatabaseSubdocument";


export abstract class AbstractDatabaseObject extends AbstractObservable implements DatabaseObject {

  constructor( name : string, parent? : DatabaseObject ) {

    super(); 

    try {

      this._isLoaded = false;

      this.parent = parent;

      this.id = new TextPropertyImpl( this );
      this.id.trackChanges = false;
      this.id.encrypt = false;

      this.name = new TextPropertyImpl( this );
      this.name.trackChanges = false;
      this.name.encrypt = false;
      this.name.setValue( name );

      this.title = new TextPropertyImpl(this);

      //log.traceInOut( "constructor()" ); 

    } catch (error) {

      log.warn("constructor()", "Error creating database document", error);

      throw new Error( (error as any).message );
    }
  }

  fromRecord(record: DatabaseRecord): void {
    //log.traceIn("fromRecord()", data);

    try {

      if( record == null ) {
        //log.traceOut("fromRecord()", "no data");
        return;
      }

      for( const property of Object.values(this) ) {

        if ( property instanceof AbstractDatabaseProperty) {

          (property as AbstractDatabaseProperty<any>).fromRecord(record);
        }
      }


      //log.traceOut("fromRecord()", this);

    } catch (error) {

      log.warn("fromRecord()", "Error reading document", error);

      throw new Error( (error as any).message );
    }
  }

  async toRecord( force? : boolean ): Promise<DatabaseRecord> {
    //log.traceIn("toRecord()", {force})
    try {

      let record: DatabaseRecord = {};

      const properties = Object.values(this);

      for( const property of properties ) {

        if (property instanceof AbstractDatabaseProperty) {

          const databaseProperty = property as AbstractDatabaseProperty<any>;

          await databaseProperty.toRecord( record, force ); 
        }
      }

      //log.traceOut("toRecord()", data)
      return record;

    } catch (error) {
      log.warn("toRecord()", "Error writing database object to record", error);

      throw new Error( (error as any).message );
    }
  }

  async toJson() : Promise<string> {

    //log.traceIn("toJson()");

    try {
      const record = await this.toRecord( true );

      const json = JSON.stringify( record );

      //log.traceOut("toJson()", json );
      return json;

    } catch (error) {

      log.warn("toRecord()", "Error writing database object to json string", error);

            throw new Error( (error as any).message );
    }
  }

  fromJson( json : string ) : void {

    //log.traceIn("fromJson()", json );

    try {
      const data = JSON.parse( json );

      this.fromRecord( data );

      //log.traceOut("fromJson()", json );
    } catch (error) {
      log.warn("fromJson()", "Error reading database object from json string", error);

            throw new Error( (error as any).message );
    }
  }

  property(key: string): DatabaseProperty<any> | undefined {

    const property = Object(this)[key];

    if( property?.type != null && property instanceof AbstractDatabaseProperty) {
      return property as DatabaseProperty<any>;
    }

    return undefined;  
  }

  properties(propertiesSelector?: PropertiesSelector): Map<string, DatabaseProperty<any>> {

    //log.traceIn("properties()", propertiesSelector);

    try {

      let result = new Map<string, DatabaseProperty<any>>();

      if( propertiesSelector?.includePropertyKeys != null ) {

        for( const includePropertyKey of propertiesSelector.includePropertyKeys ) {

          // This ensures order of includePropertyKeys remains intact

          const property = this.property( includePropertyKey );

          if( property != null && !result.has( includePropertyKey ) ) {

            result.set( includePropertyKey, property );
          }
        }
      }

      for( const propertyKey in Object(this) ) {

        if (result.has(propertyKey)) {
          continue;
        }

        const property = this.property( propertyKey );

        if( property == null ) {
          continue;
        }

        if( propertiesSelector?.includePropertyKeys != null &&
            !propertiesSelector.includePropertyKeys.includes(propertyKey)) {

          continue;
        }

        if( propertiesSelector?.excludePropertyKeys != null &&
            propertiesSelector.excludePropertyKeys.includes(propertyKey)) {

          continue;
        }

        if( propertiesSelector?.includePropertyTypes != null &&
            !propertiesSelector.includePropertyTypes.includes(property.type)) {

          continue
        }

        if( propertiesSelector?.excludePropertyTypes != null &&
            propertiesSelector.excludePropertyTypes.includes(property.type)) {

          continue;
        }
        
        result.set(propertyKey, property);
      }

      //log.traceOut("properties()", result);
      return result;

    } catch (error) {
      log.warn("Error reading properties for database object", error);

      throw new Error( (error as any).message );
    }
  }

  async copyFrom( other : DatabaseObject) : Promise<void> {

    //log.traceIn( "copyFrom()", other, includeCollections );

    try {

      await this.copyProperties( other );

      this._isLoaded = (other as AbstractDatabaseObject)._isLoaded

      //log.traceOut( "copyFrom()", this );
      return;

    } catch( error ) {
        
        log.warn( "copyFrom()", "Error copying database object", error );

        throw new Error( (error as any).message );
      }
  }

  async copyProperties( other : DatabaseObject, propertiesSelector?: PropertiesSelector ) : Promise<boolean> {

    //log.traceIn("copyProperties()", {other});

    try {
      let changed = false;

      const excludePropertyKeys = [
        "id",
        "archived",
        "archivedAt",
        "templatedProperties"];

      const excludePropertyTypes = [
        PropertyTypes.Collection as PropertyType
      ];

      const augmentedPropertiesSelector = propertiesSelector != null ? propertiesSelector : {} as PropertiesSelector;

      augmentedPropertiesSelector.excludePropertyKeys =  augmentedPropertiesSelector.excludePropertyKeys != null ? 
        augmentedPropertiesSelector.excludePropertyKeys.concat(excludePropertyKeys) : excludePropertyKeys;

      augmentedPropertiesSelector.excludePropertyTypes =  augmentedPropertiesSelector.excludePropertyTypes != null ? 
        augmentedPropertiesSelector.excludePropertyTypes.concat(excludePropertyTypes) : excludePropertyTypes;
      
      const otherProperties = other.properties( augmentedPropertiesSelector );

      for( const propertykeyValuePair of otherProperties ) {

        const propertyKey = propertykeyValuePair[0];

        const otherProperty = propertykeyValuePair[1];

        const property = this.property( propertyKey );

        if( property == null ) { 
          continue;
        }

        const savedTrackChanges = property.trackChanges;

        property.trackChanges = false;

        //log.debug("copyProperties()", property.key(), property.type, property.isChanged(), property.trackChanges ); 

        if( property.type === PropertyTypes.Template ) {

          const otherTemplate = await (otherProperty as TemplateProperty<Template<TemplatedDocument>>).document();

          if( otherTemplate != null ) {

            const template = await (property as TemplateProperty<Template<TemplatedDocument>>).document();

            changed = !!(await template?.copyProperties( otherTemplate )) || changed;      
            
            const databaseDocument = (this as any) as TemplatedDocument;
            const otherDatabaseDocument = (other as any) as TemplatedDocument;

            if( otherDatabaseDocument.templatedProperties != null ) {

              if( databaseDocument.templatedProperties == null ||
                  databaseDocument.templatedProperties.compareTo( otherDatabaseDocument.templatedProperties ) !== 0 ) { 
    
                  const templatedProperties = 
                    databaseServiceFactory!.get().databaseFactory.updateTemplatedProperties( 
                      databaseDocument, otherTemplate, false ) as TemplatedProperties;
        
                  const otherTamplatedProperties = 
                    otherDatabaseDocument.templatedProperties?.subdocument() as TemplatedProperties;
        
                  if( templatedProperties != null && templatedProperties != null ) {
        
                    await templatedProperties.copyProperties( otherTamplatedProperties );
                    
                    changed = true; 
                  }
              }
            }
          }
          property.copyStatesFrom( otherProperty ); 

        } 
        else if( property.type === PropertyTypes.Subdocument ) {

          const subdocumentProperty = property as SubdocumentProperty<DatabaseSubdocument>;

          const otherSubdocumentProperty = otherProperty as SubdocumentProperty<DatabaseSubdocument>;

          if( otherSubdocumentProperty.subdocument() != null ) {

            changed = !!(await subdocumentProperty.subdocument()?.copyProperties( otherSubdocumentProperty.subdocument()! )) || changed;
          }
        }  
        if( property.type === PropertyTypes.Subdocuments ) {

          const subdocumentsProperty = property as SubdocumentsProperty<DatabaseSubdocument>;

          const subdocuments = subdocumentsProperty.subdocuments();

          const otherSubdocumentsProperty = otherProperty as SubdocumentsProperty<DatabaseSubdocument>;

          const otherSubdocuments = otherSubdocumentsProperty.subdocuments();

          if( otherSubdocuments == null ) {

            if( subdocuments != null ) {

              subdocumentsProperty.setValue( undefined );
              changed = true;
            }
          }
          else if( subdocuments == null ) {
 
            if( otherSubdocuments != null ) {

              for( const otherSubdocumentEntry of otherSubdocuments ) {

                const otherSubdocument = otherSubdocumentEntry[1];
  
                const copiedSubdocument = subdocumentsProperty.newSubdocument();
  
                await copiedSubdocument.copyProperties( otherSubdocument );

                copiedSubdocument.id.setValue( otherSubdocument.id.value() );
  
                subdocumentsProperty.setSubdocument( copiedSubdocument );
              }
            }
            changed = true;
          }
          else {

            for( const subdocumentEntry of subdocuments ) {

              const key = subdocumentEntry[0];
              const subdocument = subdocumentEntry[1];

              const otherSubdocument = otherSubdocuments.get( key );

              if( otherSubdocument != null ) {
                changed = await subdocument.copyProperties( otherSubdocument ) || changed;

                subdocument.id.setValue( otherSubdocument.id.value() );

              }
              else {
                subdocuments.delete( key );

                changed = true;

              }
            }

            for( const otherSubdocumentEntry of otherSubdocuments ) {

              const key = otherSubdocumentEntry[0];
              const otherSubdocument = otherSubdocumentEntry[1];

              if( !subdocuments.has( key ) ) {
                
                const copiedSubdocument = subdocumentsProperty.newSubdocument();

                await copiedSubdocument.copyProperties( otherSubdocument );

                copiedSubdocument.id.setValue( otherSubdocument.id.value() );

                subdocumentsProperty.setSubdocument( copiedSubdocument );

                changed = true; 

                property.copyStatesFrom( otherProperty );

              }
            }
          }
          property.copyStatesFrom( otherProperty ); 

        } 
        else if( property.type === PropertyTypes.Organization ||
                 property.type === PropertyTypes.Owner ) {

          if( property.compareTo( otherProperty ) === 0 ) { // only copy if equal paths

            changed = property.copyValueFrom( otherProperty ) || changed;

            property.copyStatesFrom( otherProperty );

          }
        }
        else {
          changed = property.copyValueFrom( otherProperty ) || changed; 

          property.copyStatesFrom( otherProperty ); 
        }


        property.trackChanges = savedTrackChanges;
      }

      //log.traceOut("copyProperties()", {changed});
      return changed;

    } catch (error) {
      log.warn("Error copying properties for database object", error);

      throw new Error( (error as any).message );
    }
  }


  validate( propertiesSelector?: PropertiesSelector, markMissingProperties? : boolean ): Map<string, Error> {

    //log.traceIn("validate()");

    try {

      let result = new Map<string, Error>();

      const properties = this.properties( propertiesSelector );

      properties.forEach( property => {

        const error = property.validate();

        if( error != null ) {

          if( !!markMissingProperties ) {

            property.error = error;
          }

          result.set( property.key(), error );
        }
        else if( !!markMissingProperties ) {
          delete property.error;
        }
      });

      //log.traceOut("validate()", result);
      return result;

    } catch (error) {
      log.warn("Error validating properties for database object", error);

      throw new Error( (error as any).message );
    }
  }

  isLoaded() : boolean {
    return this._isLoaded;
  }


  isComplete( propertiesSelector?: PropertiesSelector): boolean {

    const validation = this.validate( propertiesSelector, false );

    //log.traceInOut( "isComplete()", {validation});

    return validation.size === 0;
  }


  isChanged() : boolean {

    //log.traceIn("isChanged()");

    const changedProperties = this.changedProperties();

    const changed = changedProperties.size > 0;

    //log.traceOut("isChanged()", changed );
    return changed;
  }

  changedProperties() : Map<string,DatabaseProperty<any>> {

    return this._changedProperties;
  }

  clearChanged() : void{

    //log.traceIn("clearChanged()");

    try {

      this._changedProperties.forEach( property => {

        if( property.type === PropertyTypes.Subdocument ) {

          const subdocumentProperty = property as SubdocumentProperty<DatabaseSubdocument>;

          if( subdocumentProperty.subdocument() != null ) {

            subdocumentProperty.subdocument()!.clearChanged(); 
          }
        }
        else if( property.type === PropertyTypes.Subdocuments ) {
          
          const subdocumentsProperty = property as SubdocumentsProperty<DatabaseSubdocument>;

          const subdocuments = subdocumentsProperty.subdocuments();

          if( subdocuments != null ) {

            for( const subdocument of subdocuments.values() ) {

              subdocument.clearChanged(); 
            }
          }
        }

        property.clearChanges();
      });

      this._changedProperties.clear();

      //log.traceOut("clearChanged()", result);

    } catch (error) {
      log.warn("Error clearing changed properties for database object", error);

      throw new Error( (error as any).message );
    }
  }

  propertyCount(propertiesSelector?: PropertiesSelector): number {

    return this.properties(propertiesSelector).size;
  }

  changesPropertiesSelector() : PropertiesSelector | undefined {

    // Override as required
    return undefined;
  }


  // Ovverride to validate any changes by returning false
  onChange( changingProperty : DatabaseProperty<any>, oldValue : any, newValue : any ): boolean {

    return true;
  }

  // Ovverride to process validated changes before they are saved / updated in DB
  onChanged( changedProperty : DatabaseProperty<any>, oldValue : any, newValue : any ): void {

    //log.traceIn("onChanged()", changedProperty.key() );

    try { 

      if( !changedProperty.trackChanges ) {
        //log.traceOut("onChanged()", "Not tracking changes", changedProperty.key() );
        return;
      }

        const changesPropertiesSelector = this.changesPropertiesSelector();

        if( changesPropertiesSelector != null ) {

          if( changesPropertiesSelector.excludePropertyKeys != null && 
            changesPropertiesSelector.excludePropertyKeys.includes(  changedProperty.key() ) ) {

            //log.traceOut("onChanged()", "key excluded", changedProperty.key() );
            return;
          }
            
          if( changesPropertiesSelector.includePropertyKeys != null && 
                !changesPropertiesSelector.includePropertyKeys.includes(  changedProperty.key() ) ) {
  
            //log.traceOut("onChanged()", "key not included", changedProperty.key() );
            return;
          }

          if( changesPropertiesSelector.excludePropertyTypes != null && 
              changesPropertiesSelector.excludePropertyTypes.includes(  changedProperty.type ) ) {

            //log.traceOut("onChanged()", "type excluded", changedProperty.type );
            return; 
          }

          if( changesPropertiesSelector.includePropertyTypes != null && 
              !changesPropertiesSelector.includePropertyTypes.includes( changedProperty.type ) ) {

            //log.traceOut("onChanged()", "type not included", changedProperty.type );
            return;
          }
        }

        this._changedProperties.set( changedProperty.key(), changedProperty );

        //log.traceOut("onChanged()", changedProperty.key() );

    } catch (error) {
      log.warn("onChanged()", "Error updating changed properties for database object", error);

      throw new Error( (error as any).message );
    }
  }

  clearChange( changedProperty : DatabaseProperty<any> ): boolean {

    return this._changedProperties.delete( changedProperty.key() ); 
  }


  setDatabaseAccess( databaseAccess : DatabaseAccess | undefined ) : void {

      //log.traceIn("setDatabaseAccess()");

      try { 
        this._databaseAccess = databaseAccess;

        const properties = this.properties();

        properties.forEach( property => {
  
          if( property.type === PropertyTypes.Subdocument ) {
  
              const subdocumentProperty = property as SubdocumentProperty<DatabaseSubdocument>;
  
              if( subdocumentProperty.subdocument() != null ) {

                subdocumentProperty.subdocument()!.setDatabaseAccess( databaseAccess ); 
              }
          }
          else if( property.type === PropertyTypes.Subdocuments ) {
          
            const subdocumentsProperty = property as SubdocumentsProperty<DatabaseSubdocument>;
  
            const subdocuments = subdocumentsProperty.subdocuments();
  
            if( subdocuments != null ) {
  
              for( const subdocument of subdocuments.values() ) {
  
                subdocument.setDatabaseAccess( databaseAccess ); 
              }
            }
          }

        });

        //log.traceOut("clearChanged()", result);
  
      } catch (error) {
        log.warn("Error clearing changed properties for database object", error);
  
        throw new Error( (error as any).message );
      }
  }

  compareTo( other? : DatabaseObject ) : number {

      try {
        //log.traceIn( "compareTo()" ); 

        if( other == null ) {
          return 1;
        }

        const properties = Array.from( this.properties() );

        const otherProperties =  Array.from( other.properties() );

        if( properties.length !== otherProperties.length ) {

          return properties.length - otherProperties.length;
        }

        for( let i = 0; i < properties.length; i++ ) {

          const databaseProperty = properties[i][1];

          const otherDatabaseProperty = otherProperties[i][1];

          let compare = databaseProperty.type.localeCompare( otherDatabaseProperty.type );

          if( compare !== 0 ) {
            //log.traceOut( "compareTo()", "different types", {compare} );
            return compare;
          }

          if (databaseProperty.type === PropertyTypes.Subdocument) {

            const subdocumentProperty = databaseProperty as SubdocumentProperty<DatabaseSubdocument>;

            const otherSubdocumentProperty = otherDatabaseProperty as SubdocumentProperty<DatabaseSubdocument>;

            compare = subdocumentProperty.compareTo( otherSubdocumentProperty );

            if( compare !== 0 ) {
              //log.traceOut( "compareTo()", "different subdocument", {compare} );
              return compare;
            }
          } 
          else if( databaseProperty.type === PropertyTypes.Subdocuments ) {

            const subdocumentsProperty = databaseProperty as SubdocumentsProperty<DatabaseSubdocument>;

            const otherSubdocumentsProperty = otherDatabaseProperty as SubdocumentsProperty<DatabaseSubdocument>;

            compare = subdocumentsProperty.compareTo( otherSubdocumentsProperty );

            if( compare !== 0 ) {
              //log.traceOut( "compareTo()", "different subdocument", {compare} );
              return compare;
            }
          }
          else {

            compare = databaseProperty.compareTo( otherDatabaseProperty );

            if( compare !== 0 ) {
              //log.traceOut( "compareTo()",  "different values", {compare} );
              return compare;
            }
          }
        }

        //log.traceOut( "compareTo()", "0" );
        return 0;

      } catch (error) {
  
        log.warn("compareTo()", "Error comparing objects", error);
  
        throw new Error( (error as any).message );
      }
  }


  async onCreate(): Promise<void> {
    try {
      //log.traceIn( "onCreate()" );

      for (const property of this.properties().values()) {

        if (property.type === PropertyTypes.Subdocument) {

          const subdocumentProperty = property as SubdocumentProperty<DatabaseSubdocument>;

          if( subdocumentProperty.subdocument() != null ) {

            await subdocumentProperty.subdocument()!.onCreate();
          }
        }
        else if( property.type === PropertyTypes.Subdocuments ) {
          
          const subdocumentsProperty = property as SubdocumentsProperty<DatabaseSubdocument>;

          const subdocuments = subdocumentsProperty.subdocuments();

          if( subdocuments != null ) {

            for( const subdocument of subdocuments.values() ) {

              await subdocument.onCreate(); 
            }
          }
        }
        else {
          await (property as AbstractDatabaseProperty<any>).onCreate();
        }
      }

      //log.traceOut( "onCreate()" );

    } catch (error) {

      log.warn("onCreate()", "Error handling create notification", error);

      throw new Error( (error as any).message );
    }
  }

  async onUpdate(): Promise<void> {
    try {
      //log.traceIn( "onUpdate()" );

      for (const property of this.properties().values()) {

        if (property.type === PropertyTypes.Subdocument) {

          const subdocumentProperty = property as SubdocumentProperty<DatabaseSubdocument>;

          if( subdocumentProperty.subdocument() != null ) {

            await subdocumentProperty.subdocument()!.onUpdate();
          }
        }
        else if( property.type === PropertyTypes.Subdocuments ) {
          
          const subdocumentsProperty = property as SubdocumentsProperty<DatabaseSubdocument>;

          const subdocuments = subdocumentsProperty.subdocuments();

          if( subdocuments != null ) {

            for( const subdocument of subdocuments.values() ) {

              await subdocument.onUpdate(); 
            }
          }
        }
        else {
          await (property as AbstractDatabaseProperty<any>).onUpdate();
        }
      }

      //log.traceOut( "onUpdate()" );

    } catch (error) {

      log.warn("onUpdate()", "Error handling update notification", error);

      throw new Error( (error as any).message );
    }
  }

  async onDelete(): Promise<void> {

    try {
      //log.traceIn( "onDelete()" );

      for (const property of this.properties().values()) {

        if (property.type === PropertyTypes.Subdocument) {

          const subdocumentProperty = property as SubdocumentProperty<DatabaseSubdocument>;
          
          if( subdocumentProperty.subdocument() != null ) {

            await subdocumentProperty.subdocument()!.onDelete();
          }
        }
        else if( property.type === PropertyTypes.Subdocuments ) {
          
          const subdocumentsProperty = property as SubdocumentsProperty<DatabaseSubdocument>;

          const subdocuments = subdocumentsProperty.subdocuments();

          if( subdocuments != null ) {

            for( const subdocument of subdocuments.values() ) {

              await subdocument.onDelete(); 
            }
          }
        }
        else {
          await (property as AbstractDatabaseProperty<any>).onDelete();
        }
      }

      //log.traceOut( "onDelete()" );

    } catch (error) {

      log.warn("onDelete()", "Error handling delete notification", error);

      throw new Error( (error as any).message );
    }
  }


  async onRead(): Promise<void> {

    try {
      //log.traceIn( "onRead()" );

      for (const property of this.properties().values()) {

        if (property.type === PropertyTypes.Subdocument) {

          const subdocumentProperty = property as SubdocumentProperty<DatabaseSubdocument>;

          if( subdocumentProperty.subdocument() != null ) {

            await (subdocumentProperty.subdocument() as GenericDatabaseSubdocument)!.onRead();
          }
        }
        else if( property.type === PropertyTypes.Subdocuments ) {
          
          const subdocumentsProperty = property as SubdocumentsProperty<DatabaseSubdocument>;

          const subdocuments = subdocumentsProperty.subdocuments();

          if( subdocuments != null ) {

            for( const subdocument of subdocuments.values() ) {

              await (subdocument as GenericDatabaseSubdocument).onRead(); 
            }
          }
        }
        else {
          await (property as AbstractDatabaseProperty<any>).onRead();
        }
      }

      this._isLoaded = true;

      //log.traceOut( "onRead()" );

    } catch (error) {

      log.warn("onRead()", "Error handling read notification", error);

      throw new Error( (error as any).message );
    }
  }

  async onCreated(): Promise<void> {
    try {
      //log.traceIn( "onCreated()" );

      for (const property of this.properties().values()) {

        if (property.type === PropertyTypes.Subdocument) {

          const subdocumentProperty = property as SubdocumentProperty<DatabaseSubdocument>;

          if( subdocumentProperty.subdocument() != null ) {

            await subdocumentProperty.subdocument()!.onCreated();
          }
        }
        else if( property.type === PropertyTypes.Subdocuments ) {
          
          const subdocumentsProperty = property as SubdocumentsProperty<DatabaseSubdocument>;

          const subdocuments = subdocumentsProperty.subdocuments();

          if( subdocuments != null ) {

            for( const subdocument of subdocuments.values() ) {

              await subdocument.onCreated(); 
            }
          }
        }
        else {
          await (property as AbstractDatabaseProperty<any>).onCreated();
        }
      }

      this._isLoaded = true;

      //log.traceOut( "onCreated()" );

    } catch (error) {

      log.warn("onCreated()", "Error handling created notification", error);

      throw new Error( (error as any).message );
    }
  }

  async onUpdated(): Promise<void> {
    try {
      //log.traceIn( "onUpdated()" );

      for (const property of this.properties().values()) {

        if (property.type === PropertyTypes.Subdocument) {

          const subdocumentProperty = property as SubdocumentProperty<DatabaseSubdocument>;

          if( subdocumentProperty.subdocument() != null ) {

            await subdocumentProperty.subdocument()!.onUpdated();
          }
        }
        else if( property.type === PropertyTypes.Subdocuments ) {
          
          const subdocumentsProperty = property as SubdocumentsProperty<DatabaseSubdocument>;

          const subdocuments = subdocumentsProperty.subdocuments();

          if( subdocuments != null ) {

            for( const subdocument of subdocuments.values() ) {

              await subdocument.onUpdated(); 
            }
          }
        }
        else {
          await (property as AbstractDatabaseProperty<any>).onUpdated();
        }
      }

      this._isLoaded = true;

      //log.traceOut( "onUpdated()" );

    } catch (error) {

      log.warn("onUpdated()", "Error handling updated notification", error);

      throw new Error( (error as any).message );
    }
  }

  async onDeleted(): Promise<void> {

    try {
      //log.traceIn( "onDeleted()" );

      for (const property of this.properties().values()) {

        if (property.type === PropertyTypes.Subdocument) {

          const subdocumentProperty = property as SubdocumentProperty<DatabaseSubdocument>;

          if( subdocumentProperty.subdocument() != null ) {

            await subdocumentProperty.subdocument()!.onDeleted();
          }
        }
        else if( property.type === PropertyTypes.Subdocuments ) {
          
          const subdocumentsProperty = property as SubdocumentsProperty<DatabaseSubdocument>;

          const subdocuments = subdocumentsProperty.subdocuments();

          if( subdocuments != null ) {

            for( const subdocument of subdocuments.values() ) {

              await subdocument.onDeleted();  
            }
          }
        }
        else {
          await (property as AbstractDatabaseProperty<any>).onDeleted();
        }
      }
      //log.traceOut( "onDeleted()" );

    } catch (error) {

      log.warn("onDeleted()", "Error handling deleted notification", error);

      throw new Error( (error as any).message );
    }
  }

  abstract path() : string | undefined;

  abstract uri() : string | undefined;

  abstract referenceHandle() : ReferenceHandle<DatabaseDocument> | undefined;

  abstract ownerId(collectionName?: string): string | undefined;

  abstract ownerIds(collectionName?: string): string[] | undefined; 

  abstract symbolicOwnerIds(collectionName?: string): string[] | undefined; 

  abstract ownerPath(collectionName?: string): string | undefined;

  abstract ownerPaths(collectionName?: string): string[] | undefined;

  abstract emptyOwnerDocument(collectionName?: string): DatabaseDocument | undefined;

  abstract emptyOwnerDocuments(collectionName?: string): DatabaseDocument[] | undefined;

  abstract ownerDocument(collectionName?: string): Promise<DatabaseDocument | undefined>;

  abstract ownerDocuments(collectionName?: string): Promise<DatabaseDocument[] | undefined>;

  abstract ownerCollection(collectionName?: string): CollectionDatabase<DatabaseDocument> | undefined;

  abstract ownerCollections(collectionName?: string): CollectionDatabase<DatabaseDocument>[] | undefined;

  abstract ownerCollectionGroup(collectionName?: string): CollectionGroupDatabase<DatabaseDocument> | undefined;

  abstract ownerCollectionGroups(collectionName?: string): CollectionGroupDatabase<DatabaseDocument>[] | undefined;

  abstract parentDatabases( collectionName : string, 
    options? : { 
        nearestIsCollectionGroup? : boolean, 
        includeRootCollection? : boolean }   ) : Database<DatabaseDocument>[] | undefined;

  abstract parentCollection(collectionName: string): CollectionDatabase<DatabaseDocument> | undefined;

  abstract parentCollections(collectionName: string): CollectionDatabase<DatabaseDocument>[] | undefined;

  abstract parentCollectionGroup(collectionName: string): CollectionGroupDatabase<DatabaseDocument> | undefined;

  abstract parentCollectionGroups(collectionName: string): CollectionGroupDatabase<DatabaseDocument>[] | undefined;

  abstract parentCollectionProperty(collectionName: string): CollectionProperty<DatabaseDocument> | undefined;

  abstract parentCollectionProperties(collectionName: string): CollectionProperty<DatabaseDocument>[] | undefined;


  abstract recordName(): string;   

  abstract databaseAccess(): DatabaseAccess;

  readonly parent? : DatabaseObject;

  readonly id : TextProperty;

  readonly name : TextProperty;

  readonly title: TextProperty;

  protected _databaseAccess? : DatabaseAccess;

  private _isLoaded : boolean;

  private readonly _changedProperties = new Map<string,DatabaseProperty<any>>()

}
