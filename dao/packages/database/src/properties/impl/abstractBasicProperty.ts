import { DatabaseRecord } from "../../core/types/databaseRecord";
import { AbstractDatabaseProperty } from "../../core/base/abstractDatabaseProperty";
import { DatabaseObject } from "../../core/spec/databaseObject";
import { BasicProperty } from "../spec/basicProperty";
import { PropertyType } from "../../core/defs/propertyType";

export abstract class AbstractBasicProperty<BasicType extends string | number | boolean> 
    extends AbstractDatabaseProperty<BasicType> implements BasicProperty<BasicType> {

    constructor( parent : DatabaseObject, type : PropertyType, defaultValue? : BasicType) {
        super( parent, type ); 

        this._defaultValue = defaultValue;
    }


    value( ignoreDefault? : boolean ) : BasicType | undefined {

        this.decryptData();

        if( this._value != null ) {
            return this._value;
        }
        return !!ignoreDefault ? undefined : this._defaultValue;
    }

    setValue( value : BasicType | undefined ): void { 

        this.decryptData();

        const oldValue = this._value;

        if( this.onChange( oldValue, value ) ) {

            this._value = value;

            this.onChanged( oldValue, value ); 
        }
    }


    defaultValue() : BasicType | undefined {

        return this._defaultValue;
    }

    setDefaultValue( defaultValue : BasicType | undefined ): void {
        this._defaultValue = defaultValue;
    }


    fromRecord( documentData: DatabaseRecord): void {

        if( this.isEncryptedData( documentData[this.key()] ) ) {

            this.setEncryptedData( documentData[this.key()] );
        }
        else {
            this._value = documentData[this.key()] as BasicType | undefined;
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

            data = this.encryptData( this._value )
        }
        else {
            data = this._value;
        }

        if( data != null ) {
            documentData[this.key()] = data;
        }
    }

    includes( other : BasicProperty<BasicType> ) : boolean {
        return this.includesValue( other.value() );
    }


    includesValue( value : BasicType | undefined ) : boolean {
        return this.compareValue( value ) === 0;
    }

    protected _value : BasicType | undefined;

    protected _defaultValue : BasicType | undefined;


}