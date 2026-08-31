import { Language } from "@dao/common";
import { DefinitionProperty } from "../../properties/spec/definitionProperty";
import { CountryProperty } from "../../properties/spec/countryProperty";
import { TextProperty } from "../../properties/spec/textProperty";
import { Entity } from "./entity";
import { PhoneNumberProperty } from "../../properties/spec/phoneNumberProperty";
import { DateProperty } from "../../properties/spec/dateProperty";

export const UserDocumentName = "user";

export interface User extends Entity {

    readonly country : CountryProperty;

    readonly language : DefinitionProperty<Language>;

    readonly email : TextProperty;
    
    readonly phoneNumber : PhoneNumberProperty;

    readonly firstName : TextProperty;

    readonly lastName : TextProperty;

    readonly dateOfBirth : DateProperty;
}

