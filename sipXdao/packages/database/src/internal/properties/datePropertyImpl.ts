import { AbstractDatabaseProperty } from "../../api/dao/abstractDatabaseProperty";
import { DatabaseConverter } from "../../api/dao/databaseConverter";
import { DatabaseObject } from "../../api/dao/databaseObject";
import { DateProperty } from "../../api/properties/dateProperty";
import { PropertyTypes, PropertyType } from "../../api/types/propertyType";

export class DatePropertyImpl extends AbstractDatabaseProperty<Date> implements DateProperty {

    constructor( parent : DatabaseObject, defaultDate? : Date ) {
        super( parent, PropertyTypes.Date as PropertyType ); 

        this._defaultDate = defaultDate; 
    } 

    value( ignoreDefault? : boolean ) {
        return this.date( ignoreDefault );
    }

    setValue( date : Date | undefined ) : void {
        this.setDate( date );
    }

    defaultValue() {
        return this.defaultDate();
    }

    setDefaultValue( defaultDate : Date | undefined ) : void {
        this.setDefaultDate( defaultDate ); 
    }

    date( ignoreDefault? : boolean ) : Date | undefined {

        this.decryptData();

        if( this._date != null ) {
            return this._date;
        }
        return !!ignoreDefault ? undefined : this._defaultDate;
    }

    setDate( date : Date | undefined ): void {

        this.decryptData();

        const oldDate = this._date;

        if( this.onChange( oldDate, date ) ) {

            this._date = date;

            this.onChanged( oldDate, date );
        }
    }

    defaultDate() : Date | undefined {

        return this._defaultDate;
    }

    setDefaultDate( defaultDate : Date | undefined ) : void {
        this._defaultDate = defaultDate; 
    }

    fromRecord( documentData: Record<string, any> ) : void {  

        if( this.isEncryptedData( documentData[this.key()] ) ) {

            this.setEncryptedData( documentData[this.key()] );
        }
        else {
            const data = documentData[this.key()];

            if( this._databaseConverter == null ) {

                this._databaseConverter = this.parent.ownerCollection()?.databaseManager.converter;
            }
            
            this._date = this._databaseConverter != null ? 
                this._databaseConverter.toDate( data ) : undefined;

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
        
        if( this._databaseConverter == null ) {

            this._databaseConverter = this.parent.ownerCollection()?.databaseManager.converter;
        }

        const convertedDate = this._date != null ? 
            this._databaseConverter!.fromDate( this._date ) : undefined;

        let data;

        if( this.encrypted() ) {
            data = this.encryptData( convertedDate )
        }
        else {
            data = convertedDate;
        }   
        
        if( data != null ) {
            documentData[this.key()] = data;
        }
    }


    compareTo( other : DateProperty ) : number {

       return this.compareValue( other.date() );
    }

    compareValue( date : Date | undefined ) : number {

        try {
            const thisDate = this.date();

            if( thisDate == null && date == null ) {
                return 0;
            }

            if( thisDate != null && date == null ) {
                return 1;
            }

            if( thisDate == null && date != null ) {
                return -1;
            }

            if( thisDate!.getTime() > date!.getTime() ) {
                return 1;
            }

            if( thisDate!.getTime() < date!.getTime() ) {
                return -1;
            }

            return 0;
            
        } catch( error ) {
            return 1;
        }
    }

    includes( other : DateProperty  ) : boolean {
        return this.includesValue( other.value() );
    }

    includesValue( value : Date | undefined ) : boolean {
        return this.compareValue( value ) === 0;
    }

    private _date : Date | undefined;

    private _defaultDate : Date | undefined;

    private _databaseConverter? : DatabaseConverter;

}