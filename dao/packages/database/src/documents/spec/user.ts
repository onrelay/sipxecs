import { Language } from "@dao/common";
import { DefinitionProperty } from "../../properties/spec/definitionProperty";
import { CountryProperty } from "../../properties/spec/countryProperty";
import { TextProperty } from "../../properties/spec/textProperty";
import { Entity } from "./entity";

export const UserDocumentName = "user";

export interface User extends Entity {
    
    readonly country : CountryProperty;

    readonly language : DefinitionProperty<Language>;
}

