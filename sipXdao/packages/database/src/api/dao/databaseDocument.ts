
import { BooleanProperty } from "../properties/booleanProperty";
import { CollectionProperty } from "../properties/collectionProperty";
import { DateProperty } from "../properties/dateProperty";
import { ReferenceProperty } from "../properties/referenceProperty";
import { Change } from "./change";
import { CollectionDatabase } from "./collectionDatabase";
import { DatabaseFilter } from "./databaseFilter";
import { DatabaseObject } from "./databaseObject";
import { ReferenceHandle } from "./referenceHandle";

export const EndDatePropertyKey = "endDate";

export const ArchivedPropertyKey = "archived";

export const DatabaseDocumentNameKey = "name"; 

export interface DatabaseDocument extends DatabaseObject {
    
    isNew() : boolean,

    create() : Promise<void>,

    read() : Promise<void>,

    update( force? : boolean ) : Promise<void>,

    delete() : Promise<void>,

    move( nextCollectionDatabase : CollectionDatabase<DatabaseDocument> ) : Promise<DatabaseDocument>; 

    duplicate() : Promise<DatabaseDocument>,

    path() : string,

    uri() : string,

    documentReference() : any | undefined,

    referenceHandle() : ReferenceHandle<DatabaseDocument>,

    referenceDateProperty() : DateProperty | undefined,

    matchFilter( params: {
        from? : Date, 
        to? : Date, 
        matchHistoric? : boolean, 
        databaseFilters? : Map<string,DatabaseFilter> 
    } ) : boolean;

    updateKeys() : Promise<boolean>;

    disableKeys() : Promise<boolean>;

    readonly collectionDatabase : CollectionDatabase<DatabaseDocument>;
    
    readonly startDate : DateProperty,

    readonly endDate : DateProperty,

    readonly changes : CollectionProperty<Change>,

    readonly lastChangedBy : ReferenceProperty<DatabaseDocument>,

    readonly lastChangedAt : DateProperty,

    readonly archived: BooleanProperty,

    readonly archivedAt : DateProperty
}




