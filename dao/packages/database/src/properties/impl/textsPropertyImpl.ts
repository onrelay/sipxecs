import { GenericDatabaseDocument } from "../../core/impl/genericDatabaseDocument";
import { AbstractDatabaseProperty } from "../../core/base/abstractDatabaseProperty";
import { log } from "../../core/base/abstractDatabaseService";
import { DatabaseDocument } from "../../core/spec/databaseDocument";
import { DatabaseObject } from "../../core/spec/databaseObject";
import { TextsProperty } from "../spec/textsProperty";
import { PropertyType, PropertyTypes } from "../../core/defs/propertyType";
import { OptionsReference } from "../../core/impl/optionsReference";

export class TextsPropertyImpl extends AbstractDatabaseProperty<string[]> implements TextsProperty {

    constructor( parent : DatabaseObject ) {
        super( parent, PropertyTypes.Texts as PropertyType );  

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

    value( ignoreDefault? : boolean ) {
        return this.values( ignoreDefault );
    } 

    setValue( values : string[] | undefined ) : void {
        this.setValues( values );
    }

    values( ignoreDefault? : boolean ) : string[] | undefined {

        this.decryptData();

        if( this._values != null ) {
            return this._values;
        }
        return !!ignoreDefault ? undefined : this._defaultValues;
    }

    setValues( values : string[] | undefined ) : void {

        this.decryptData();

        const oldValues = this._values != null ? Object.assign([], this._values) : undefined;

        if( this.onChange( oldValues, values ) ) { 

            this._values = values;
            
            this.onChanged( oldValues, values ); 
        }
    }

    clearValues(): void {

        this.decryptData();

        const oldValues = this._values != null ? Object.assign([], this._values) : undefined;

        if( this.onChange( oldValues, undefined ) ) {

            delete this._values;

            this.onChanged( oldValues, undefined );
        }
    }

    addValue( value : string ): void { 

        this.decryptData();

        const oldValues = this._values;

        const newValues = this._values == null ? [] as string[]:
            Object.assign([], this._values);

        newValues.push( value );

        if( this.onChange( oldValues, newValues ) ) {

            this._values = newValues;

            this.onChanged( oldValues, newValues );
        }
    }

    removeValue( value : string ): boolean {

        this.decryptData();

        const oldValues = this._values;

        const newValues = this._values == null ? [] as string[]:
            Object.assign([], this._values);

        const index = newValues.indexOf( value );

        if( index !== -1 ) {

            newValues.splice( index, 1 );

            if( this.onChange( oldValues, newValues ) ) {

                this._values = newValues;

                this.onChanged( oldValues, newValues );

                return true;
            }
        }
        return false;
    }

    defaultValue() : string[] | undefined { 

        return this.defaultValues();
    }

    setDefaultValue( defaultValues : string[] | undefined ): void { 

        this.setDefaultValues( defaultValues );
    }

    defaultValues() : string[] | undefined { 

        return this._defaultValues;
    }

    setDefaultValues( defaultValues : string[] | undefined ): void { 

        this._defaultValues = defaultValues != null ? Object.assign([], defaultValues) : undefined; 
    }

    fromRecord( documentData: Record<string, any>): void {

        if( this.isEncryptedData( documentData[this.key()] ) ) {

            this.setEncryptedData( documentData[this.key()] );
        }
        else {

            this._values = documentData[this.key()] as string[]; 
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
            data = this.encryptData( this._values );
        }
        else {
            data = this._values;
        }

        if( data != null ) {
            documentData[this.key()] = data;
        }
    }

    requiresSelect() : boolean {
        return this._optionsReferenceProperty != null || this._optionsReference != null;
    }

    async options() : Promise<Map<string,string> | undefined> {

        try {
            const values = this.values();

            if( values == null ) {
                return undefined;
            }

            const result = new Map<string,string>();

            values.forEach( value => {
                result.set( value, value );
            });

            return result;  
            
        } catch( error ) {
                
            log.warn( "options()", "Error retrieving options", error );
            return undefined;
        }
    }

    async select( params? : {
        filterValues? : boolean }) : Promise<Map<string,string> | undefined> { 

        log.traceIn( "select()" );

        try {

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

            const values = this.values();

            const result = new Map<string,string>(); 

            for( const option of options.values() ) {

                if( !!params?.filterValues && values != null && values.includes( option ) ) {
                    continue;
                }
                result.set( option, option );
            }

            log.traceOut( "select()", result.size );
            return result.size === 0 ? undefined : result;  

        } catch( error ) {

            log.warn( "select()", "Error selecting value", error );
            return undefined;
        } 
    }
    
    compareTo( other : TextsProperty ) : number {

        return this.compareValue( (other as TextsPropertyImpl)._values );

    }

    compareValue( values : string[] | undefined ) : number {

        if( this._values == null && values == null ) {
            return 0;
        }

        if( this._values != null && values == null ) {
            return 1;
        }

        if( this._values == null && values != null ) {
            return -1;
        }

        if( this._values!.length > values!.length ) {
            return 1;
        }

        if( this._values!.length < values!.length ) {
            return -1;
        }

        for( let i = 0; i < values!.length; i++ ) {

            if( !this._values!.includes( values![i]) ) {
                return 1;
            }
        }

        return 0;
    }

    includes( other : TextsProperty, matchAny? : boolean ) : boolean {
        return this.includesValue( other.value(), matchAny );
    }

    includesValue( otherValues : string[] | undefined, matchAny? : boolean  ) : boolean {

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

            const compare = thisValues!.includes( otherValues![i]);

            if( compare && !!matchAny ) {
                return true;
            }

            if( !compare && !matchAny ){
                return false;
            }
        }

        return !matchAny;
    }

    onChange( oldValue : string[] | undefined, newValue : string[] | undefined ) : boolean {

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

    onChanged( oldValue : string[] | undefined,  newValue : string[] | undefined ) : void {

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

    minEntries? : number;

    maxEntries? : number;

    private _onOptions? : () => Promise<string[] | undefined>;

    private _optionsReferenceProperty? : TextsProperty;

    private _optionsReference? : OptionsReference<DatabaseDocument,TextsProperty>;

    protected _values : string[] | undefined;

    protected _defaultValues : string[] | undefined;
}