import { BasicProperty } from "./basicProperty";

export interface PhoneNumberProperty extends BasicProperty<string> {
    
    cleanValue() : string | undefined;

    validValue( requireE164 : boolean ) : boolean;

}

