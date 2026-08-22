

export const MinE164PhoneNumberLength = 10;
export const MinNationalPhoneNumberLength = 8;

export class PhoneNumber {

     static cleanPhoneNumber( phoneNumber : string | undefined ) : string | undefined {

        let cleanPhoneNumber = phoneNumber != null ? "" : undefined;

        if( cleanPhoneNumber != null ) {

            for( let i = 0; i < phoneNumber!.length; i++ ) {

                if( phoneNumber![i] === ' ' ) {
                    continue;
                }
                
                if( !isNaN( +phoneNumber![i] ) || phoneNumber![i] === '+' ) {
                    cleanPhoneNumber += phoneNumber![i];
                }
            }
        }
        return cleanPhoneNumber; 
    }

    static isValidPhoneNumber( phoneNumber : string | undefined, requireE164 : boolean ) : boolean {

        let cleanPhoneNumber = PhoneNumber.cleanPhoneNumber( phoneNumber );

        if( cleanPhoneNumber == null || cleanPhoneNumber.length === 0 ) {
            return false;
        }

        if( requireE164 && cleanPhoneNumber.length < MinE164PhoneNumberLength + 1 ) { // +1 for "+"
            return false;
        }

        if( !requireE164 && cleanPhoneNumber.length < MinNationalPhoneNumberLength ) {
            return false;
        }

        if( requireE164 && !cleanPhoneNumber.startsWith("+") ) {
            return false;
        }

        return true;
    }

}