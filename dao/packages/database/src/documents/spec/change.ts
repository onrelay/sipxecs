import { DefinitionProperty } from "../../properties/spec/definitionProperty";
import { MapProperty } from "../../properties/spec/mapProperty";
import { OwnerProperty } from "../../properties/spec/ownerProperty";
import { ReferenceProperty } from "../../properties/spec/referenceProperty";
import { ChangeType, ChangeTypeName } from "../../core/defs/changeType";
import { DatabaseDocument } from "../../core/spec/databaseDocument";
import { User } from "./user";


export const ChangeTypePropertyKey = ChangeTypeName;

export const ChangeDocumentName = "change";

export interface Change extends DatabaseDocument {
    
    readonly changedDocument: OwnerProperty<DatabaseDocument>, 
   
    readonly changedBy : ReferenceProperty<User>,

    readonly changeType : DefinitionProperty<ChangeType>,

    readonly changed : MapProperty<any> // changed property is key, updated entry is value

}

