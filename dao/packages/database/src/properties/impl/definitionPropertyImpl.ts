import { AbstractDatabaseProperty } from "../../core/base/abstractDatabaseProperty";
import { log } from "../../core/base/abstractDatabaseService";
import { DatabaseDocument } from "../../core/spec/databaseDocument";
import { DatabaseObject } from "../../core/spec/databaseObject";
import { DefinitionProperty } from "../spec/definitionProperty";
import { DefinitionsProperty } from "../spec/definitionsProperty";
import { PropertyTypes, PropertyType } from "../../core/defs/propertyType";
import { OptionsReference } from "../../core/impl/optionsReference";

export class DefinitionPropertyImpl<Definition extends string> 
    extends AbstractDatabaseProperty<Definition> implements DefinitionProperty<Definition> {

    constructor( parent : DatabaseObject, 
        definitionName : string, 
        definitions : {},
        defaultValue? : Definition ) {
            
        super( parent, PropertyTypes.Definition as PropertyType );  

        this.definition = definitionName;

        this.definitions = definitions;

        this._defaultValue = defaultValue;
    }

    setOnOptions( onOptions : () => Promise<Definition[] | undefined> ) {

        this._onOptions = onOptions;
    }

    setOptionsReference( optionsReference : OptionsReference<DatabaseDocument,DefinitionsProperty<Definition>>) {
        this._optionsReference = optionsReference;
    }  

    setOptionsReferenceProperty( optionsReferenceProperty : DefinitionsProperty<Definition>) {
        this._optionsReferenceProperty = optionsReferenceProperty;
    }  

    value( ignoreDefault? : boolean ) : Definition | undefined {

        this.decryptData();

        if( this._value != null ) {
            return this._value;
        }
        return !!ignoreDefault ? undefined : this._defaultValue;
    }

    setValue( value : Definition | undefined ): void {

        //log.traceIn( "setValue()", value );

        this.decryptData();

        if( value != null ) {
            if( Object.values( this.definitions ).indexOf( value ) < 0 ) {
                throw new Error( "Invalid definition " + value );
            }
        }

        const oldValue = this._value;

        if( this.onChange( oldValue, value ) ) {

            this._value = value;

            this.onChanged( oldValue, value );
        }

        //log.traceOut( "setValue()", this._value );

    }

    defaultValue() : Definition | undefined {  

        return this._defaultValue;
    }

    setDefaultValue( defaultValue : Definition | undefined ): void { 

        if( defaultValue != null ) {
            if( Object.values( this.definitions ).indexOf( defaultValue ) < 0 ) {
                throw new Error( "Invalid definition " + defaultValue );
            }
        }

        this._defaultValue = defaultValue;
    }

    requiresSelect() : boolean {
        return true;
    }


    async select( params?: { filterValue?: boolean }) : Promise<Map<string,Definition> | undefined> { 

        let options;

        if( this._onOptions != null ) {

            options = await this._onOptions();
        }
        else {

            if( this._optionsReferenceProperty == null ) {

                this._optionsReferenceProperty = this._optionsReference != null ? 
                    await this._optionsReference.optionsReferenceProperty() : 
                    undefined;
            }

            if( this._optionsReferenceProperty == null ||
                (!this._optionsReferenceProperty.required && this._optionsReferenceProperty.value() == null ) ) {

                options = Object.values( this.definitions );  
            }
            else {
                options = (await this._optionsReferenceProperty.options())?.values();
            }
        }

        if( options == null ) {
            log.traceOut( "select()", "no options", undefined ); 
            return undefined;
        }

        const result = new Map<string,Definition>();

        const value = this.value();

        for( const definition of options ) {

            if( !!params?.filterValue && value != null && value === definition as Definition ) {
                continue;
            }
            result.set( definition as string, definition as Definition);
        }

        log.traceOut( "select()", result.size );
        return result.size === 0 ? undefined : result;  
    }

    fromRecord( documentData: Record<string, any>): void {

        if( this.isEncryptedData( documentData[this.key()] ) ) {

            this.setEncryptedData( documentData[this.key()] );            
        }
        else {
            const data = documentData[this.key()];
        
            if( data == null ) {

                this._value = undefined;
                return;
            }

            if ( !(typeof data === 'string') ) {

                log.warn( "Unrecognized type for definition", data );
                this._value = undefined;
                return;
            }

            if ( Object.values( this.definitions ).indexOf( data ) < 0 ) {

                log.warn( "Unrecognized value for definition", data );
                this._value = undefined;
                return;

            } 

            this._value = data as Definition;
        }
        
    }

    async toRecord( documentData: Record<string, any>, force? : boolean ) : Promise<void> {

        if( !!force ) {
            this.decryptData();
        }
        
        if( this.encrypted() && this.encryptedData() != null ) {
            
            documentData[this.key()] = this.encryptedData();
            return;
        }

        let data;
        
        if( this.encrypted() ) {
            data = this.encryptData( this._value )
        }
        else {
            data = this._value;
        }

        if( data != null ) {
            documentData[this.key()] = data;
        }
    }

    compareTo( other : DefinitionProperty<Definition>, translator? : ( value? : string | undefined ) => string ) : number {

        return this.compareValue( other.value(), translator );
    }

    compareValue( value : Definition | undefined, translator? : ( value? : string ) => string | undefined ) : number {

        let thisValue = translator != null ? translator( this.value() as string ) : this.value() as string;
        let otherValue = translator != null ? translator( value as string ) : value as string;

        if( thisValue == null && otherValue  == null ) {
            return 0;
        }

        if( thisValue != null && otherValue  == null ) {
            return 1; 
        }

        if( thisValue == null && otherValue  != null ) {
            return -1;
        }

        return thisValue!.localeCompare( otherValue ! );
    }

    includes( other : DefinitionProperty<Definition>, matchAny? : boolean, translator? : ( value? : string ) => string | undefined ) : boolean {
        return this.includesValue( other.value(), matchAny, translator );
    }

    includesValue( value : Definition | undefined, matchAny? : boolean, translator? : ( value? : string ) => string | undefined ) : boolean {
        return this.compareValue( value, translator ) === 0;
    }


    readonly definition : string;

    readonly definitions : {};

    private _value : Definition | undefined;

    private _defaultValue : Definition | undefined;

    private _onOptions? : () => Promise<Definition[] | undefined>;

    private _optionsReferenceProperty? : DefinitionsProperty<Definition>;  

    private _optionsReference?: OptionsReference<DatabaseDocument,DefinitionsProperty<Definition>>;


}