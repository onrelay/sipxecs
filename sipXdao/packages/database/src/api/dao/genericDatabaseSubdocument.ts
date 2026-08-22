import { Monitor, Observation, UniqueId } from "@sipxdao/common";
import { AbstractDatabaseObject } from "./abstractDatabaseObject";
import { DatabaseDocument } from "./databaseDocument";
import { DatabaseObject } from "./databaseObject";
import { DatabaseSubdocument } from "./databaseSubdocument";
import { ReferenceHandle } from "./referenceHandle";
import { CollectionDatabase } from "./collectionDatabase";
import { CollectionGroupDatabase } from "./collectionGroupDatabase";
import { Database } from "./database";
import { CollectionProperty } from "../properties/collectionProperty";
import { DatabaseAccess } from "../types/databaseAccess";
import { log } from "./abstractDatabaseService";
import { GenericDatabaseDocument } from "./genericDatabaseDocument";

export abstract class GenericDatabaseSubdocument extends AbstractDatabaseObject implements DatabaseSubdocument {

    constructor( subdocumentName : string, parent : DatabaseObject, parentKey : string ) {

        super( subdocumentName, parent );

        this.parentKey = parentKey;

        //log.traceInOut( "constructor()", parent.title, parentKey );
    }

    path() : string | undefined {
        return this.parentDocument().path();
    }

    uri() : string | undefined {
        return this.parentDocument().uri();
    }
  
    referenceHandle() : ReferenceHandle<DatabaseDocument> | undefined {
        return this.parentDocument().referenceHandle();
    }

    recordName() : string {
        return this.name.value()!;
    }


    ownerId( collectionName? : string ) : string | undefined {

        //log.traceIn( "ownerDocumentId()", collectionName );

        try {

            const parentDocument = this.parentDocument();

            const id = parentDocument.ownerId( collectionName );

            //log.traceOut( "ownerDocumentId()", id );
            return id;   

        } catch( error ) {
            log.warn( "ownerDocumentId()", "Error reading owner document id", error );

            throw new Error( (error as any).message );
        }
    }

    ownerIds( collectionName? : string ) : string[] | undefined {

        //log.traceIn( "ownerDocumentIds()", collectionName );

        try {

            const parentDocument = this.parentDocument();

            const ids = parentDocument.ownerIds( collectionName );

            //log.traceOut( "ownerDocumentIds()", ids );
            return ids;   

        } catch( error ) {
            log.warn( "ownerDocumentIds()", "Error reading owner document ids", error );

            throw new Error( (error as any).message );
        }
    }

    symbolicOwnerIds( collectionName? : string ) : string[] | undefined { 

        //log.traceIn( "symbolicOwnerDocumentIds()", collectionName );

        try {

            const parentDocument = this.parentDocument();

            const ids = parentDocument.symbolicOwnerIds( collectionName );

            //log.traceOut( "symbolicOwnerDocumentIds()", ids );
            return ids;   

        } catch( error ) {
            log.warn( "symbolicOwnerDocumentIds()", "Error reading owner document ids", error );

            throw new Error( (error as any).message );
        }
    }

    ownerPath( collectionName? : string ) : string | undefined {

        //log.traceIn( "ownerDocumentPath()", collectionName );

        try {

            const parentDocument = this.parentDocument();

            const path = parentDocument.ownerPath( collectionName );

            //log.traceOut( "ownerDocumentPath()", path );
            return path;   

        } catch( error ) {
            log.warn( "ownerDocumentPath()", "Error reading owner document path", error );

            throw new Error( (error as any).message );
        }
    }

    ownerPaths( collectionName? : string ) : string[] | undefined {

        //log.traceIn( "ownerDocumentPaths()", collectionName );

        try {

            const parentDocument = this.parentDocument();

            const paths = parentDocument.ownerPaths( collectionName );

            //log.traceOut( "ownerDocumentPaths()", paths );
            return paths;   

        } catch( error ) {
            log.warn( "ownerDocumentPaths()", "Error reading owner document paths", error );

            throw new Error( (error as any).message );
        }
    }

    emptyOwnerDocument( collectionName? : string ) : DatabaseDocument | undefined {

        log.traceIn( "emptyOwnerDocument()", collectionName );

        try {

            const parentDocument = this.parentDocument();

            const result = parentDocument.emptyOwnerDocument( collectionName );

            log.traceOut( "emptyOwnerDocument()", result );
            return result;

        } catch( error ) {
            
            log.warn( "emptyOwnerDocument()", "Error reading owner document", error );

            throw new Error( (error as any).message );
        }
    }

    emptyOwnerDocuments( collectionName? : string ) : DatabaseDocument[] | undefined {

        log.traceIn( "emptyOwnerDocuments()", collectionName );

        try {

            const parentDocument = this.parentDocument();

            const result = parentDocument.emptyOwnerDocuments( collectionName );

            log.traceOut( "emptyOwnerDocuments()", result );
            return result;

        } catch( error ) {
            
            log.warn( "emptyOwnerDocuments()", "Error reading owner documents", error );

            throw new Error( (error as any).message );
        }
    }

    async ownerDocument( collectionName? : string ) : Promise<DatabaseDocument | undefined> {

        log.traceIn( "ownerDocument()", collectionName );

        try {

            const parentDocument = this.parentDocument();

            const result = await parentDocument.ownerDocument( collectionName );

            log.traceOut( "ownerDocument()", result );
            return result;

        } catch( error ) {
            
            log.warn( "ownerDocument()", "Error reading owner document", error );

            throw new Error( (error as any).message );
        }
    }

    async ownerDocuments( collectionName? : string ) : Promise<DatabaseDocument[] | undefined> {

        log.traceIn( "ownerDocuments()", collectionName );

        try {

            const parentDocument = this.parentDocument();

            const result = await parentDocument.ownerDocuments( collectionName );

            log.traceOut( "ownerDownerDocumentsocument()", result );
            return result;

        } catch( error ) {
            
            log.warn( "ownerDocument()", "Error reading owner documents", error ); 

            throw new Error( (error as any).message );
        }
    }

    ownerCollection( collectionName? : string ) : CollectionDatabase<DatabaseDocument> | undefined {

        //log.traceIn( "ownerCollection()", collectionName );

        try {

            const parentDocument = this.parentDocument();

            const result = parentDocument.ownerCollection( collectionName );

            //log.traceOut( "ownerCollection()", result );
            return result;

        } catch( error ) {
            
            log.warn( "ownerCollection()", "Error reading owner databases", error );

            throw new Error( (error as any).message );
        }
    }

    ownerCollections( collectionName? : string ) : CollectionDatabase<DatabaseDocument>[] | undefined {

        //log.traceIn( "ownerCollections()", collectionName );

        try {

            const parentDocument = this.parentDocument();

            const result = parentDocument.ownerCollections( collectionName );

            //log.traceOut( "ownerCollections()", result );
            return result;

        } catch( error ) {
            
            log.warn( "ownerCollections()", "Error reading owner databases", error );

            throw new Error( (error as any).message );
        }
    }

    ownerCollectionGroup( collectionName? : string ) : CollectionGroupDatabase<DatabaseDocument> | undefined {

        //log.traceIn( "ownerCollectionGroup()", collectionName );

        try {

            const parentDocument = this.parentDocument();

            const result = parentDocument.ownerCollectionGroup( collectionName );

            //log.traceOut( "ownerCollectionGroup()", result );
            return result;

        } catch( error ) {
            
            log.warn( "ownerCollectionGroup()", "Error reading owner databases", error );

            throw new Error( (error as any).message );
        }
    }

    ownerCollectionGroups( collectionName? : string ) : CollectionGroupDatabase<DatabaseDocument>[] | undefined {

        //log.traceIn( "ownerCollectionGroups()", collectionName );

        try {

            const parentDocument = this.parentDocument();

            const result = parentDocument.ownerCollectionGroups( collectionName );

            //log.traceOut( "ownerCollectionGroups()", result ); 
            return result;

        } catch( error ) {
            
            log.warn( "ownerCollectionGroups()", "Error reading owner databases", error );

            throw new Error( (error as any).message );
        }
    }

    parentDatabases( collectionName : string, 
        options? : { 
            nearestIsCollectionGroup? : boolean, 
            includeRootCollection? : boolean }    ) : Database<DatabaseDocument>[] | undefined {

        //log.traceIn( "parentDatabases()", {collectionName}, {nearestAsCollectionGroup} );

        try {

            const parentDocument = this.parentDocument();

            const result = parentDocument.parentDatabases( collectionName, options );

            //log.traceOut( "parentCollection()", result );

            return result;

        } catch( error ) {
            log.warn( "parentDatabases()", error );

            throw new Error( (error as any).message );
        }
    }


    parentCollection( collectionName : string ) : CollectionDatabase<DatabaseDocument> | undefined {

        //log.traceIn( "parentCollection()", collectionName );

        try {

            const parentDocument = this.parentDocument();

            const result = parentDocument.parentCollection( collectionName );

            //log.traceOut( "parentCollection()", result );
            return result;

        } catch( error ) {
            
            log.warn( "parentCollection()", "Error reading parent collection", error );

            throw new Error( (error as any).message );
        }
    }

    parentCollections( collectionName : string ) : CollectionDatabase<DatabaseDocument>[] | undefined {

        //log.traceIn( "parentCollections()", collectionName );

        try {

            const parentDocument = this.parentDocument();

            const result = parentDocument.parentCollections( collectionName );

            //log.traceOut( "parentCollections()", result );
            return result;

        } catch( error ) {
            
            log.warn( "parentCollections()", "Error reading parent collections", error );

            throw new Error( (error as any).message );
        }
    }

    parentCollectionGroup( collectionName : string ) : CollectionGroupDatabase<DatabaseDocument> | undefined {

        //log.traceIn( "parentCollectionGroup()", collectionName );

        try {

            const parentDocument = this.parentDocument();

            const result = parentDocument.parentCollectionGroup( collectionName );

            //log.traceOut( "parentCollectionGroup()", result );
            return result;

        } catch( error ) {
            
            log.warn( "parentCollectionGroup()", "Error reading parent collection group", error );

            throw new Error( (error as any).message );
        }
    }

    parentCollectionGroups( collectionName : string ) : CollectionGroupDatabase<DatabaseDocument>[] | undefined {

        //log.traceIn( "parentCollections()", collectionName );

        try {

            const parentDocument = this.parentDocument();

            const result = parentDocument.parentCollectionGroups( collectionName );

            //log.traceOut( "parentCollectionGroups()", result );
            return result;

        } catch( error ) {
            
            log.warn( "parentCollectionGroups()", "Error reading parent collection groups", error );

            throw new Error( (error as any).message );
        }
    }

    parentCollectionProperty( collectionName : string ) : CollectionProperty<DatabaseDocument> | undefined {

        //log.traceIn( "parentCollectionProperty()", collectionName );

        try {

            const parentDocument = this.parentDocument();

            const result = parentDocument.parentCollectionProperty( collectionName );

            //log.traceOut( "parentCollectionProperty()", result );
            return result;

        } catch( error ) {
            
            log.warn( "parentCollectionProperty()", "Error reading parent collection property", error );

            throw new Error( (error as any).message );
        }
    }

    parentCollectionProperties( collectionName : string ) : CollectionProperty<DatabaseDocument>[] | undefined {

        //log.traceIn( "parentCollectionProperties()", collectionName );

        try {

            const parentDocument = this.parentDocument();

            const result = parentDocument.parentCollectionProperties( collectionName );

            //log.traceOut( "parentCollections()", result );
            return result;

        } catch( error ) {
            
            log.warn( "parentCollectionProperties()", "Error reading parent collection properties", error );

            throw new Error( (error as any).message );
        }
    }


    protected async monitor( newMonitor : Monitor): Promise<void> {}

    databaseAccess() : DatabaseAccess {
        return this.parent!.databaseAccess();
    }

    parentDocument() : DatabaseDocument {

        let parent = this.parent;

        while( parent != null && parent instanceof GenericDatabaseSubdocument ) {

            parent = parent.parent;
        }

        if( parent == null ) {
            throw new Error( "Subdocument has no parent document ");
        }

        if( !(parent instanceof GenericDatabaseDocument) ) {
            throw new Error( "Subdocument has unkown parent document type");
        }

        return (parent as GenericDatabaseDocument);
    }

    /*
    backReferenceKey( propertyKey : string ) : string {

        let subdocumentKeys = this.parentKey;

        let parent = this.parent;

        while( parent != null && parent instanceof DatabaseSubdocument ) {

            subdocumentKeys = parent.parentKey + "." + subdocumentKeys;

            parent = parent.parent;
        }

        if( parent == null ) {
            throw new Error( "Subdocument has no parent document ");
        }

        if( !(parent instanceof DatabaseDocument) ) {
            throw new Error( "Subdocument has unkown parent document type");
        }

        const backReferenceKey = parent.documentName() + "." + subdocumentKeys + "." + parent.id.value();

        return backReferenceKey;
    }
    */

    async onCreate() : Promise<void> {

        //log.traceIn( "onCreate()" );

        try {

            await super.onCreate(); 

            if( this.id.value() == null ) {
                this.id.setValue( UniqueId.get() ); 
            }
    
            //log.traceOut( "onCreate()" );
  
        } catch( error ) {
            
            log.warn( "onCreate()", "Error handling created notification", error );
  
            throw new Error( (error as any).message );
        }
    }
 
  
    protected async release(observationFilter?: Observation[], objectIdsFilter?: string[]): Promise<void> {}

    readonly parentKey : string;

}
