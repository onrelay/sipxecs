import { DefinitionProperty } from "../properties/definitionProperty";
import { MapProperty } from "../properties/mapProperty";
import { OwnerProperty } from "../properties/ownerProperty";
import { ReferenceProperty } from "../properties/referenceProperty";
import { ChangeType, ChangeTypeName } from "../types/changeType";
import { DatabaseDocument } from "./databaseDocument";
import { Entity } from "./entity";


export const ChangeTypePropertyKey = ChangeTypeName;

export const ChangeDocumentName = "change";

export interface Change extends DatabaseDocument {
    
    readonly changedDocument: OwnerProperty<DatabaseDocument>, 
   
    readonly changedBy : ReferenceProperty<Entity>,

    readonly changeType : DefinitionProperty<ChangeType>,

    readonly changed : MapProperty<any> // changed property is key, updated entry is value

}

