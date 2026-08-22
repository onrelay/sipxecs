import { CountryProperty } from "../properties/countryProperty";
import { TextProperty } from "../properties/textProperty";
import { DatabaseSubdocument } from "../dao/databaseSubdocument";


export interface Address extends DatabaseSubdocument {

    readonly address1 : TextProperty,

    readonly address2? : TextProperty,

    readonly city? : TextProperty,

    readonly zip? : TextProperty,

    readonly state? : TextProperty,

    readonly country? : CountryProperty
}

