import { DatabaseRecord } from "../../core/types/databaseRecord";
import { GenericDatabaseDocument } from "../../core/impl/genericDatabaseDocument";
import { AbstractDatabaseProperty } from "../../core/base/abstractDatabaseProperty";
import { log } from "../../core/base/abstractDatabaseService";
import { DatabaseDocument } from "../../core/spec/databaseDocument";
import { DatabaseObject } from "../../core/spec/databaseObject";
import { DefinitionsProperty } from "../spec/definitionsProperty";
import { PropertyType, PropertyTypes } from "../../core/defs/propertyType";
import { OptionsReference } from "../../core/impl/optionsReference";

export class DefinitionsPropertyImpl<Definition extends string> 
    extends AbstractDatabaseProperty<Definition[]> implements DefinitionsProperty<Definition> {

    constructor( parent : DatabaseObject, 
        definitionName : string, 
        definitions : {}  ) {

        super( parent, PropertyTypes.Definitions as PropertyType ); 

        this.definition = definitionName;

        this.definitions = definitions; 

    }

    setOnOptions( onOptions : () => Promise<Definition[] | undefined> ) {

        this._onOptions = onOptions;
    }

    setOptionsReferenceProperty( optionsReferenceProperty : DefinitionsProperty<Definition>) {
        this._optionsReferenceProperty = optionsReferenceProperty;
    } 

    setOptionsReference( optionsReference : OptionsReference<DatabaseDocument,DefinitionsProperty<Definition>>) {
        this._optionsReference = optionsReference;
    } 

    value( ignoreDefault? : boolean ) {
        return this.values( ignoreDefault );
    } 

    setValue( values : Definition[] | undefined ) : void {
        this.setValues( values );
    }

    values( ignoreDefault? : boolean ) : Definition[] | undefined {

        this.decryptData();

        if( this._values != null ) {
            return this._values;
        }
        return !!ignoreDefault ? undefined : this._defaultValues;
    }

    setValues( values : Definition[] | undefined ) : void {

        this.decryptData();

        const oldValues = this._values != null ? Object.assign([], this._values) : undefined;

        if( this.onChange( oldValues, values ) ) {

            this._values = values;

            this.onChanged( oldValues, values );
        }
    }

    clearValues(): void {

        const values = this.values();

        if( values != null ) {

            const oldValues = this._values != null ? Object.assign([], this._values) : undefined;

            if( this.onChange( oldValues, undefined ) ) {

                delete this._values;

                this.onChanged( oldValues, undefined );
            }
        }
    }

    addValue( value : Definition ): void {

        if( Object.values( this.definitions ).indexOf( value ) < 0 ) {
            throw new Error( "Invalid definition " + value );
        }

        const values = this.values();

        const oldValues = this._values != null ? Object.assign([], this._values) : undefined;

        if( values == null ) {

            if( this.onChange( oldValues, [value] ) ) {

                this._values = [value];

                this.onChanged( oldValues, [value] );
            }

        }
        else if( values.indexOf( value ) === -1 ) {

            values.push( value );

            if( this.onChange( oldValues, values ) ) {

                this._values = values;

                this.onChanged( oldValues, values );
            }
        }
    }

    removeValue( value : Definition ): boolean {

        if( Object.values( this.definitions ).indexOf( value ) < 0 ) {
            throw new Error( "Invalid definition " + value );
        }

        const values = this.values();

        if( values == null ) {
            return false;
        }

        let index = values.indexOf( value );

        if( index === -1 ) {
            return false;
        }

        const oldValues = this._values != null ? Object.assign([], this._values) : undefined;

        values.splice( index, 1 );

        if( this.onChange( oldValues, values ) ) {

            this._values = values;

            this.onChanged( oldValues, values );

            return true;
        }

        return false;
    }

    defaultValue() : Definition[] | undefined { 

        return this.defaultValues();
    }

    setDefaultValue( defaultValues : Definition[] | undefined ): void { 

        this.setDefaultValues( defaultValues );
    }

    defaultValues() : Definition[] | undefined { 

        return this._defaultValues;
    }

    setDefaultValues( defaultValues : Definition[] | undefined ): void { 

        if( defaultValues != null ) {
            for( const defaultValue of defaultValues ) {
                if( Object.values( this.definitions ).indexOf( defaultValue ) < 0 ) {
                    throw new Error( "Invalid definition " + defaultValue );
                }
            }
        }

        this._defaultValues = defaultValues != null ? Object.assign([], defaultValues) : undefined; 
    }


    requiresSelect() : boolean {
        return true;
    }

    async options() : Promise<Map<string,Definition> | undefined> {

        log.traceInOut( "options()" );

        const values = this.values();

        if( values == null ) {
            return undefined;
        }

        const result = new Map<string,Definition>();

        values.forEach( value => {
            result.set( value, value );
        });

        return result;
    }

    async select( params?: { filterValues?: boolean } ) : Promise<Map<string,Definition> | undefined> { 

        log.traceIn( "select()" );

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

        const values = this.values();

        for( const definition of options ) {

            if( !!params?.filterValues && values != null && values.includes( definition as Definition ) ) {
                continue;
            }
            result.set( definition as string, definition as Definition);
        }

        log.traceOut( "select()", result.size );
        return result.size === 0 ? undefined : result;  
    }

    fromRecord( documentData: DatabaseRecord): void {

        if( this.isEncryptedData( documentData[this.key()] ) ) {

            this.setEncryptedData( documentData[this.key()] );
        }
        else {
            const data = documentData[this.key()];       

            if( data == null ) {
                this._values = undefined;
                return;
            }

            this._values = [];
            
            if( !Array.isArray( data ) ) {
                this._values = undefined;
                return;
            }

            data.forEach( (value : any) => {
                if ( Object.values( this.definitions ).indexOf( value ) < 0 ) {

                    log.warn( "Unrecognized value for definition", value );
                } 
                else {
                    if( this._values!.indexOf( value as Definition ) === -1 ) {
                        this._values!.push( value as Definition );
                    }
                }
            });
        }
    }

    async toRecord( documentData: DatabaseRecord, force? : boolean ) : Promise<void> {

        if( !!force ) {
            this.decryptData();
        }
        
        if( this.encrypted() && this.encryptedData() != null ) {
            
            documentData[this.key()] = this.encryptedData();
            return;
        }

        let data;

        if( this.encrypted() ) {
            data = this.encryptData( this._values );
        }
        else {
            data = this._values;
        }

        if( data != null ) {
            documentData[this.key()] = data;
        }
    }

    compareTo( other : DefinitionsProperty<Definition>, translator? : ( value? : string ) => string | undefined  ) : number {

        return this.compareValue( other.values(), translator );

    }

    compareValue( otherValues: Definition[] | undefined, translator? : ( value? : string ) => string | undefined ) : number {

        const thisValues = this.values();

        if( thisValues == null && otherValues == null ) {
            return 0;
        }

        if( thisValues != null && otherValues == null ) {
            return 1;
        }

        if( thisValues == null && otherValues != null ) {
            return -1;
        }

        if( thisValues!.length > otherValues!.length ) {
            return 1;
        }

        if( thisValues!.length < otherValues!.length ) {
            return -1;
        }
        
        for( let i = 0; i < thisValues!.length; i++ ) {

            let found = false;

            let compare = 1;

            let thisValue = translator != null ? translator( thisValues![i] as string ) : thisValues![i] as string;

            for( let j = 0; j < otherValues!.length; j++ ) {

                let otherValue = translator != null ? translator( otherValues![j] as string ) : otherValues![j] as string;            
                
                compare = thisValue!.localeCompare( otherValue! );

                if( compare === 0 ) {
                    found = true;
                    break;
                }
            }

            if( !found ) {
                return compare;
            }

        }

        return 0;
    }

    includes( other : DefinitionsProperty<Definition>, matchAny? : boolean, translator? : ( value? : string ) => string | undefined ) : boolean {
        return this.includesValue( other.value(), matchAny, translator );
    }

    includesValue( otherValues : Definition[] | undefined, matchAny? : boolean, translator? : ( value? : string ) => string | undefined ) : boolean {

        const thisValues = this.values();

        if( otherValues == null || otherValues.length === 0 ) {
            return false;
        }

        if( thisValues == null || thisValues.length === 0 ) {
            return false;
        }

        if( thisValues != null && otherValues == null ) {
            return false;
        }

        if( thisValues == null && otherValues != null ) {
            return false;
        }

        if( thisValues!.length < otherValues!.length && !matchAny) {
            return false;
        }


        for( let i = 0; i < otherValues!.length; i++ ) {

            let otherValue = translator != null ? translator( otherValues![i] as string ) : otherValues![i] as string;

            let found = false;
            for( let j = 0; j < thisValues!.length; j++ ) {

                let thisValue = translator != null ? translator( thisValues![j] as string ) : thisValues![j] as string;            
                
                const compare = thisValue!.localeCompare( otherValue! );

                if( compare === 0 ) {
                    if( !!matchAny ) {
                        return true;
                    }
                    found = true;
                    break;
                }
            }
            if( !found && !matchAny ) {
                return false;
            }
        }

        return !matchAny;
    }

    
    onChange( oldValue : Definition[] | undefined, newValue : Definition[] | undefined ) : boolean {

        if( this.minEntries != null && newValue != null && newValue.length < this.minEntries ) {

            this.error = new Error( "propertyValueRejected" );

            if( oldValue == null || newValue.length >= oldValue.length ) {
                return true;
            }
            else {
                return false;
            }
        }

        if( this.maxEntries != null && newValue != null && newValue.length > this.maxEntries ) {
            
            this.error = new Error( "propertyValueRejected" );
            return false;
        }

        return super.onChange( oldValue, newValue );
    }

    onChanged( oldValue : Definition[] | undefined,  newValue : Definition[] | undefined ) : void {

        try {
            //log.traceIn( "onChanged()", {oldValue}, {newValue} );

           this.setPreviousValues( this.previousValues() != null ? 
                this.previousValues().concat(newValue) : [newValue] );

            if( this.minEntries != null && newValue != null && newValue.length >= this.minEntries ) { 

                delete this.error;
            }
 
            (this.parent as GenericDatabaseDocument).onChanged( this, oldValue, newValue ); // Note this is async, so will not wait for processing / block

            //log.traceOut( "onChanged()", {oldValue}, {newValue} ); 
            
        } catch( error ) {

            log.warn( "onChanged()", "Error checking change", error );
            
            throw new Error("Error checking change: " + (error as any).message );
        } 
    }


    readonly definition : string;

    readonly definitions : {};

    minEntries? : number;

    maxEntries? : number;

    protected _values : Definition[] | undefined;

    private _defaultValues : Definition[] | undefined;

    private _onOptions? : () => Promise<Definition[] | undefined>;

    private _optionsReferenceProperty? : DefinitionsProperty<Definition>;

    private _optionsReference? : OptionsReference<DatabaseDocument,DefinitionsProperty<Definition>>;

}