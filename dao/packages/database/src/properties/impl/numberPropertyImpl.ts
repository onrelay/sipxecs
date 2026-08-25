import { log } from "../../core/base/abstractDatabaseService";
import { DatabaseObject } from "../../core/spec/databaseObject";
import { NumberProperty } from "../spec/numberProperty";
import { PropertyTypes, PropertyType } from "../../core/defs/propertyType";
import { AbstractBasicProperty } from "./abstractBasicProperty";

export class NumberPropertyImpl extends AbstractBasicProperty<number> implements NumberProperty {

    constructor( parent : DatabaseObject, defaultValue? : number, minValue? : number, maxValue? : number ) {
        super( parent, PropertyTypes.Number as PropertyType, defaultValue ); 

        this.minValue = minValue;

        this.maxValue = maxValue;
    }

    setValue( value : number | undefined ): void {

        this.decryptData();

        const oldValue = this._value;

        if( this.onChange( oldValue, value ) ) {

            this._value = value;

            this.onChanged( oldValue, value );
        }
    }

    onChange( oldValue : number | undefined, newValue : number | undefined ) : boolean {

        if( this.minValue != null && newValue != null && newValue < this.minValue ) {

            this.error = new Error( "propertyValueRejected" );

            if( (newValue + "").length <= (this.minValue + "").length ) {
                return true;
            }
            else {
                return false;
            }
        }

        if( this.maxValue != null && newValue != null && newValue > this.maxValue ) {
            
            this.error = new Error( "propertyValueRejected" );
            return false;
        }

        return super.onChange( oldValue, newValue );
    }

    onChanged( oldValue : number | undefined, newValue : number | undefined ) : void {

        try {
            //log.traceIn( "onChanged()", {oldValue}, {newValue} );

           this.setPreviousValues( this.previousValues() != null ? 
                this.previousValues().concat(newValue) : [newValue] );

            if( this.minValue != null && newValue != null && newValue >= this.minValue ) {

                delete this.error;
            }
 

            //log.traceOut( "onChanged()", {oldValue}, {newValue} ); 
            
        } catch( error ) {

            log.warn( "onChanged()", "Error checking change", error );
            
            throw new Error("Error checking change: " + (error as any).message );
        }
    }


    compareTo( other : NumberProperty ) : number {

        return this.compareValue( other.value() );
    }

    compareValue( otherValue : number | undefined ) : number {

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

        if( value! > otherValue! ) {
            return 1;
        }

        if( value! < otherValue! ) {
            return -1;
        }

        return 0;
    }

    readonly minValue? : number;

    readonly maxValue? : number;

}