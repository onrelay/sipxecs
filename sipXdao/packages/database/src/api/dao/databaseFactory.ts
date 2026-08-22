import { ConfigurationManager } from "@sipxdao/configuration";

import { CollectionDatabase } from "./collectionDatabase";
import { DatabaseDocument } from "./databaseDocument";
import { CollectionGroupDatabase } from "./collectionGroupDatabase";
import { Database } from "./database";
import { DatabaseObserver } from "./databaseObserver";
import { DatabaseQuery } from "./databaseQuery";
import { Template } from "./template";
import { TemplatedProperties } from "./templatedProperties";
import { DatabaseManager } from "./databaseManager";
import { TemplatedDocument } from "./templatedDocument";

export interface DatabaseFactory {

    documentId( uri : string ) : string | undefined;

    documentReference( uri : string ) : any | undefined;

    documentUri( documentReference : any ) : string | undefined;

    collectionNames() : string[],

    rootCollectionNames() : string[],

    documentNames() : string[],

    collectionGroupDatabaseFromCollectionName( 
        collectionName : string, 
        owner? : DatabaseDocument,
        template? : Template<TemplatedDocument>  ) : CollectionGroupDatabase<DatabaseDocument> | undefined;
    
    collectionGroupDatabaseFromDocumentName( 
        documentName : string, 
        owner? : DatabaseDocument,
        template? : Template<TemplatedDocument> ) : CollectionGroupDatabase<DatabaseDocument>  | undefined;

    collectionDatabaseFromCollectionName( 
        collectionName : string, 
        owner? : DatabaseDocument,
        template? : Template<TemplatedDocument> ) : CollectionDatabase<DatabaseDocument>  | undefined;

    collectionDatabaseFromDocumentName( 
        documentName : string, 
        owner? : DatabaseDocument,
        template? : Template<TemplatedDocument> ) : CollectionDatabase<DatabaseDocument>  | undefined;

    isUriDatabase( url : string  ) : boolean;

    isUriDocument( url : string  ) : boolean;

    uriToPath( databasePath? : string ) : string | undefined;

    equalUris( databaseUri1? : string, databaseUri2? : string ) : boolean;

    documentNameFromUri( url : string ) : string | undefined;

    collectionNameFromUri( url : string  ) : string | undefined;

    collectionPathFromUri( url : string, collectionName? : string  ) : string | undefined; 

    documentPathFromUri( url : string, collectionName? : string  ) : string | undefined; 

    databaseFromUri( url : string ) : Database<DatabaseDocument> | undefined;

    collectionGroupFromUri( url : string  ) : CollectionGroupDatabase<DatabaseDocument> | undefined;

    collectionFromUri( url : string ) : CollectionDatabase<DatabaseDocument> | undefined;

    collectionInUri( url : string, collectionName? : string ) : CollectionDatabase<DatabaseDocument> | undefined;

    documentFromUri( url : string ) : Promise<DatabaseDocument | undefined>;

    decodeUriQuery( uri : string ) : Map<string,string>;

    encodeUriQuery( queryParams : Map<string,string> ) : string;

    documentFromRecord( documentPath: string, data: any ): Promise<DatabaseDocument | undefined>, 

    newTemplatedDocument( template: Template<TemplatedDocument> ) : DatabaseDocument;

    templatePathFromUri( url : string ) : string | undefined;

    updateTemplatedProperties( databaseDocument : DatabaseDocument, 
        template: Template<TemplatedDocument>,
        applyEditRestrictions? : boolean ) : TemplatedProperties | undefined; 

    newDocument( 
        collectionDatabase : CollectionDatabase<DatabaseDocument>, 
        documentPath? : string,
        template? : Template<TemplatedDocument> ) : DatabaseDocument | undefined;

    newDocumentFromUri( url : string ) : DatabaseDocument | undefined;

    parentCollections( database : Database<DatabaseDocument> ) : Database<DatabaseDocument>[];

    newDatabaseObserver( databaseQuery? : DatabaseQuery<DatabaseDocument> ) : DatabaseObserver<DatabaseDocument>;

    readonly configurationManager : ConfigurationManager;

    readonly databaseManager : DatabaseManager;
}

 