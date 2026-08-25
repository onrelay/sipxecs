import { Monitor, Observable, Observation } from "@dao/common";
import { DatabaseDocument } from "../spec/databaseDocument";
import { DateProperty } from "../../properties/spec/dateProperty";
import { CollectionDatabase } from "../spec/collectionDatabase";
import { CollectionGroupDatabase } from "../spec/collectionGroupDatabase";
import { Database } from "../spec/database";
import { CollectionProperty } from "../../properties/spec/collectionProperty";
import { PropertyType, PropertyTypes } from "../defs/propertyType";
import { DatabaseFilter } from "../types/databaseFilter";
import { Comparators } from "../defs/comparator";
import { PropertiesSelector } from "../types/propertiesSelector";
import { AbstractDatabaseObject } from "../base/abstractDatabaseObject";
import { ReferenceHandle } from "./referenceHandle";
import { DatePropertyImpl } from "../../properties/impl/datePropertyImpl";
import { SymbolicOwnersProperty } from "../../properties/spec/symbolicOwnersProperty";
import { DatabaseAccess } from "./databaseAccess";
import { log } from "../base/abstractDatabaseService";
import { ChangesCollection, NewObjectId, OwnerIds } from "../spec/databaseService";
import { databaseServiceFactory, DatabaseServiceFactory } from "./databaseServiceFactory";
import { Change } from "../../documents/spec/change";
import { ReferenceProperty } from "../../properties/spec/referenceProperty";
import { User } from "../../documents/spec/user";
import { BooleanProperty } from "../../properties/spec/booleanProperty";
import { CollectionPropertyImpl } from "../../properties/impl/collectionPropertyImpl";
import { ReferencePropertyImpl } from "../../properties/impl/referencePropertyImpl";
import { BooleanPropertyImpl } from "../../properties/impl/booleanPropertyImpl";
import { DatabaseDocumentNameKey } from "../spec/databaseDocument";
import { KeysCollectionName } from "../../documents/spec/key";

export class GenericDatabaseDocument extends AbstractDatabaseObject implements DatabaseDocument {

    constructor( documentName : string,
        collectionDatabase : CollectionDatabase<DatabaseDocument>,
        documentPath? : string ) {

        super( documentName );

        try {
            //log.traceIn( "constructor()", collectionDatabase, documentPath );

            this.collectionDatabase = collectionDatabase;

            this.startDate = new DatePropertyImpl( this );

            this.endDate = new DatePropertyImpl( this );  

            this.changes = new CollectionPropertyImpl<Change>( this, ChangesCollection, false );

            this.lastChangedBy = new ReferencePropertyImpl<User>( this );
            this.lastChangedBy.trackChanges = false;

            this.lastChangedAt = new DatePropertyImpl( this );
            this.lastChangedAt.trackChanges = false;

            this.archived = new BooleanPropertyImpl( this );
            this.archived.trackChanges = false;
            this.archived.encrypt = false;

            this.archivedAt = new DatePropertyImpl( this ); 
            this.archivedAt.trackChanges = false;
            this.archivedAt.encrypt = false;

            if( documentPath != null ) {

                const documentId = databaseServiceFactory!.get().databaseFactory.documentId( documentPath );

                if( documentId == null ) {
                    throw new Error( "Invalid document path: " + documentPath );
                }

                if( !documentId.endsWith( NewObjectId ) ) {
                    this.id.setValue( documentId );
                    this.id.clearChanges();
                }
            }

            this.onNotifyDocumentChange = this.onNotifyDocumentChange.bind(this);

            //log.traceOut( "constructor()");

        } catch( error ) {
                
            log.warn(  "("+documentName+")", "constructor()", "Error creating database document", error );

            throw new Error( (error as any).message );
        }
    }

    isNew() : boolean { 
        return this.id.value() == null; 
    }

    async create(): Promise<void> {

        //log.traceIn( "("+this.name.value()!+")", "create()", this.referenceHandle().title );

        try {
            await this.collectionDatabase.createDocument( this );

            //log.traceOut( "("+this.name.value()!+")", "create()", this.referenceHandle().title );

        } catch( error ) {
            
            log.warn( "("+this.name.value()!+")", "create()", "Error creating database object", error );

            throw new Error( (error as any).message );
        }
    }


    async read(): Promise<void> {

        //log.traceIn( "("+this.name.value()!+")", "read()" );

        try {
            if( this.id.value() == null ) {
                throw new Error( "Cannot read document without ID: " + this.uri());
            }

            const exists = await this.collectionDatabase.readDocument( this );

            if( !exists ) { 

                throw new Error( "Document does not exist with path: " + this.path() );
            }    

            //log.traceOut( "("+this.name.value()!+")", "read()", this.referenceHandle().title );

        } catch( error ) {
            
            log.warn( "("+this.name.value()!+")", "read()", "Error reading database object", error );

            throw new Error( (error as any).message );
        }
    }

    async update( force? : boolean ): Promise<void> {

        //log.traceIn( "("+this.name.value()!+")", "update()", this.referenceHandle().title, {force} );

        try {

            await this.collectionDatabase.updateDocument( this, force );

            //log.traceOut( "("+this.name.value()!+")", "update()", this.referenceHandle().title );

        } catch( error ) {
            
            log.warn( "("+this.name.value()!+")", "update()", "Error updating database object", error );

            throw new Error( (error as any).message );
        }
    }

    async delete(): Promise<void> {

        //log.traceIn( "("+this.name.value()!+")", "delete()", this.referenceHandle().title );

        try {

            await this.collectionDatabase.deleteDocument( this );

            //log.traceOut( "("+this.name.value()!+")", "delete()", this.referenceHandle().title );

        } catch( error ) {
            
            log.warn( "("+this.name.value()!+")", "delete()", "Error deleting database object", error );

            throw new Error( (error as any).message );
        }
    }

    async move( nextCollectionDatabase : CollectionDatabase<DatabaseDocument> ) : Promise<DatabaseDocument> {

        log.traceIn( "move()" );

        try {

            const movedDocument = await this.collectionDatabase.moveDocument( this, nextCollectionDatabase );

            log.traceOut( "move()" );
            return movedDocument;

        } catch( error ) {
            
            log.warn( "("+this.name.value()!+")", "move()", "Error moving database object", error );

            throw new Error( (error as any).message );
        }
    }

    path() : string {

        //log.traceIn( "("+this.collectionDatabase.documentName+")", "databasePath()", {excludeDocumentName} );

        let path = this.collectionDatabase.path();

        if( this.id.value() != null ) {

            path += "/" + this.id.value();
        }
        else {
            path += "/" + NewObjectId;               
        }

        //log.traceOut( "("+this.collectionDatabase.documentName+")", "databasePath()", path );
        return path;
    }

    uri() : string {

        //log.traceIn( "("+this.collectionDatabase.documentName+")", "databasePath()", {excludeDocumentName} );

        let uri = this.path();

        let variables = "";

        const documentName = this.name.value();

        if( documentName != null  ) {
            variables += DatabaseDocumentNameKey + "=" + documentName;
        }

        uri = uri + (variables.length > 0 ? "?" : "") + variables;
    
    
        //log.traceOut( "("+this.collectionDatabase.documentName+")", "uri()", uri );
        return uri;
    }

    documentReference() : any | undefined {
        
        if( this.isNew() ) {
            return undefined;
        }

        return this.collectionDatabase.databaseManager.documentReference( this.uri() );
    }

    referenceHandle() : ReferenceHandle<DatabaseDocument> {

        const referenceHandle = new ReferenceHandle<DatabaseDocument>( {

            title: this.title.value(),

            date: this.referenceDateProperty()?.value(),

            path: this.path(),

            uri: this.uri(),

            databaseDocument: this,

            documentReference: this.documentReference()
        });

        return referenceHandle;
    }

    // Override as required
    referenceDateProperty() : DateProperty | undefined {
        return this.startDate;
    }


    ownerId( collectionName? : string ) : string | undefined {

        //log.traceIn( "ownerDocumentId()", collectionName );

        try {
            if( this.collectionDatabase == null ) {
                //log.traceOut( "ownerDocumentPath()", "no collection" );
                return undefined;
            }

            if( collectionName == null ) {

                const ownerDocumentId = this.collectionDatabase.owner() != null ? 
                    this.collectionDatabase.owner()!.id.value() : undefined;

                //log.traceOut( "ownerDocumentPath()", "no collection name", ownerDocumentPath );
                return ownerDocumentId;
            }

            /*
            if( this.collectionDatabase.collectionName()=== collectionName && this.id.value() != null) {

                const ownerDocumentId = this.id.value();

                //log.traceOut( "ownerDocumentPath()", "this document is the owner", ownerDocumentId );
                return ownerDocumentId;
            }
            */ 

            let result : string | undefined;

            let path = this.collectionDatabase.path();

            const pathElements = path.startsWith("/") ? 
                path.substring(1).split("/") : path.split("/");  // remove leading "/" and split the rest

            for( let i = 0; i < pathElements.length; i++ ) {

                if( pathElements[i] === collectionName ) {

                    if( i + 1 < pathElements.length ) {
                        //log.trace( "ownerDocumentId()", "found", pathElements[i + 1] );
                        result = pathElements[i + 1];  
                    } 
                    else {
                        result = this.id.value();
                    }            
                }
            }

            //log.traceOut( "ownerDocumentId()", "not found" );
            return result;   

        } catch( error ) {
            log.warn( "ownerDocumentId()", "Error reading owner document ID", error );

            throw new Error( (error as any).message );
        }
    }

    ownerIds( collectionName? : string ) : string[] | undefined {

        //log.traceIn( "ownerDocumentIds()", collectionName );  

        try {
            if( this.collectionDatabase == null ) {
                //log.traceOut( "ownerDocumentIds()", "no collection" );  
                return undefined;
            }

            const result : string[] = [];

            let path = this.collectionDatabase.path();

            const pathElements = path.startsWith("/") ? 
                path.substring(1).split("/") : path.split("/");  // remove leading "/" and split the rest

            for( let i = 0; i < pathElements.length; i++ ) {

                if( collectionName == null && i % 2 === 1 ) { // every uneven path element is a document,

                    //log.traceOut( "ownerDocumentIds()", "found no collectionName", pathElements[i] );
                    result.push( pathElements[i] );                
                }
                else if( i > 0 && pathElements[i-1] === collectionName ) {

                    //log.traceOut( "ownerDocumentIds()", "found collectionName", pathElements[i] );
                    result.push( pathElements[i] );   
                }
            }

            //log.traceOut( "ownerDocumentIds()", result );
            return result;   

        } catch( error ) {
            log.warn( "ownerDocumentId()", "Error owner document IDs", error );

            throw new Error( (error as any).message );
        }
    }

    symbolicOwnerIds( collectionName? : string ) : string[] | undefined {

       //log.traceIn( "symbolicOwnerDocumentIds()", collectionName );

        try {
            if( this.collectionDatabase == null ) {
                //log.traceOut( "symbolicOwnerDocumentIds()", "no collection" ); 
                return undefined;
            }

            const result : string[] = [];

            const symbolicOwnersProperties = this.properties( {
                includePropertyTypes: [PropertyTypes.SymbolicOwners]
            } as PropertiesSelector ) as Map<string,SymbolicOwnersProperty<DatabaseDocument>>;
    
            //log.debug( "symbolicOwnerDocumentIds()", "read symbolic owner properties", symbolicOwnersProperties.size );
     
            for( const symbolicOwnersProperty of symbolicOwnersProperties.values() ) {

                const symbolicOwnersReferenceHandles = symbolicOwnersProperty.referenceHandles();

                for( const symbolicOwnersReferenceHandle of symbolicOwnersReferenceHandles.values() ) { 

                    const path = symbolicOwnersReferenceHandle.path;

                    const pathElements = path.startsWith("/") ?
                        path.substring(1).split("/") : path.split("/");  // remove leading "/" and split the rest

                    for (let i = 0; i < pathElements.length; i++) {

                        if (collectionName == null && i % 2 === 1) { // every uneven path element is a document,

                            //log.traceOut( "symbolicOwnerDocumentIds()", "found no collectionName", pathElements[i] );

                            if(!result.includes( pathElements[i] )) {
                                result.push(pathElements[i]);
                            }
                        }
                        else if (i > 0 && pathElements[i - 1] === collectionName) {

                            //log.traceOut( "symbolicOwnerDocumentIds()", "found collectionName", pathElements[i] );
                            if(!result.includes( pathElements[i] )) {
                                result.push(pathElements[i]);
                            }
                        }
                    }
                }
            }

            //log.traceOut( "symbolicOwnerDocumentIds()", result );
            return result;   

        } catch( error ) {
            log.warn( "symbolicOwnerDocumentIds()", "Error owner document IDs", error );

            throw new Error( (error as any).message );
        }
    }

    ownerPath( collectionName? : string ) : string | undefined {

        //log.traceIn( "ownerDocumentPath()", collectionName );

        try {
            if( this.collectionDatabase == null ) {
                //log.traceOut( "ownerDocumentPath()", "no collection" );
                return undefined;
            }

            if( collectionName == null ) {

                const ownerDocumentPath = this.collectionDatabase.owner() != null ? 
                    this.collectionDatabase.owner()!.path() : undefined;

                //log.traceOut( "ownerDocumentPath()", "no collection name", ownerDocumentPath );
                return ownerDocumentPath;
            }
 
            /*
            if( this.collectionDatabase.collectionName()=== collectionName && this.id.value() != null ) {

                const ownerDocumentPath = this.databasePath();

                //log.traceOut( "ownerDocumentPath()", "this document is the owner", ownerDocumentPath );
                return ownerDocumentPath;
            }
            */

            let path = this.collectionDatabase.path();

            let ownerDocumentPath = "";

            let result : string | undefined;

            const pathElements = path.startsWith("/") ? 
                path.substring(1).split("/") : path.split("/");  // remove leading "/" and split the rest

            for( let i = 0; i < pathElements.length; i++ ) {

                ownerDocumentPath += "/" + pathElements[i];

                if( i > 0 && pathElements[i-1] === collectionName ) {

                    //log.trace( "ownerDocumentPath()", "found", ownerDocumentPath );
                    result = ownerDocumentPath;                
                }
            }

            //log.traceOut( "ownerDocumentPath()", result );
            return result;   

        } catch( error ) {
            log.warn( "ownerDocumentPath()", "Error owner document path", error );

            throw new Error( (error as any).message );
        }
    }

    ownerPaths( collectionName? : string ) : string[] | undefined {

        //log.traceIn( "ownerDocumentPaths()", collectionName );

        try {
            if( this.collectionDatabase == null ) {
                //log.traceOut( "ownerDocumentPaths()", "no collection" );
                return undefined;
            }

            const result : string[] = [];

            let path = this.collectionDatabase.path();

            const pathElements = path.startsWith("/") ? 
                path.substring(1).split("/") : path.split("/");  // remove leading "/" and split the rest

            let ownerDocumentPath = "";

            for( let i = 0; i < pathElements.length; i++ ) {

                ownerDocumentPath += "/" + pathElements[i];

                if( collectionName == null && i % 2 === 1 ) { // every uneven path element is a document,

                    //log.traceOut( "ownerDocumentPaths()", "add no collection name", ownerDocumentPath );
                    result.push( ownerDocumentPath );                
                }
                else if( i > 0 && pathElements[i-1] === collectionName ) {

                    //log.traceOut( "ownerDocumentPaths()", "add", ownerDocumentPath );
                    result.push( ownerDocumentPath );          
                }
            }

            if( result.length > 0 ) {
                //log.traceOut( "ownerDocumentPaths()", result );
                return result;  
            }

            //log.traceOut( "ownerDocumentPaths()", "not found" );
            return undefined;   

        } catch( error ) {
            log.warn( "ownerDocumentId()", "Error document paths", error );

            throw new Error( (error as any).message );
        }
    }


    emptyOwnerDocument( collectionName? : string ) : DatabaseDocument | undefined {

        //log.traceIn( "("+this.collectionDatabase+")", "ownerDocument()", collectionName );

        try {

            const path = this.ownerPath( collectionName );

            if( path == null ) {
                //log.traceOut( "("+this.collectionDatabase.documentName+")", "emptyOwnerDocument()", "not found" );
                return undefined;
            }

            const result = databaseServiceFactory!.get().databaseFactory.newDocumentFromUri( path ) as DatabaseDocument;

            //log.traceOut( "("+this.collectionDatabase.documentName+")", "emptyOwnerDocument()", result.referenceHandle().title );
            return result;

        } catch( error ) {
            
            log.warn( "("+this.name.value()!+")", "emptyOwnerDocument()", "Error reading owner document", error );

            throw new Error( (error as any).message );
        }
    }

    emptyOwnerDocuments( collectionName? : string ) : DatabaseDocument[] | undefined {

        //log.traceIn( "("+this.collectionDatabase+")", "emptyOwnerDocuments()", collectionName );

        try {

            const paths = this.ownerPaths( collectionName );

            if( paths == null ) {
                //log.traceOut( "("+this.collectionDatabase.documentName+")", "emptyOwnerDocuments()", "not found" );
                return undefined;
            }

            const result : DatabaseDocument[] = [];

            for( const path of paths ) {

                const databaseDocument = 
                    databaseServiceFactory!.get().databaseFactory.newDocumentFromUri( path ) as DatabaseDocument;

                if( databaseDocument == null ) {
                    throw new Error( "Document in owner path does not exist: " + path );
                }
                result.push( databaseDocument );
            }


            //log.traceOut( "("+this.collectionDatabase.documentName+")", "emptyOwnerDocuments()", result.size() );
            return result;

        } catch( error ) {
            
            log.warn( "("+this.name.value()!+")", "emptyOwnerDocuments()", "Error reading owner documentd", error );

            throw new Error( (error as any).message );
        }
    }

    async ownerDocument( collectionName? : string ) : Promise<DatabaseDocument | undefined> {

        //log.traceIn( "("+this.collectionDatabase+")", "ownerDocument()", collectionName );

        try {
            const path = this.ownerPath( collectionName );

            if( path == null ) {
                //log.traceOut( "("+this.collectionDatabase.documentName+")", "ownerDocument()", "not found" );
                return undefined;
            }

            const result = await databaseServiceFactory!.get().databaseFactory.documentFromUri( path ) as DatabaseDocument;

            //log.traceOut( "("+this.collectionDatabase.documentName+")", "ownerDocument()", result.referenceHandle() );
            return result;

        } catch( error ) {
            
            log.warn( "("+this.name.value()!+")", "ownerDocument()", "Error reading owner document", error );

            throw new Error( (error as any).message );
        }
    }

    async ownerDocuments( collectionName? : string ) : Promise<DatabaseDocument[] | undefined> {

        //log.traceIn( "("+this.collectionDatabase+")", "ownerDocuments()", collectionName );

        try {

            const paths = this.ownerPaths( collectionName );

            if( paths == null ) {
                //log.traceOut( "("+this.collectionDatabase.documentName+")", "ownerDocuments()", "not found" );
                return undefined;
            }

            const result : DatabaseDocument[] = [];

            for( const path of paths ) {

                const databaseDocument = 
                    await databaseServiceFactory!.get().databaseFactory.documentFromUri( path ) as DatabaseDocument;

                if( databaseDocument == null ) {
                    throw new Error( "Document in owner path does not exist: " + path );
                }
                result.push( databaseDocument );

            }

            //log.traceOut( "("+this.collectionDatabase.documentName+")", "ownerDocuments()", result.size() );
            return result;

        } catch( error ) {
            
            log.warn( "("+this.name.value()!+")", "ownerDocuments()", "Error reading owner documentd", error );

            throw new Error( (error as any).message );
        }
    }

    ownerCollection( collectionName? : string ) : CollectionDatabase<DatabaseDocument> | undefined {

        //log.traceIn( "("+this.collectionDatabase+")", "ownerCollection()", {collectionName} );

        try {

            if( collectionName == null ) {
                //log.traceOut( "("+this.collectionDatabase.documentName+")", "ownerCollection()", this.collectionDatabase );
                return this.collectionDatabase;
            }

            const path = this.ownerPath( collectionName );

            if( path == null ) {

                const result = 
                    databaseServiceFactory!.get().databaseFactory.collectionDatabaseFromCollectionName( 
                        collectionName ) as CollectionDatabase<DatabaseDocument>;

                //log.traceOut( "("+this.collectionDatabase.documentName+")", "ownerCollection()", "use root collection" );
                return result;
            }
            
            const result = 
                databaseServiceFactory!.get().databaseFactory.collectionFromUri( 
                    path ) as CollectionDatabase<DatabaseDocument>;

            //log.traceOut( "("+this.collectionDatabase.documentName+")", "ownerCollection()", result.databasePath() );
            return result;

        } catch( error ) {
            
            log.warn( "("+this.name.value()!+")", "ownerCollection()", "Error reading owner collection", error );

            throw new Error( (error as any).message );
        }
    }

    ownerCollections( collectionName? : string ) : CollectionDatabase<DatabaseDocument>[] | undefined {

        log.traceIn( "ownerCollections()", {collectionName} );

        try {
            if( this.collectionDatabase == null ) {
                //log.traceOut( "ownerCollections()", "no collection" );
                return undefined;
            }

            const result : CollectionDatabase<DatabaseDocument>[] = [];

            let path = this.collectionDatabase.path();

            const pathElements = path.startsWith("/") ? 
                path.substring(1).split("/") : path.split("/");  // remove leading "/" and split the rest

            let collectionPath = "";

            for( let i = 0; i < pathElements.length; i++ ) {

                collectionPath += "/" + pathElements[i];

                if( collectionName == null && i % 2 === 0 ) { // every even path element is a collection,

                    const collectionDatabase = 
                        databaseServiceFactory!.get().databaseFactory.collectionFromUri( collectionPath ) as CollectionDatabase<DatabaseDocument>;

                    //log.traceOut( "ownerCollections()", "found no collectionName", pathElements[i] );
                    result.push( collectionDatabase );   
                }
                else if( collectionName === pathElements[i] ) {

                    const collectionDatabase = 
                        databaseServiceFactory!.get().databaseFactory.collectionFromUri( collectionPath ) as CollectionDatabase<DatabaseDocument>;

                    //log.traceOut( "ownerCollections()", "found with collectionName", pathElements[i] );
                    result.push( collectionDatabase );                
                }
            }

            //log.traceOut( "ownerCollections()", result.length > 0 ? result.length : undefined );
            return result.length > 0 ? result : undefined;   

        } catch( error ) {
            log.warn( "ownerCollections()", "Error database path", error );

            throw new Error( (error as any).message );
        }
    }

    ownerCollectionGroup( collectionName? : string ) : CollectionGroupDatabase<DatabaseDocument> | undefined {

        //log.traceIn( "("+this.collectionDatabase+")", "ownerCollectionGroup()", {collectionName} );

        try {
            const path = this.ownerPath( 
                collectionName != null ? collectionName : this.collectionDatabase.collectionName());

            if( path == null ) {

                const result = 
                    databaseServiceFactory!.get().databaseFactory.collectionGroupDatabaseFromCollectionName( 
                        collectionName != null ? 
                            collectionName : 
                            this.collectionDatabase.collectionName()) as CollectionGroupDatabase<DatabaseDocument>;

                //log.traceOut( "("+this.collectionDatabase.documentName+")", "ownerCollectionGroup()", "use root collection" );
                return result;
            }
            
            const result = 
                databaseServiceFactory!.get().databaseFactory.collectionGroupFromUri( 
                    path ) as CollectionGroupDatabase<DatabaseDocument>;

            //log.traceOut( "("+this.collectionDatabase.documentName+")", "ownerCollectionGroup()", result.databasePath() );
            return result;

        } catch( error ) {
            
            log.warn( "("+this.name.value()!+")", "ownerCollectionGroup()", "Error reading owner collection", error );

            throw new Error( (error as any).message );
        }
    }

    ownerCollectionGroups( collectionName? : string ) : CollectionGroupDatabase<DatabaseDocument>[] | undefined {

        log.traceIn( "ownerCollectionGroups()", {collectionName} );

        try {

            const result : CollectionGroupDatabase<DatabaseDocument>[] = [];

            let path = this.collectionDatabase.path();

            const pathElements = path.startsWith("/") ? 
                path.substring(1).split("/") : path.split("/");  // remove leading "/" and split the rest

            let collectionPath = "";

            for( let i = 0; i < pathElements.length; i++ ) {

                collectionPath += "/" + pathElements[i];

                if( collectionName == null && i % 2 === 0 ) { // every even path element is a collection,

                    const collectionGroupDatabase = 
                        databaseServiceFactory!.get().databaseFactory.collectionGroupFromUri( 
                            collectionPath ) as CollectionGroupDatabase<DatabaseDocument>;

                    //log.traceOut( "ownerCollectionGroups()", "found no collectionName", pathElements[i] );
                    result.push( collectionGroupDatabase );   
                }
                else if( collectionName === pathElements[i] ) {

                    const collectionGroupDatabase = 
                        databaseServiceFactory!.get().databaseFactory.collectionGroupFromUri( 
                            collectionPath ) as CollectionGroupDatabase<DatabaseDocument>;

                    //log.traceOut( "ownerCollectionGroups()", "found with collectionName", pathElements[i] );
                    result.push( collectionGroupDatabase );                
                }
            }

            //log.traceOut( "ownerCollectionGroups()", result.length > 0 ? result.length : undefined );
            return result.length > 0 ? result : undefined;   

        } catch( error ) {
            log.warn( "ownerCollectionGroups()", "Error database path", error );

            throw new Error( (error as any).message );
        }
    }

    parentDatabases( collectionName : string, 
        options? : { 
            nearestIsCollectionGroup? : boolean, 
            includeRootCollection? : boolean }  ) : Database<DatabaseDocument>[] | undefined {

        //log.traceIn( "parentDatabases()", {collectionName}, {nearestAsCollectionGroup} );

        try {

            const result : Database<DatabaseDocument>[] = [];

            let nearestFound = false;

            const parentCollectionProperties = this.parentCollectionProperties( collectionName );

            if (parentCollectionProperties != null && parentCollectionProperties.length > 0) {

                for( const parentCollectionProperty of parentCollectionProperties ) {

                    if( !nearestFound && !!options?.nearestIsCollectionGroup ) {
                        result.push( parentCollectionProperty.collectionGroup()! );
                    }
                    else {
                        result.push( parentCollectionProperty.collection() );
                    }
                    nearestFound = true;
                }
            }

            if( !!options?.includeRootCollection ) {
            
                if( !nearestFound && !!options?.nearestIsCollectionGroup ) {

                    const rootCollectionGroup = 
                        databaseServiceFactory!.get().databaseFactory.collectionGroupDatabaseFromCollectionName( 
                            collectionName ) as CollectionGroupDatabase<DatabaseDocument>;

                    if( rootCollectionGroup.allowRootCollection ) {
                        result.push( rootCollectionGroup );
                    }
                }
                else {
                    const rootCollection =
                        databaseServiceFactory!.get().databaseFactory.collectionDatabaseFromCollectionName(
                            collectionName) as CollectionDatabase<DatabaseDocument>;

                    if (rootCollection.allowRootCollection) {
                        result.push(rootCollection);
                    }
                }
            }

            //log.traceOut("parentDatabases()", result.length > 0 ? result : undefined);
            return result.length > 0 ? result : undefined; 

        } catch( error ) {
            log.warn( "parentDatabases()", error );

            throw new Error( (error as any).message );
        }
    }


    parentCollection( collectionName : string ) : CollectionDatabase<DatabaseDocument> | undefined {

        //log.traceIn( "parentCollection()", {collectionName} );

        try {

            const parentCollectionProperties = this.parentCollectionProperties( collectionName );

            if (parentCollectionProperties != null && parentCollectionProperties.length > 0) {
    
                const collection = parentCollectionProperties[0].collection();

                //log.traceOut( "parentCollection()", collectionGroup.databasePath() ); 
                return collection;
            }

            const rootCollection =
                databaseServiceFactory!.get().databaseFactory.collectionDatabaseFromCollectionName(
                    collectionName) as CollectionDatabase<DatabaseDocument>;

            if (rootCollection.allowRootCollection) {

                //log.traceOut( "parentCollection()", collection.databasePath() ); 
                return rootCollection;
            }      

            //log.traceOut( "parentCollection()", "not found" );
            return undefined

        } catch (error) {

            log.warn("parentCollection()", "Error reading parent collection", error);

            throw new Error( (error as any).message );
        }
    }

    parentCollections( collectionName : string ) : CollectionDatabase<DatabaseDocument>[] | undefined {

       // log.traceIn( "parentCollections()", {collectionName} );

        try {

            const result : CollectionDatabase<DatabaseDocument>[] = [];

            const parentCollectionProperties = this.parentCollectionProperties( collectionName );

            if (parentCollectionProperties != null && parentCollectionProperties.length > 0) {

                for( const parentCollectionProperty of parentCollectionProperties ) {

                    result.push( parentCollectionProperty.collection() );
                }
            }

            const noOwnerCollection = 
                databaseServiceFactory!.get().databaseFactory.collectionDatabaseFromCollectionName( 
                    collectionName ) as CollectionDatabase<DatabaseDocument>;

            if( noOwnerCollection.allowRootCollection ) {
                result.push( noOwnerCollection );
            }

            //log.traceOut("parentCollections()", result.length > 0 ? result : undefined);
            return result.length > 0 ? result : undefined; 

        } catch( error ) {
            log.warn( "parentCollections()", error );

            throw new Error( (error as any).message );
        }
    }

    parentCollectionGroup( collectionName : string ) : CollectionGroupDatabase<DatabaseDocument> | undefined {

        //log.traceIn( "("+this.collectionDatabase+")", "parentCollectionGroup()", {collectionName} );

        try {

            const parentCollectionProperties = this.parentCollectionProperties( collectionName );

            if (parentCollectionProperties != null && parentCollectionProperties.length > 0) {
    
                const collectionGroup = parentCollectionProperties[0].collectionGroup();

                //log.traceOut( "parentCollectionGroup()", collectionGroup.databasePath() ); 
                return collectionGroup;
            }

            const rootCollectionGroup =
                databaseServiceFactory!.get().databaseFactory.collectionGroupDatabaseFromCollectionName(
                    collectionName) as CollectionGroupDatabase<DatabaseDocument>;

            if (rootCollectionGroup.allowRootCollection) {

                //log.traceOut( "parentCollectionGroup()", rootCollectionGroup.databasePath() ); 
                return rootCollectionGroup;
            }
            
            //log.traceOut( "parentCollectionGroup()", "not found" );
            return undefined

        } catch (error) {

            log.warn("(" + this.name.value()! + ")", "parentCollection()", "Error reading parent collection", error);

            throw new Error( (error as any).message );
        }
    }

    parentCollectionGroups( collectionName : string ) : CollectionGroupDatabase<DatabaseDocument>[] | undefined {

       // log.traceIn( "parentCollectionGroups()", {collectionName} );

        try {

            const result : CollectionGroupDatabase<DatabaseDocument>[] = [];

            const parentCollectionProperties = this.parentCollectionProperties( collectionName );

            if (parentCollectionProperties != null && parentCollectionProperties.length > 0) {

                for( const parentCollectionProperty of parentCollectionProperties ) {

                    if( !!parentCollectionProperty.allowCollectionGroup() ) {
                        result.push( parentCollectionProperty.collectionGroup()! ); 
                    }
                }
            }

            const rootCollectionGroup = 
                databaseServiceFactory!.get().databaseFactory.collectionGroupDatabaseFromCollectionName( 
                    collectionName ) as CollectionGroupDatabase<DatabaseDocument>;

            if( rootCollectionGroup.allowRootCollection ) {
                result.push( rootCollectionGroup );
            }

            //log.traceOut("parentCollectionGroups()", result.length > 0 ? result : undefined);
            return result.length > 0 ? result : undefined; 

        } catch( error ) {
            log.warn( "parentCollectionGroups()", error );

            throw new Error( (error as any).message );
        }
    }

    parentCollectionProperty( collectionName : string ) : CollectionProperty<DatabaseDocument> | undefined {

        //log.traceIn( "parentCollectionProperty()", {collectionName} );

        try {

            const ownerDocument = this.emptyOwnerDocument();

            if (ownerDocument != null) {

                const databaseProperty = ownerDocument.property(collectionName);

                if (databaseProperty != null) {

                    if (databaseProperty.type === PropertyTypes.Collection) {

                        log.traceOut( "parentCollectionProperty()", "found in owner:", ownerDocument.path() );
                        return databaseProperty as CollectionProperty<DatabaseDocument>;
                    }
                }
            }

            //log.traceOut( "parentCollectionProperty()", "not found" );
            return undefined

        } catch (error) {

            log.warn( "parentCollectionProperty()", "Error reading parent collection", error);

            throw new Error( (error as any).message );
        }
    }

    parentCollectionProperties( collectionName : string ) : CollectionProperty<DatabaseDocument>[] | undefined {

        //log.traceIn( "parentCollectionProperties()", {collectionName} );

        try {

            const result : CollectionProperty<DatabaseDocument>[] = [];

            const ownerDocuments = this.emptyOwnerDocuments();

            if (ownerDocuments != null && ownerDocuments.length > 0) {

                for (let i = ownerDocuments.length - 1; i >= 0; i--) {

                    const ownerDocument = ownerDocuments[i];

                    const databaseProperty = ownerDocument.property(collectionName);

                    if (databaseProperty != null) {

                        if (databaseProperty.type === PropertyTypes.Collection) {

                            result.push( databaseProperty as CollectionProperty<DatabaseDocument> );
                        }
                    }
                }
            }

            //log.traceOut("parentCollectionProperties()", result.length > 0 ? result : undefined);
            return result.length > 0 ? result : undefined; 

        } catch( error ) {
            log.warn( "parentCollectionProperties()", error );

            throw new Error( (error as any).message );
        }
    }

    recordName() : string {
        return this.name.value()!; 
    }

    async duplicate() : Promise<DatabaseDocument> {

        log.traceIn( "("+this.collectionDatabase.collectionName()+")", "duplicate()", this );

        try {
            const copy = databaseServiceFactory!.get().databaseFactory.newDocument( 
                    this.collectionDatabase, this.uri() ) as GenericDatabaseDocument;
            
            await copy.copyProperties( this );  // Ensures deep copy

            log.traceOut( "("+this.collectionDatabase.collectionName()+")", "duplicate()", copy );
            return copy;

        } catch( error ) {
            
            log.warn( "("+this.name.value()!+")", "duplicate()", "Error copying database object", error );

            throw new Error( (error as any).message );
        }
    }

    async subscribe( monitor : Monitor ) : Promise<void>
    {
        //log.traceIn( "usubscribe()", monitor );

        try {

            if( this.id.value() == null ) {
                return;
            }

            monitor.objectIdsFilter = [this.referenceHandle().path];

            await super.subscribe( monitor );

            //log.traceOut( "subscribe()", "return", result );

        } catch( error ) {
            log.warn( "subscribe()", "Error adding new monitor", monitor, error );

            throw new Error( (error as any).message );
        }
    }

    protected async monitor( newMonitor : Monitor ): Promise<void> {

        //log.traceIn( "monitor()" );  
        
        try {
            if( this.id.value() == null ) {
                throw new Error("Cannot monitor document without ID" );
            }

            const uri = this.uri();

            if( newMonitor.objectIdsFilter != null && 
                ( newMonitor.objectIdsFilter.length !== 1 || newMonitor.objectIdsFilter[0] !== uri ) ) {

                throw new Error("Document to monitor not held by this handle: " + newMonitor.objectIdsFilter );
            }
        
            await this.updateDatabaseSubscription();
  
            //log.traceOut( "("+this.collectionDatabase.collectionName()+")", "monitor()" );
  
        } catch( error ) {
            log.warn( "("+this.collectionDatabase.collectionName()+")", "monitor()", "Error monitoring document", error );
  
            throw new Error( (error as any).message );
        }
    }

    private async updateDatabaseSubscription(): Promise<void> {
        log.traceIn("(" + this.collectionDatabase.collectionName()+ ")", "updateDatabaseSubscription()", this);

        try {
            const uri = this.uri();

            if (super.isMonitoring() && uri != null && uri.length > 0) {

                const monitor = this.collectionDatabase.observer( this );

                if( monitor == null || 
                    JSON.stringify( monitor.objectIdsFilter ) !== JSON.stringify([uri]) ) {

                    log.debug("(" + this.collectionDatabase.collectionName()+ ")", "Updating database subscription for: " + uri);

                    await this.collectionDatabase.subscribe({
                        observer: this,
                        onNotify: this.onNotifyDocumentChange,
                        objectIdsFilter: [uri]
                    });
                }
            }
            else {
                log.debug("(" + this.collectionDatabase.collectionName()+ ")", "No database subscription for: " + uri);

                this.collectionDatabase.unsubscribe(this);
            }

            log.traceOut("(" + this.collectionDatabase.collectionName()+ ")", "updateDatabaseSubscription()", uri);

        } catch (error) {
            log.warn("(" + this.collectionDatabase.collectionName()+ ")", "monitor()", "Error subscribing to database", error);

            throw new Error( (error as any).message );
        }

    }
  
  
    protected async release(): Promise<void> {
  
        try {
            //log.traceIn( "("+this.collectionDatabase.collectionName+")", "release()" );
  
            this.collectionDatabase.unsubscribe( this );
  
            //log.traceOut( "("+this.collectionDatabase.collectionName+")", "release()" );
  
        } catch( error ) {
            
            log.warn( "("+this.collectionDatabase.collectionName+")", "release()", "Error releasing database objects", error );
  
            throw new Error( (error as any).message );
        }
    }

    protected onNotifyDocumentChange = async (observable: Observable,
        observation: Observation,
        objectId: string | null | undefined,
        object: object | null | undefined): Promise<void> => {

        log.traceIn( "onNotifyDocumentChange()", this, observation, objectId );
  
        try {

            if( objectId == null || 
                !databaseServiceFactory!.get().databaseFactory.equalUris( objectId, this.uri() ) ) {
                    
                log.traceOut( "onNotifyDocumentChange()", "Not for us", {objectId}, this.uri() );
                return;
            }

            const databaseDocument = object as DatabaseDocument;
    
            if( databaseDocument != null ) {

                await this.copyFrom( databaseDocument );  
            }
  
            await super.notify( observation, objectId, this );
  
            log.traceOut( "onNotifyDocumentChange()" );
  
        } catch( error ) {
            
            log.warn( "onNotifyDocumentChange()", "Error notifying database handle", this, error );
        }
    }


    fromRecord(record: Record<string,any>): void {
        //log.traceIn("fromRecord()", data);

        try {

            const id = this.id.value();

            if( record.id != null ) {
                delete record.id;  // Use actual path to evaluate id
            }

            if( record.path != null ) {
                delete record.path;  // Use actual path to evaluate id
            } 

            if( record[OwnerIds] != null ) {
                delete record[OwnerIds];  // We read this from the path
            } 

            super.fromRecord( record );

            if( this.id.value() == null ) {
                this.id.setValue( id );
            }

            //log.traceOut("fromRecord()", this);

        } catch (error) {

            log.warn("fromRecord()", "Error reading document", error);

            throw new Error( (error as any).message );
        }
    }

    async toRecord( force? : boolean ): Promise<Record<string, any>> {
        log.traceIn("toRecord()", {force})
        try {

            const record = await super.toRecord( force );

            record.path = this.path();

            let ownerIds = this.ownerIds();

            const symbolicOwnerIds = this.symbolicOwnerIds();

            if( symbolicOwnerIds != null ) {

                for( const symbolicOwnerId of symbolicOwnerIds ) {

                    if( ownerIds == null ) {
                        ownerIds = [];
                    }
                    
                    if( !ownerIds.includes( symbolicOwnerId ) ) {
                        ownerIds.push( symbolicOwnerId );
                    }
                }
            }

            const ownerDocuments = this.emptyOwnerDocuments(); 

            if( ownerDocuments != null ) {
                for( const ownerDocument of ownerDocuments.values() ) { 

                    if( ownerDocument.properties( { 
                        includePropertyTypes: [PropertyTypes.SymbolicOwners as PropertyType] } ).size > 0 ) {

                        await ownerDocument.read();
                    }

                    const ownerSymbolicOwnerIds = ownerDocument.symbolicOwnerIds();

                    if( ownerSymbolicOwnerIds != null ) {

                        for( const ownerSymbolicOwnerId of ownerSymbolicOwnerIds ) {

                            if( ownerIds == null ) {
                                ownerIds = [];
                            }
                            
                            if( !ownerIds.includes( ownerSymbolicOwnerId ) ) {
                                ownerIds.push( ownerSymbolicOwnerId );
                            }
                        }
                    }
                }
            }

            if (ownerIds != null && ownerIds.length > 0) {
                record[OwnerIds] = ownerIds;
            }
            else {
                record[OwnerIds] = [];
            }

            delete record.id;  // We don't store ID, only path 

            log.traceOut("toRecord()", {record} )
            return record;
        } catch (error) {
            log.warn("toRecord()", "Error writing database document to data", error);

            throw new Error( (error as any).message );
        }
    }

    matchFilter( params: { 
        from?: Date, 
        to?: Date, 
        matchHistoric? : boolean, 
        databaseFilters?: Map<string, DatabaseFilter>
        }): boolean {

        //log.traceIn("matchFilter()" );

        try {

            if (params.from != null && this.startDate.value() == null ) {

                //log.traceOut("matchFilter()", "document has no date" );
                return false;
            }

            if (params.from != null && this.startDate.compareValue(params.from) < 0) {

                //log.traceOut("matchFilter()", "before date range" );
                return false;
            }

            if (params.to != null && this.startDate.compareValue(params.to) > 0) {
                //log.traceOut("matchFilter()", "after date range" );
                return false;
            }

            if ( params.matchHistoric != null && !params.matchHistoric && 
                this.endDate.value() != null && this.endDate.value()!.getTime() <= new Date().getTime() ){
                //log.traceOut("matchFilter()", "matchHistoric" );
                return false;
            }

            if (params.databaseFilters != null) {
                for (const keyValuePair of params.databaseFilters) {

                    const propertyKey = keyValuePair[0];
                    const databaseFilter = keyValuePair[1];

                    const documentProperty = this.property(propertyKey);

                    if (documentProperty != null) {

                        if( documentProperty.type === PropertyTypes.Collection ) { 
                            continue;
                        }

                        if( !!databaseFilter.ignoreEmpty ) {

                            const value = documentProperty.value();
                            
                            if( value == null ) {
                                continue;
                            }

                            if( value.size != null && value.size === 0 ) {
                                continue;
                            }

                            if( value.length != null && value.length === 0 ) {
                                continue;
                            }
                        } 

                        //log.trace("matchFilter()", "documentProperty", propertyKey, documentProperty.value());

                        //log.trace("matchFilter()", "databaseFilter", propertyKey, databaseFilter.value );

                        switch (databaseFilter.comparator) {

                            case Comparators.Equal: {

                                const compare = documentProperty.compareValue(databaseFilter.value);

                                if (compare !== 0) {
                                    //log.traceOut("matchFilter()", "failed", databaseFilter.comparator, propertyKey);
                                    return false;
                                }
                                break;
                            }
                            case Comparators.GreaterThan: {

                                const compare = documentProperty.compareValue(databaseFilter.value);

                                if (compare <= 0) {
                                    //log.traceIn("matchFilter()", "failed", databaseFilter.comparator, propertyKey);
                                    return false;
                                }
                                break;
                            }
                            case Comparators.GreaterThanOrEqual: {

                                const compare = documentProperty.compareValue(databaseFilter.value);

                                if (compare < 0) {
                                    //log.traceIn("matchFilter()", "failed", databaseFilter.comparator, propertyKey);
                                    return false;
                                }
                                break;
                            }
                            case Comparators.LessThan: {

                                const compare = documentProperty.compareValue(databaseFilter.value);

                                if (compare >= 0) {
                                    //log.traceIn("matchFilter()", "failed", databaseFilter.comparator, propertyKey);
                                    return false;
                                }
                                break;
                            }
                            case Comparators.LessThanOrEqual: {

                                const compare = documentProperty.compareValue(databaseFilter.value);

                                if (compare >= 0) {
                                    //log.traceIn("matchFilter()", "failed", databaseFilter.comparator, propertyKey);
                                    return false;
                                }
                                break;
                            }
                            case Comparators.NotEqual: {

                                const compare = documentProperty.compareValue(databaseFilter.value);

                                if (compare === 0) {
                                    //log.traceIn("matchFilter()", "failed", databaseFilter.comparator, propertyKey);
                                    return false;
                                }
                                break;
                            }
                            case Comparators.Includes: {

                                if( !documentProperty.includesValue( databaseFilter.value ) ) {
                                    //log.traceIn("matchFilter()", "failed", databaseFilter.comparator, propertyKey);
                                    return false;
                                }

                                break;
                            }
                            case Comparators.IncludesAny: {

                                if( !documentProperty.includesValue( databaseFilter.value, true ) ) {
                                    //log.traceIn("matchFilter()", "failed", databaseFilter.comparator, propertyKey);
                                    return false;
                                }

                                break;
                            }
                            case Comparators.NotIncludes: {

                                if( documentProperty.includesValue( databaseFilter.value ) ) {
                                    //log.traceIn("matchFilter()", "failed", databaseFilter.comparator, propertyKey);
                                    return false;
                                }

                                break;

                            }
                            case Comparators.Exists: {
                    
                                const value = documentProperty.value();

                                if ( value == null ||
                                     (value.length != null && value.length === 0 ) ||
                                     (value.size != null && value.size === 0 ) ) {

                                    //log.traceIn("matchFilter()", "failed", databaseFilter.comparator, propertyKey);
                                    return false;
                                }
                                break;
                            }
                            case Comparators.NotExists:  {
                                
                                const value = documentProperty.value();

                                if (value != null &&
                                    (value.length == null || value.length > 0 ) &&
                                    (value.size == null || value.size > 0 ) ) {

                                    //log.traceIn("matchFilter()", "failed", databaseFilter.comparator, propertyKey);
                                    return false;
                                }
                                break;
                            }
                            default:
                                throw new Error( "Unexpected comparator: " + databaseFilter.comparator );
                        }
                    }
                }
            }

            //log.traceOut("matchFilter()", "passed" );
            return true;

        } catch (error) {
            log.warn("Error filtering document", error);

            throw new Error( (error as any).message );
        }
    }


    changesPropertiesSelector() : PropertiesSelector {

        return {

            //excludePropertyKeys: ["backReferences","lastChangedAt","lastChangedBy"],
            excludePropertyKeys: ["lastChangedAt","lastChangedBy"],

            excludePropertyTypes: [PropertyTypes.Collection]

        } as PropertiesSelector;
    }


    databaseAccess() : DatabaseAccess {

        if( this._databaseAccess != null ) {

            return this._databaseAccess;
        }

        const databaseAccess = databaseServiceFactory!.get().databaseAccessor.databaseAccess( this.uri() );   

        //log.traceInOut( "databaseAccess()", {databaseAccess} )
        return databaseAccess;
    }


    validate( propertiesSelector?: PropertiesSelector, markMissingProperties? : boolean  ): Map<string, Error> {

        //log.traceIn("validate()");

        try {

            let result = super.validate( propertiesSelector, markMissingProperties );

            if( this.startDate.value() != null && this.endDate.value() != null &&
                (this.startDate.isChanged() || this.startDate.isChanged()) &&
                this.startDate.compareTo( this.endDate ) > 0 ) {

                const error = new Error( "endDateBeforeStartDate" );
                
                this.endDate.error = error;
                
                result.set( this.endDate.key(), error );
            }
            else if( result.get( this.endDate.key() ) == null  ) { 

                delete this.endDate.error;
            }
            
            //log.traceOut("validate()", result);
            return result;

        } catch (error) {
            log.warn("Error validating properties for database document", error);

            throw new Error( (error as any).message );
        }
    }

    async updateKeys() : Promise<boolean> {

        //log.traceIn("updateKeys()");

        try {
            const keysDatabaseCollection = this.parentCollection( KeysCollectionName );

            if( keysDatabaseCollection == null || keysDatabaseCollection.owner() == null ) {

                //log.traceOut("updateKeys()", "no owner");
                return false;
            }

            const result = await keysDatabaseCollection.owner()!.updateKeys();

            //log.traceOut("updateKeys()", {result});
            return result;

        } catch (error) {
            log.warn("Error updating keys for database document", error);

            throw new Error( (error as any).message );
        }
    }

    async disableKeys() : Promise<boolean> {

        try {
            const keysDatabaseCollection = this.parentCollection( KeysCollectionName );

            if( keysDatabaseCollection == null || keysDatabaseCollection.owner() == null ) {

                //log.traceOut("disableKeys()", "no owner");
                return false;
            }

            const result = await keysDatabaseCollection.owner()!.disableKeys();

            //log.traceOut("disableKeys()", {result});
            return result;

        } catch (error) {
            log.warn("Error disabling keys for database document", error);

            throw new Error( (error as any).message );
        }
    }


    readonly collectionDatabase : CollectionDatabase<DatabaseDocument>;

    readonly startDate: DateProperty;

    readonly endDate : DateProperty;

    readonly changes : CollectionProperty<Change>;

    readonly lastChangedBy : ReferenceProperty<User>;

    readonly lastChangedAt : DateProperty;

    readonly archived : BooleanProperty;

    readonly archivedAt : DateProperty;

}
