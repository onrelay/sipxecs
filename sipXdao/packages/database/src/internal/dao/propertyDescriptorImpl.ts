import { MediaType, MediaTypeName, MediaTypes } from "@sipxdao/storage";
import { log } from "../../api/dao/abstractDatabaseService";
import { GenericDatabaseSubdocument } from "../../api/dao/genericDatabaseSubdocument";
import { DatabaseProperty } from "../../api/dao/databaseProperty";
import { DatabaseObject } from "../../api/dao/databaseObject";
import { PropertyDescriptor, PropertyDescriptorName } from "../../api/dao/propertyDescriptor";
import { BasicPropertyTypes, PropertyType, PropertyTypeName, PropertyTypes, StandardPropertyTypes } from "../../api/types/propertyType";
import { TextType, TextTypeName, TextTypes } from "../../api/types/textType";
import { AttachmentsPropertyImpl } from "../properties/attachmentsPropertyImpl";
import { BooleanPropertyImpl } from "../properties/booleanPropertyImpl";
import { ConfirmationPropertyImpl } from "../properties/confirmationPropertyImpl";
import { CountryPropertyImpl } from "../properties/countryPropertyImpl";
import { DatePropertyImpl } from "../properties/datePropertyImpl";
import { DefinitionPropertyImpl } from "../properties/definitionPropertyImpl";
import { DefinitionsPropertyImpl } from "../properties/definitionsPropertyImpl";
import { EmptyPropertyImpl } from "../properties/emptyPropertyImpl";
import { GeolocationPropertyImpl } from "../properties/geolocationPropertyImpl";
import { ImagePropertyImpl } from "../properties/imagePropertyImpl";
import { LinksPropertyImpl } from "../properties/linksPropertyImpl";
import { LongTextPropertyImpl } from "../properties/longTextPropertyImpl";
import { NumberPropertyImpl } from "../properties/numberPropertyImpl";
import { PhoneNumberPropertyImpl } from "../properties/phoneNumberPropertyImpl";
import { ReferencePropertyImpl } from "../properties/referencePropertyImpl";
import { ReferencesPropertyImpl } from "../properties/referencesPropertyImpl";
import { TextPropertyImpl } from "../properties/textPropertyImpl";
import { TextsPropertyImpl } from "../properties/textsPropertyImpl";
import { DefinitionProperty } from "../../api/properties/definitionProperty";
import { LongTextProperty } from "../../api/properties/LongTextProperty";
import { TextsProperty } from "../../api/properties/textsProperty";
import { NumberProperty } from "../../api/properties/numberProperty";
import { BooleanProperty } from "../../api/properties/booleanProperty";


export class PropertyDescriptorImpl<Property extends DatabaseProperty<any>> extends GenericDatabaseSubdocument implements PropertyDescriptor<Property> { 

    constructor( parent : DatabaseObject, key : string ) {   

        super( PropertyDescriptorName, parent, key );   

        try {
            this.title.required = true;

            this.propertyType = new DefinitionPropertyImpl<PropertyType>( 
                this, 
                PropertyTypeName, 
                {...BasicPropertyTypes,...StandardPropertyTypes} ); 

            this.propertyType.required = true;

            //log.traceInOut( "constructor()", parent.title, key );

        } catch( error ) { 

            log.warn( "constructor()", "Error initializing settings data", error );
            
            throw new Error( (error as any).message );
        }
    }
    
    newProperty( owner : DatabaseObject, propertyType : PropertyType, applyRestrictions? : boolean ) : DatabaseProperty<any> {

        //log.traceIn( "newProperty()" );

        try {

            let property : DatabaseProperty<any>;

            switch( propertyType ) { 

                case PropertyTypes.Confirmation:

                    property = new ConfirmationPropertyImpl( owner, this.defaultValue?.value() ); 
                    break;

                case PropertyTypes.Text:

                    property = new TextPropertyImpl( owner, this.textType?.value(), this.defaultValue?.value() ); 
                    break;

                case PropertyTypes.LongText:

                    property = new LongTextPropertyImpl( owner, this.defaultValue?.value(),  );
                    break;

                case PropertyTypes.Number:
                    property = new NumberPropertyImpl( owner, this.defaultValue?.value(), this.minValue?.value(), this.maxValue?.value() );
                    break;

                case PropertyTypes.Boolean:
                    property = new BooleanPropertyImpl( owner, this.defaultValue?.value() );
                    break;

                case PropertyTypes.PhoneNumber:
                    property = new PhoneNumberPropertyImpl( owner );
                    break;

                case PropertyTypes.Country: 
                    property = new CountryPropertyImpl( owner );
                    break;

                case PropertyTypes.Texts: 

                    property = new TextsPropertyImpl( owner );
                    break;
           
                case PropertyTypes.Definition: 

                    property = new DefinitionPropertyImpl( owner, this.title.value()!, this.options!.value()!, this.defaultValue?.value()  );
                    break;

                case PropertyTypes.Definitions: 

                    property = new DefinitionsPropertyImpl( owner, this.title.value()!, this.options!.value()! );
                    break;

                case PropertyTypes.Reference: 

                    property = new ReferencePropertyImpl( owner );
                    break;

                case PropertyTypes.References: 

                    property = new ReferencesPropertyImpl( owner );
                    break;

                case PropertyTypes.Date: 

                    property = new DatePropertyImpl( owner ); 
                    break;

                case PropertyTypes.Image: 

                    property = new ImagePropertyImpl( owner );
                    break;

                case PropertyTypes.Geolocation: 

                    property = new GeolocationPropertyImpl( owner );
                    break;

                case PropertyTypes.Attachments: 

                    property = new AttachmentsPropertyImpl( owner, this.mediaType!.value()! );
                    break;
                
                case PropertyTypes.Links: 

                    property = new LinksPropertyImpl( owner );
                    break;

                case PropertyTypes.Empty:  
                default:
                    property = new EmptyPropertyImpl( owner ); 
                    break;
            }

            if( !!applyRestrictions ) {

                property.required = !this.propertyDisabled?.value() && !!this.propertyRequired?.value();
            }

            property.prompt = this.prompt?.value();

            property.help = this.help?.value(); 

            //log.traceOut( "newProperty()" ); 
            return property;

        } catch( error ) { 

            log.warn( "newProperty()", "Error creating new property", error );
            
            throw new Error( (error as any).message );
        }
    }

    onChange( changingProperty : DatabaseProperty<any>, oldValue : any, newValue : any ): boolean {

        //log.traceIn( "onChange()" ); 

        try {

            let allowChange = super.onChange( changingProperty, oldValue, newValue );

            if( !allowChange ) {

                //log.traceOut( "onChange()", "rejected by super" ); 
                return false;
            }

            if( changingProperty === this.propertyType ) {

                if( oldValue != null && newValue != null ) {

                    // Clear previous properties first
                    this.updateProperties( undefined );
                }

                this.updateProperties( newValue as PropertyType );

                //log.traceOut( "onChange()", "not property type or text type, allowed by super", true ); 
                return true;
            }

            if( changingProperty === this.textType ) {

                this.updateTextType( newValue as TextType );

                //log.traceOut( "onChange()", "not property type or text type, allowed by super", true ); 
                return true;
            }

            //log.traceOut( "onChange()", "OK" ); 
            return true;

        } catch( error ) { 

            log.warn( "onChange()", "Error handling property change", error );
            
            throw new Error( (error as any).message );
        }
    }


    fromRecord(record: Record<string, any>): void {
        log.traceIn("fromRecord()", record);
    
        try {

          super.fromRecord( record );  

          this.updateProperties( this.propertyType.value(), record );
    
          log.traceOut("fromRecord()", this);
    
        } catch (error) {
    
          log.warn("fromRecord()", "Error reading document", error);
    
          throw new Error( (error as any).message );
        }
    }

    protected async updateProperties( propertyType? : PropertyType, data? : any )  : Promise<void> {

        //log.traceIn("updateProperties()", {propertyType}, {data} );
    
        try {

            if( propertyType != null ) {
                        
                switch( propertyType ) {

                    case PropertyTypes.Confirmation:

                        this.prompt = new LongTextPropertyImpl( this );
                        this.defaultValue = this.newProperty( this, propertyType );
                        break;
    
                    case PropertyTypes.Text:

                        this.help = new LongTextPropertyImpl( this );
                        this.textType = new DefinitionPropertyImpl( this, TextTypeName, TextTypes );
                        this.defaultValue = this.newProperty( this, propertyType );
                        break;
    
                    case PropertyTypes.LongText:
    
                        this.help = new LongTextPropertyImpl( this );
                        this.defaultValue = this.newProperty( this, propertyType );
                        break;
    
                    case PropertyTypes.Number:
                        this.defaultValue = this.newProperty( this, propertyType );
                        this.minValue = this.newProperty( this, propertyType );
                        this.maxValue = this.newProperty( this, propertyType  );
                        break;
    
                    case PropertyTypes.Boolean:
                        this.defaultValue = this.newProperty( this, propertyType );
                        break;
    
                    case PropertyTypes.PhoneNumber:
                        break;
    
                    case PropertyTypes.Country: 
                        this.defaultValue = this.newProperty( this, propertyType );
                        break;
    
                    case PropertyTypes.Texts: 
                        this.defaultValue = this.newProperty( this, propertyType );
                        this.minEntries = new NumberPropertyImpl( this ); 
                        this.maxEntries = new NumberPropertyImpl( this ); 
                        break;
                        
                    case PropertyTypes.Definition: 
    
                        this.defaultValue = new TextPropertyImpl( this );
                        this.options = new TextsPropertyImpl( this );
                        (this.defaultValue as TextPropertyImpl).setOptionsReferenceProperty( this.options! );
                        break;
    
                    case PropertyTypes.Definitions:  
                        this.options = new TextsPropertyImpl( this );
                        this.minEntries = new NumberPropertyImpl( this ); 
                        this.maxEntries = new NumberPropertyImpl( this ); 
                        break;
    
                    case PropertyTypes.Date: 
    
                        this.help = new LongTextPropertyImpl( this );this.defaultValue = this.newProperty( this, propertyType );
                        break;
    
                    case PropertyTypes.Image: 
    
                        break;
    
                    case PropertyTypes.Geolocation: 
    
                        this.defaultValue = this.newProperty( this, propertyType );
                        break;
                        
                    case PropertyTypes.Attachments:     
                        this.mediaType = new DefinitionPropertyImpl( this, MediaTypeName, MediaTypes ); 
                        this.mediaType.required = true;
                        this.minEntries = new NumberPropertyImpl( this ); 
                        this.maxEntries = new NumberPropertyImpl( this ); 
                        break;
                    
                    case PropertyTypes.Links:  
                        this.minEntries = new NumberPropertyImpl( this ); 
                        this.maxEntries = new NumberPropertyImpl( this );    
                        break;
    
                    case PropertyTypes.References:  
                        this.minEntries = new NumberPropertyImpl( this ); 
                        this.maxEntries = new NumberPropertyImpl( this );    
                        break;

                    case PropertyTypes.Empty:  
                    default:
                        break;
                }

                if( this.options != null && data != null ) {
                    this.options.fromRecord( data );
                }

                if( this.textType != null && data != null ) {
                    this.textType.fromRecord( data );

                    this.updateTextType( this.textType.value() );
                }

                if( this.mediaType != null && data != null ) {
                    this.mediaType.fromRecord( data );
                }

                if(  this.minValue != null && data != null ) {
                    this.minValue.fromRecord( data );
                }

                if( this.maxValue != null && data != null ) {
                    this.maxValue.fromRecord( data );
                }

                if(  this.minEntries != null && data != null ) {
                    this.minEntries.fromRecord( data );
                }

                if( this.maxEntries != null && data != null ) {
                    this.maxEntries.fromRecord( data );
                }

                if( this.defaultValue != null && data != null ) {                   
                    this.defaultValue.fromRecord( data );
                }

                if( this.prompt != null && data != null ) {
                    this.prompt.fromRecord( data );
                }

                if( this.help != null && data != null ) {
                    this.help.fromRecord( data );
                }

                this.propertyDisabled = new BooleanPropertyImpl( this );
                if( data != null ) {
                    this.propertyDisabled.fromRecord( data );
                }

                this.propertyRequired = new BooleanPropertyImpl( this );  
                if( data != null ) {
                    this.propertyRequired.fromRecord( data );
                }

            }
            else {
                delete this.propertyDisabled;
                delete this.propertyRequired;
                delete this.textType;
                delete this.mediaType;
                delete this.options;
                delete this.minValue;
                delete this.maxValue;                
                delete this.minEntries;
                delete this.maxEntries;
                delete this.defaultValue;
                delete this.prompt;
                delete this.help;
            }
    
            //log.traceOut("updateProperties()");  
    
        } catch (error) {
    
          log.warn("updateProperties()", "Error updating properties", error);
    
          throw new Error( (error as any).message );
        }
    }

    protected updateTextType( textType? : TextType )  : void {

        log.traceIn("updateTextType()", {textType} );
    
        try {

            if( (this.defaultValue as TextPropertyImpl).textType === textType ) {

                log.traceOut("updateTextType()", "no change" );
                return;
            }

            if( textType === TextTypes.Password ) {

                delete this.defaultValue;

                log.traceOut("updateTextType()", "changed to password" );
                return;
            }

            const value = this.defaultValue?.value();

            this.defaultValue = new TextPropertyImpl( this, textType );

            this.defaultValue.setValue( value );
            
            log.traceOut("updateTextType()", "changed to", {textType} );

        } catch (error) {
    
            log.warn("updateTextType()", "Error updating text type", error);
      
            throw new Error( (error as any).message );
        }
    }

    readonly propertyType  : DefinitionProperty<PropertyType>; 

    textType?  : DefinitionProperty<TextType>; 

    mediaType?  : DefinitionProperty<MediaType>; 

    prompt? : LongTextProperty;

    help? : LongTextProperty;

    options? : TextsProperty;    

    minValue? : DatabaseProperty<any>;  

    maxValue? : DatabaseProperty<any>;  

    minEntries? : NumberProperty; 

    maxEntries? : NumberProperty;  

    defaultValue? : DatabaseProperty<any>;  

    propertyDisabled? : BooleanProperty;  

    propertyRequired? : BooleanProperty;   
 
 }
