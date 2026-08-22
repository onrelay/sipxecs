import { KeyStatus, KeyVault } from "@sipxdao/security";
import { DefinitionProperty } from "../properties/definitionProperty";
import { LongTextProperty } from "../properties/LongTextProperty";
import { NumberProperty } from "../properties/numberProperty";
import { OwnerProperty } from "../properties/ownerProperty";
import { TextProperty } from "../properties/textProperty";
import { DatabaseDocument } from "./databaseDocument";
import { Entity } from "./entity";

export const KeyDocumentName = "key";
export const KeysCollectionName = "keys";

export interface Key extends DatabaseDocument  { 

    readonly owner: OwnerProperty<Entity>, 

    readonly publicKey : LongTextProperty;

    readonly secretKey : TextProperty;  // Only for temporary storage

    readonly keyVault : DefinitionProperty<KeyVault>;

    readonly version : NumberProperty;

    readonly keyStatus : DefinitionProperty<KeyStatus>;

    readonly keyType : DefinitionProperty<KeyType>;

    readonly keyFormat : DefinitionProperty<KeyFormat>;
}

