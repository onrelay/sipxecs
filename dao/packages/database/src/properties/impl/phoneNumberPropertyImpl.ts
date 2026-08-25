import { DatabaseObject } from "../../core/spec/databaseObject";
import { CountryProperty } from "../spec/countryProperty";
import { PhoneNumberProperty } from "../spec/phoneNumberProperty";
import { PhoneNumber } from "../../core/impl/phoneNumber";
import { PropertyTypes, PropertyType } from "../../core/defs/propertyType";
import { AbstractBasicProperty } from "./abstractBasicProperty";

export const MinE164PhoneNumberLength = 10;
export const MinNationalPhoneNumberLength = 8;


export class PhoneNumberPropertyImpl extends AbstractBasicProperty<string> implements PhoneNumberProperty {

    constructor( parent : DatabaseObject, countrySource? : CountryProperty, defaultValue? : string ) {
        super( parent, PropertyTypes.PhoneNumber as PropertyType, defaultValue ); 

        this._countrySource = countrySource;
    }

    countrySource() : CountryProperty | undefined {
        return this._countrySource;
    }

    setCountrySource( countrySource? : CountryProperty ) {
        this._countrySource = countrySource;
    }


    defaultCountry() : string | undefined {
        return this._countrySource?.value();
    }

    setValue( value : string | undefined ): void { 

        const oldValue = this._value;

        if( this.onChange( oldValue, value ) ) {

            this._value = value;

            this.onChanged( oldValue, value );
        }
    }

    cleanValue() : string | undefined {
        return PhoneNumber.cleanPhoneNumber( this.value() );
    }

    validValue( requireE164 : boolean ) : boolean {
        return PhoneNumber.isValidPhoneNumber( this.value(), requireE164 );
    }

    compareTo( other : PhoneNumberProperty ) : number {

       return this.compareValue( other.value() );
    }

    compareValue( other : string | undefined ) : number {

        const thisValue = this.cleanValue();

        const otherValue = PhoneNumber.cleanPhoneNumber( other );

        if( thisValue == null && otherValue == null ) {
            return 0;
        }

        if( thisValue != null && otherValue == null ) {
            return 1;
        }

        if( thisValue == null && otherValue != null ) {
            return -1;
        }

        return thisValue!.localeCompare( otherValue! );
    }

    includes( other : PhoneNumberProperty ) : boolean {
        return this.includesValue( other.value() );
    }

    includesValue( value : string | undefined ) : boolean {

        const thisValue = this.cleanValue();

        const otherValue = PhoneNumber.cleanPhoneNumber( value );

        return thisValue != null && otherValue != null && thisValue.includes( otherValue ); 
    }


    private _countrySource? : CountryProperty; 

}