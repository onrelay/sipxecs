import { DatabaseRecord } from "../types/databaseRecord";
import { DatabaseFilter } from "../types/databaseFilter";
import { CollectionDatabase } from "./collectionDatabase";
import { DatabaseDocument } from "./databaseDocument";
import { ReferenceHandle } from "../impl/referenceHandle";
import { DatabaseProperty } from "./databaseProperty";
import { DatabaseConverter } from "./databaseConverter";
import { Database } from "./database";
import { CollectionGroupDatabase } from "./collectionGroupDatabase";


export interface DatabaseManager {

    database( database : Database<DatabaseDocument> ): Promise<Map<string,DatabaseDocument>>; // path key
    
    monitorDatabase( database : Database<DatabaseDocument> ): Promise<void>;  

    releaseDatabase( database : Database<DatabaseDocument> ): Promise<void>; 

    isMonitoringDatabase( database : Database<DatabaseDocument> ): boolean; 

    collection( collectionDatabase : CollectionDatabase<DatabaseDocument> ): Promise<Map<string,DatabaseDocument>>;

    collectionGroup( collectionGroupDatabase : CollectionGroupDatabase<DatabaseDocument> ): Promise<Map<string,DatabaseDocument>>;

    groupReferenceHandles( collectionGroupDatabase : CollectionGroupDatabase<DatabaseDocument> ): Promise<Map<string,ReferenceHandle<DatabaseDocument>>>;

    monitorCollection( collectionDatabase : CollectionDatabase<DatabaseDocument> ): Promise<void>;

    releaseCollection( collectionDatabase : CollectionDatabase<DatabaseDocument> ): Promise<void>;

    isMonitoringCollection( collectionDatabase : CollectionDatabase<DatabaseDocument> ): boolean;

    monitorCollectionGroup( collectionGroupDatabase : CollectionGroupDatabase<DatabaseDocument> ): Promise<void>;

    releaseCollectionGroup( collectionGroupDatabase : CollectionGroupDatabase<DatabaseDocument> ): Promise<void>;

    isMonitoringCollectionGroup( collectionGroupDatabase : CollectionGroupDatabase<DatabaseDocument> ): boolean;

    referenceHandles( database : Database<DatabaseDocument> ): Promise<Map<string,ReferenceHandle<DatabaseDocument>>>;

    documents( collectionDatabase : CollectionDatabase<DatabaseDocument>, uris : string[] ) : Promise<Map<string,DatabaseDocument>>;

    monitorDocuments( collectionDatabase : CollectionDatabase<DatabaseDocument>, uri : string[] ) : Promise<void>;

    releaseDocuments( collectionDatabase : CollectionDatabase<DatabaseDocument>, uri : string[] ) : Promise<void>;

    releaseAllDocuments( collectionDatabase : CollectionDatabase<DatabaseDocument> ) : Promise<void>;

    isMonitoringDocument( collectionDatabase : CollectionDatabase<DatabaseDocument>, uri : string ) : boolean;
  
    documentReference( uri : string ) : any | undefined;

    documentUri( documentReference : any ) : string | undefined;

    createDocument( collectionDatabase : CollectionDatabase<DatabaseDocument>, 
        databaseDocument : DatabaseDocument ): Promise<DatabaseDocument>;

    readDocument( collectionDatabase : CollectionDatabase<DatabaseDocument>, 
        databaseDocument: DatabaseDocument): Promise<boolean>;

    updateDocument( collectionDatabase : CollectionDatabase<DatabaseDocument>, 
        databaseDocument: DatabaseDocument,
        force? : boolean ): Promise<boolean>;

    deleteDocument( collectionDatabase : CollectionDatabase<DatabaseDocument>, 
        databaseDocument : DatabaseDocument ): Promise<boolean>;

    archiveDocument( collectionDatabase : CollectionDatabase<DatabaseDocument>, 
        databaseDocument : DatabaseDocument ): Promise<boolean>;

    rewriteDocument( databaseDocument : DatabaseDocument, recursive? : boolean ) : Promise<void>;

    addProperty( collectionDatabase : CollectionDatabase<DatabaseDocument>, 
        documentPath : string,      
        property : DatabaseProperty<any> ): Promise<void>;

    readProperty( collectionDatabase : CollectionDatabase<DatabaseDocument>, 
        documentPath : string,      
        property : DatabaseProperty<any>): Promise<void>;

    updateProperty( collectionDatabase : CollectionDatabase<DatabaseDocument>, 
        documentPath : string,      
        property : DatabaseProperty<any>): Promise<void>;

    removeProperty( collectionDatabase : CollectionDatabase<DatabaseDocument>, 
        documentPath : string,      
        property : DatabaseProperty<any> ): Promise<void>;

    documentWithProperty( 
        database : Database<DatabaseDocument>, 
        property : string, 
        value : string ) : Promise<DatabaseDocument | undefined>;
    
    documentWithProperties( 
        database : Database<DatabaseDocument>, 
        properties : Map<string,string> ) : Promise<DatabaseDocument | undefined>;

    documentsWithProperty( 
        database : Database<DatabaseDocument>,
        property : string, 
        value : string ) : Promise<Map<string,DatabaseDocument>>;
    
    documentsWithProperties( 
        database : Database<DatabaseDocument>, 
        properties : Map<string,string> ) : Promise<Map<string,DatabaseDocument>>;

    expiredArchivedDocuments() : Promise<Map<string,DatabaseDocument>>;

    documentRecords( 
        database : Database<DatabaseDocument>, 
        databaseFilters? : DatabaseFilter[] ): Promise<Map<string,DatabaseRecord>>; 

    documentRecord( documentReference : any ): Promise<[string,DatabaseRecord] | undefined>;

    newDocumentRecordId( collectionDatabase : CollectionDatabase<DatabaseDocument> ): Promise<string>; 
    
    createDocumentRecord( uri : string, documentRecord : DatabaseRecord ): Promise<void>; 

    readDocumentRecord( uri : string ): Promise<DatabaseRecord | undefined>; 

    updateDocumentRecord( uri : string, documentRecord : DatabaseRecord ): Promise<void>; 

    deleteDocumentRecord( uri : string ): Promise<boolean>; 

    readonly converter : DatabaseConverter;

    readonly clientEncryption : boolean;
}
