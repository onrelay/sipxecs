import { Language } from "@sipxdao/common";
import { DefinitionProperty } from "../properties/definitionProperty";
import { DatabaseDocument } from "./databaseDocument";
import { CountryProperty } from "../properties/countryProperty";
import { CollectionProperty } from "../properties/collectionProperty";
import { Key } from "./key";
import { TextProperty } from "../properties/textProperty";

export const EntityDocumentName = "entity";

export interface Entity extends DatabaseDocument {

    readonly authenticationId : TextProperty;
    
    readonly country : CountryProperty;

    readonly language : DefinitionProperty<Language>;

    readonly keys : CollectionProperty<Key>;
}

