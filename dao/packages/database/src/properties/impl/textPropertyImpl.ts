import { log } from "../../core/base/abstractDatabaseService";
import { DatabaseDocument } from "../../core/spec/databaseDocument";
import { DatabaseObject } from "../../core/spec/databaseObject";
import { TextProperty } from "../spec/textProperty";
import { PropertyType, PropertyTypes } from "../../core/defs/propertyType";
import { DefaultTextType, TextType } from "../../core/defs/textType";
import { OptionsReference } from "../../core/impl/optionsReference";
import { AbstractBasicProperty } from "./abstractBasicProperty";
import { TextsProperty } from "../spec/textsProperty";

export class TextPropertyImpl extends AbstractBasicProperty<string> implements TextProperty {

    constructor( parent : DatabaseObject, textType? : TextType, defaultValue? : string ) {
        super( parent, PropertyTypes.Text as PropertyType, defaultValue ); 

        this.textType = textType != null ? textType : DefaultTextType;
    }

    setOnOptions( onOptions : () => Promise<string[] | undefined> ) {

        this._onOptions = onOptions;
    }

    setOptionsReferenceProperty( optionsReferenceProperty : TextsProperty ) {

        this._optionsReferenceProperty = optionsReferenceProperty;
    } 

    setOptionsReference( optionsReference :  OptionsReference<DatabaseDocument,TextsProperty> ) {
        this._optionsReference = optionsReference;
    }  

    requiresSelect() : boolean {
        return this._onOptions != null ||
            this._optionsReferenceProperty != null || 
            this._optionsReference != null;
    }

    setValue( value : string | undefined ): void {

        if( value != null && typeof value !== "string" ) {
            throw new Error( "Value is not a string: " + value );
        }

        super.setValue( value ); 
    }

    async select( params? : { filterValue? : boolean }) : Promise<Map<string,string> | undefined> { 

        log.traceIn( "select()" );

        let options : Map<string,string> | undefined;

        if( this._onOptions != null ) {

            const optionsArray = await this._onOptions();
            if( optionsArray != null ) {
                for( const option of optionsArray ) {

                    if( options == null ) {
                        options = new Map<string,string>();
                    }
                    
                    options.set( option as string, option ); 
                }
            }
        }
        else {
            if( this._optionsReferenceProperty == null ) {

                this._optionsReferenceProperty = this._optionsReference != null ? 
                    await this._optionsReference.optionsReferenceProperty() : 
                    undefined;
            }

            options = await this._optionsReferenceProperty?.options();
        }

        if( options == null ) {

            log.traceOut( "select()", "no options" ); 
            return undefined;    
        }

        const value = this.value();

        const result = new Map<string,string>();

        for( const option of options.values() ) {

            if( !!params?.filterValue && value != null && value === option ) {
                continue;
            }
            result.set( option, option );
        }

        log.traceOut( "select()", result.size );
        return result.size === 0 ? undefined : result;  
    }


    compareTo( other : TextProperty ) : number {

       return this.compareValue( other.value() );
    }

    compareValue( otherValue : string | undefined ) : number {

        const value = this.value();

        if( value == null && otherValue == null ) {
            return 0;
        }

        if( value != null && otherValue == null ) {
            return 1;
        }

        if( value == null && otherValue != null ) {
            return -1;
        }

        return this.value()!.toString().localeCompare( otherValue! );
    }

    includes( other : TextProperty ) : boolean {
        return this.includesValue( other.value() );
    }

    includesValue( otherValue : string | undefined ) : boolean {

        const value = this.value();

        return value != null && otherValue != null && value.includes( otherValue ); 
    }

    readonly textType : TextType;

    private _onOptions? : () => Promise<string[] | undefined>;

    private _optionsReferenceProperty? : TextsProperty;

    private _optionsReference? : OptionsReference<DatabaseDocument,TextsProperty>;
}