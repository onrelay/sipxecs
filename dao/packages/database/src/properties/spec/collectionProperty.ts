import { CollectionDatabase } from "../../core/spec/collectionDatabase";
import { CollectionGroupDatabase } from "../../core/spec/collectionGroupDatabase";
import { Database } from "../../core/spec/database";
import { DatabaseDocument } from "../../core/spec/databaseDocument";
import { DatabaseProperty } from "../../core/spec/databaseProperty";
import { PropertyDescriptor } from "../../core/spec/propertyDescriptor";

export interface CollectionProperty<DerivedDocument extends DatabaseDocument> extends DatabaseProperty<DerivedDocument> {

    database() : Database<DerivedDocument>;

    collection() : CollectionDatabase<DerivedDocument>;

    collectionGroup() : CollectionGroupDatabase<DerivedDocument> | undefined;

    collectionName() : string; 

    allowCollectionGroup() : boolean | undefined; 

    newDocument(): DerivedDocument | undefined;

}

export interface CollectionPropertyDescriptor<DerivedDocument extends DatabaseDocument> 
    extends PropertyDescriptor<CollectionProperty<DerivedDocument>>  { 

}
