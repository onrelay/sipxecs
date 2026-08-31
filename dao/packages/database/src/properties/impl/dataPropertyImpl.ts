import { DatabaseRecord } from "../../core/types/databaseRecord";
import { AbstractDatabaseProperty } from "../../core/base/abstractDatabaseProperty";
import { DataProperty } from "../spec/dataProperty";

export class DataPropertyImpl<Data extends Object> extends AbstractDatabaseProperty<Data> implements DataProperty<Data> {

    value() : Data | undefined {

        return this.data();
    }

    setValue( data : Data | undefined ) : void {

        this.setData( data );
    }

    data() : Data | undefined { 

        this.decryptData();

        return this._data;
    }

    setData( data : Data | undefined ): void {

        this.decryptData();

        const oldValue = Object.assign( {}, this._data )

        if( this.onChange( oldValue, data ) ) {

            this._data = data;

            this.onChanged( oldValue, data );
        }
    }

    fromRecord( documentData: DatabaseRecord): void {

        if( this.isEncryptedData( documentData[this.key()] ) ) {

            this.setEncryptedData( documentData[this.key()] );
        }
        else {
            const data = documentData[this.key()];
            
            if( data == null ) {
                this._data = undefined;
            }
            else if (typeof data === 'object' ) {

                this._data = data as Data;
            } 
            else if (typeof data === 'string') {

                try {
                    this._data = JSON.parse( data );
                } catch( error ) {
                    this._data = undefined;
                }
            }
            else {
                this._data = undefined;
            }
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
            data = this.encryptData( this._data )
        }
        else {
            data = this._data;
        }

        if( data != null ) {
            documentData[this.key()] = data;
        }
    }

    compareTo( other : DataProperty<Data> ) : number {

        return this.compareValue( other.value() );
    }


    compareValue( otherData : Data | undefined ) : number {

        const data = this.data();

        if( data == null && otherData == null ) {
            return 0;
        }

        if( data != null && otherData == null ) {
            return 1;
        }

        if( data == null && otherData != null ) {
            return -1;
        }

        return JSON.stringify( data ).localeCompare( JSON.stringify( otherData ));
    }

    includes( other : DataProperty<Data> ) : boolean {
        return this.includesValue( other.value() );
    }


    includesValue( value : Data | undefined ) : boolean {
        return this.compareValue( value ) === 0;
    }

    protected _data : Data | undefined;

    protected _defaultData : Data | undefined;


}