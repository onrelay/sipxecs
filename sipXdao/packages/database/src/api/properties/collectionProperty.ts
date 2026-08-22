import { CollectionDatabase } from "../dao/collectionDatabase";
import { CollectionGroupDatabase } from "../dao/collectionGroupDatabase";
import { Database } from "../dao/database";
import { DatabaseDocument } from "../dao/databaseDocument";
import { DatabaseProperty } from "../dao/databaseProperty";
import { PropertyDescriptor } from "../dao/propertyDescriptor";

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
