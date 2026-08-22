import { Observable } from "@sipxdao/common";
import { CollectionProperty } from "../properties/collectionProperty";
import { TextProperty } from "../properties/textProperty";
import { CollectionDatabase } from "./collectionDatabase";
import { CollectionGroupDatabase } from "./collectionGroupDatabase";
import { Database } from "./database";
import { DatabaseDocument } from "./databaseDocument";
import { DatabaseProperty } from "./databaseProperty";
import { PropertiesSelector } from "./propertiesSelector";
import { ReferenceHandle } from "./referenceHandle";
import { DatabaseAccess } from "../types/databaseAccess";


export interface DatabaseObject extends Observable {
    
    property( key : string ) : DatabaseProperty<any> | undefined;

    properties( propertiesSelector? : PropertiesSelector ) : Map<string,DatabaseProperty<any>>;

    propertyCount( propertiesSelector? : PropertiesSelector ) : number;

    copyProperties( other : DatabaseObject, propertiesSelector?: PropertiesSelector ) : Promise<boolean>;

    isComplete( propertiesSelector? : PropertiesSelector ) : boolean;

    validate( propertiesSelector? : PropertiesSelector, markMissingProperties? : boolean ) : Map<string,Error>;

    changedProperties() : Map<string,DatabaseProperty<any>>;

    clearChanged() : void;

    isChanged() : boolean;

    isLoaded() : boolean, 

    path() : string | undefined,

    uri() : string | undefined,
  
    referenceHandle() : ReferenceHandle<DatabaseDocument> | undefined,

    ownerIds(collectionName?: string): string[] | undefined; 

    symbolicOwnerIds(collectionName?: string): string[] | undefined; 

    ownerId(collectionName?: string): string | undefined; 

    ownerIds( collectionName? : string ) : string[] | undefined;

    ownerPath(collectionName?: string): string | undefined;

    ownerPaths( collectionName? : string ) : string[] | undefined;
  
    emptyOwnerDocument(collectionName?: string): DatabaseDocument | undefined;

    emptyOwnerDocuments( collectionName? : string ) : DatabaseDocument[] | undefined; 

    ownerDocument(collectionName?: string): Promise<DatabaseDocument | undefined>;

    ownerDocuments( collectionName? : string ) : Promise<DatabaseDocument[] | undefined>;
  
    ownerCollection(collectionName?: string): CollectionDatabase<DatabaseDocument> | undefined;

    ownerCollections(collectionName?: string): CollectionDatabase<DatabaseDocument>[] | undefined;

    ownerCollectionGroup(collectionName?: string): CollectionGroupDatabase<DatabaseDocument> | undefined;

    ownerCollectionGroups(collectionName?: string): CollectionGroupDatabase<DatabaseDocument>[] | undefined;

    parentDatabases( collectionName : string, 
        options? : { 
            nearestIsCollectionGroup? : boolean, 
            includeRootCollection? : boolean }  ) : Database<DatabaseDocument>[] | undefined;

    parentCollection(collectionName: string): CollectionDatabase<DatabaseDocument> | undefined;

    parentCollections(collectionName: string): CollectionDatabase<DatabaseDocument>[] | undefined;

    parentCollectionGroup(collectionName: string): CollectionGroupDatabase<DatabaseDocument> | undefined;

    parentCollectionGroups(collectionName: string): CollectionGroupDatabase<DatabaseDocument>[] | undefined;

    parentCollectionProperty(collectionName: string): CollectionProperty<DatabaseDocument> | undefined;

    parentCollectionProperties(collectionName: string): CollectionProperty<DatabaseDocument>[] | undefined;

    copyFrom( other : DatabaseObject ) : Promise<void>,

    compareTo( other? : DatabaseObject ) : number, 

    recordName() : string, 

    databaseAccess() : DatabaseAccess,

    setDatabaseAccess( databaseAccess : DatabaseAccess | undefined ) : void;

    fromRecord( data : Record<string, any> ) : void,

    toRecord( force? : boolean ) : Promise<Record<string, any>>, 

    toJson() : Promise<string>,

    fromJson( json : string ) : void,

    onCreate(): Promise<void>;

    onUpdate(): Promise<void>;

    onDelete(): Promise<void>;

    onCreated(): Promise<void>;

    onUpdated(): Promise<void>;

    onDeleted(): Promise<void>;

    readonly parent? : DatabaseObject,

    readonly id : TextProperty,

    readonly name : TextProperty,

    readonly title : TextProperty

}

