import { DatabaseDocument } from "../../core/spec/databaseDocument";
import { TextProperty } from "../../properties/spec/textProperty";
import { CollectionProperty } from "../../properties/spec/collectionProperty";
import { Key } from "./key";

export const EntityDocumentName = "entity";

export interface Entity extends DatabaseDocument {

    readonly authId : TextProperty;

    readonly keys : CollectionProperty<Key>;
}

