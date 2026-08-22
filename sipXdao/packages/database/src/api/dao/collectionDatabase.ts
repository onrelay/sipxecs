import { Observable, Observation } from "@sipxdao/common";
import { Database } from "./database";
import { DatabaseDocument } from "./databaseDocument";
import { DatabaseManager } from "./databaseManager";
import { DatabaseProperty } from "./databaseProperty";

export interface CollectionDatabase<DerivedDocument extends DatabaseDocument>  extends Database<DerivedDocument> {
 
    createDocument( databaseDocument: DerivedDocument ): Promise<void>,

    readDocument(databaseDocument: DerivedDocument ): Promise<boolean>,

    updateDocument(databaseDocument: DerivedDocument, force? : boolean ): Promise<void>,

    deleteDocument( databaseDocument: DerivedDocument ): Promise<void>,

    moveDocument( databaseDocument: DerivedDocument, nextCollectionDatabase : CollectionDatabase<DerivedDocument> ) : Promise<DerivedDocument>;

    addProperty( documentPath : string, property : DatabaseProperty<any> ): Promise<void>;

    readProperty( documentPath : string, property : DatabaseProperty<any>): Promise<void>;

    updateProperty( documentPath : string, property : DatabaseProperty<any>): Promise<void>;

    removeProperty( documentPath : string, property : DatabaseProperty<any> ): Promise<void>;

    readonly databaseManager : DatabaseManager,

    readonly allowRootCollection : boolean,

    readonly encrypted : boolean,

}