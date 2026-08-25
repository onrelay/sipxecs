import { DatabaseObject } from "../../core/spec/databaseObject";
import { ConfirmationProperty } from "../spec/confirmationProperty";
import { PropertyType, PropertyTypes } from "../../core/defs/propertyType";
import { BooleanPropertyImpl } from "./booleanPropertyImpl";

export class ConfirmationPropertyImpl extends BooleanPropertyImpl implements ConfirmationProperty {

    constructor( parent : DatabaseObject, defaultValue? : boolean ) {
        super( parent, PropertyTypes.Confirmation as PropertyType, defaultValue );
    }

    onChange( oldValue : boolean | undefined, newValue : boolean | undefined ) : boolean {

        if( this.required && !newValue ) {

            this.error = new Error( "propertyValueRejected" );
            return true;
        }

        return super.onChange( oldValue, newValue );
    }


}