import { KeyStatus, KeyVault } from "@dao/security";
import { DefinitionProperty } from "../../properties/spec/definitionProperty";
import { LongTextProperty } from "../../properties/spec/LongTextProperty";
import { NumberProperty } from "../../properties/spec/numberProperty";
import { OwnerProperty } from "../../properties/spec/ownerProperty";
import { TextProperty } from "../../properties/spec/textProperty";
import { DatabaseDocument } from "../../core/spec/databaseDocument";
import { User } from "./user";

export const KeyDocumentName = "key";
export const KeysCollectionName = "keys";

export interface Key extends DatabaseDocument  { 

    readonly owner: OwnerProperty<User>, 

    readonly publicKey : LongTextProperty;

    readonly secretKey : TextProperty;  // Only for temporary storage

    readonly keyVault : DefinitionProperty<KeyVault>;

    readonly version : NumberProperty;

    readonly keyStatus : DefinitionProperty<KeyStatus>;

    readonly keyType : DefinitionProperty<KeyType>;

    readonly keyFormat : DefinitionProperty<KeyFormat>;
}

