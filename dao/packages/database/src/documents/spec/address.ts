import { CountryProperty } from "../../properties/spec/countryProperty";
import { TextProperty } from "../../properties/spec/textProperty";
import { DatabaseSubdocument } from "../../core/spec/databaseSubdocument";


export interface Address extends DatabaseSubdocument {

    readonly address1 : TextProperty,

    readonly address2? : TextProperty,

    readonly city? : TextProperty,

    readonly zip? : TextProperty,

    readonly state? : TextProperty,

    readonly country? : CountryProperty
}

