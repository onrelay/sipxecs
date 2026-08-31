import { DatabaseRecord } from "../../core/types/databaseRecord";
import { Monitor, Observable, Observation, Observations } from "@dao/common";
import { GenericDatabaseDocument } from "../../core/impl/genericDatabaseDocument";
import { AbstractDatabaseProperty } from "../../core/base/abstractDatabaseProperty";
import { Database } from "../../core/spec/database";
import { DatabaseDocument } from "../../core/spec/databaseDocument";
import { DatabaseObject } from "../../core/spec/databaseObject";
import { DocumentsDatabase } from "../../core/spec/documentsDatabase";
import { ReferenceHandle } from "../../core/impl/referenceHandle";
import { DocumentsProperty } from "../spec/documentsProperty";
import { PropertyType } from "../../core/defs/propertyType";
import { DocumentsDatabaseImpl } from "../../core/impl/documentsDatabaseImpl";
import { Template } from "../../documents/spec/template";
import { DatabaseAccess } from "../../core/impl/databaseAccess";
import { databaseServiceFactory } from "../../core/impl/databaseServiceFactory";
import { TemplatedDocument } from "../../core/spec/templatedDocument";
import { log } from "../../core/base/abstractDatabaseService";
import { TemplatesCollection } from "../../core/spec/databaseService";

export abstract class AbstractDocumentsProperty<DerivedDocument extends DatabaseDocument> 
    extends AbstractDatabaseProperty<Map<string,ReferenceHandle<DerivedDocument>>> implements DocumentsProperty<DerivedDocument> {
 
    constructor( parent : DatabaseObject,
        type : PropertyType,  
        onSelectDatabases? : () => (Database<DerivedDocument> | undefined)[],
        reciprocalKey? : keyof DerivedDocument ) {   

        super( parent, type ); 

        //log.traceIn( "constructor()", parent.title, reciprocalKey);

        try {
            if( reciprocalKey != null && !(parent instanceof GenericDatabaseDocument) ) {
                throw new Error( "Reciprocal keys can only be used for documents" );
            }

            this._onSelectDatabases = onSelectDatabases;

            this.reciprocalKey = reciprocalKey as string;

            this.onNotify = this.onNotify.bind(this);

            //log.traceOut( "constructor()" );

        } catch( error ) {
            
            log.warn( "constructor()", "Error initializing document reference", error );

            throw new Error( (error as any).message );
        }
    }

    count() : number {

        const result = this.handles().size;

        //log.traceInOut( "count()", result );
        return result;
    }

    hasDocument( documentPath : string ) : boolean {

        const result = this.handles().has( 
            databaseServiceFactory!.get().databaseFactory.uriToPath( documentPath )! ); 

        //log.traceInOut( "hasDocument()", result );
        return result;
    }

    hasDocumentId( documentId : string ) : boolean {

        try {
            //log.traceIn( "hasDocumentId()" );

            for( const handle of this.handles().values() ) {

                const handleDocumentId = databaseServiceFactory!.get().databaseFactory.documentId( handle.uri )!;

                if( handleDocumentId === documentId ) {
                    return true;
                }
            }

            //log.traceOut( "ids()", result );
            return false;   

        } catch( error ) {
            
            log.warn( "ids()", "Error reading database object ids", error );

            throw new Error( (error as any).message );
        }
    }

    documentsDatabase() : DocumentsDatabase<DerivedDocument> {

        if( this._documentsDatabase == null ) {
            this._documentsDatabase = new DocumentsDatabaseImpl( this ); 
        }
        return this._documentsDatabase;
    }

    value() : Map<string,ReferenceHandle<DerivedDocument>> | undefined {
        const values = this.referenceHandles();

        return values.size > 0 ? values : undefined;
    }


    setValue( value : Map<string,ReferenceHandle<DerivedDocument>> | undefined ) : void {
        if( value == null ) {
            this.clearDocuments();
        }
        else {
            this.setDocuments( value );
        }
    }

    referenceHandles() : Map<string,ReferenceHandle<DerivedDocument>> {

        try {
            //log.traceIn( "referenceHandles()" );

            let result = new Map<string,ReferenceHandle<DerivedDocument>>();

            //log.trace( "referenceHandles()", this.handles() );

            this.handles().forEach( handle => {

                const referenceHandle = this.referenceHandle( handle.path )!;

                result.set( referenceHandle.uri, referenceHandle );
            })
            
            //log.traceOut( "referenceHandles()", {result} );
            return result;    

        } catch( error ) {
            
            log.warn( "referenceHandles()", "Error reading reference handles", error );

            throw new Error( (error as any).message );
        }
    }

    referenceHandle( documentPath: string ) : ReferenceHandle<DerivedDocument> | undefined {

        try {
            //log.traceIn( "referenceHandle()",{documentPath} );

            const handle = this.handles().get(
                databaseServiceFactory!.get().databaseFactory.uriToPath( documentPath )! );

            if (handle == null) {
                //log.traceOut( "referenceHandles()", "not found' );
                return undefined;
            }

            const result = handle.copy();

            //log.traceOut( "referenceHandles()", result );
            return result;   

        } catch( error ) {
            
            log.warn( "referenceHandles()", "Error reading reference handles", error );

            throw new Error( (error as any).message );
        }
    }


    paths() : string[] {

        try {
            //log.traceIn( "documentPaths()" );

            let result : string[] = [];
            
            if( this.handles().size > 0 ) {
                result = Array.from( this.handles().keys() );
            }

            //log.traceOut( "documentPaths()", result );
            return result;   

        } catch( error ) {
            
            log.warn( "documentPaths()", "Error reading database object ids", error );

            throw new Error( (error as any).message );
        }
    }

    ids() : string[] {

        try {
            //log.traceIn( "ids()" );

            let result : string[] = [];
            
            this.handles().forEach( handle => {

                const documentId = databaseServiceFactory!.get().databaseFactory.documentId( handle.path )!;

                result.push( documentId );

            })

            //log.traceOut( "ids()", result );
            return result;   

        } catch( error ) {
            
            log.warn( "ids()", "Error reading database object ids", error );

            throw new Error( (error as any).message );
        }
    }

    title(documentPath: string): string | undefined {

        //log.traceIn( "documentTitle()", documentPath );

        try {
            const referenceHandle = this.referenceHandle( documentPath );

            if( referenceHandle == null ) {
                //log.traceOut( "documentTitle()", undefined );
                return undefined;
            }
    
            //log.traceOut( "documentTitle()", handle.title );
            return referenceHandle.referenceHandleTitle;

        } catch( error ) {
            log.warn( "documentTitle()", "Error reading database document title", error );

            throw new Error( (error as any).message );
        }
    }

    emptyDocuments(): Map<string,DerivedDocument> {
 
        //log.traceIn( "documents()" );

        let result = new Map<string,DerivedDocument>();
        try {

            for( const handle of this.handles().values() ) {
            
                const databaseDocument = 
                    databaseServiceFactory!.get().databaseFactory.newDocumentFromUri( handle.path ) as DerivedDocument;

                if( databaseDocument == null ) {

                    this.handles().delete( handle.path );
                    //log.traceOut( "emptyDocuments()", "not found", undefined );
                    continue;
                }

                result.set( handle.path, databaseDocument );
            }
            
            //log.traceOut( "documents()", result );
            return result;  

        } catch( error ) {

            log.warn( "documents()", "Error reading database objects", error );

            throw new Error( (error as any).message );
        }    
    }
    
    async documents(): Promise<Map<string,DerivedDocument>> {
 
        //log.traceIn( "documents()" );

        let result = new Map<string,DerivedDocument>();
        try {

            for( const handle of this.handles().values() ) {

                let databaseDocument = handle.databaseDocument as DerivedDocument | undefined;

                if ( databaseDocument == null) {

                    databaseDocument = 
                        await databaseServiceFactory!.get().databaseFactory.documentFromUri( handle.path ) as DerivedDocument;

                    if( databaseDocument != null ) {

                        this.handles().set( 
                            handle.path, 
                            databaseDocument.referenceHandle() as ReferenceHandle<DerivedDocument>
                        );
                    }
                    else {
                        this.handles().delete( handle.path );
                    }
                }  

                if( databaseDocument != null ) {
                    result.set( handle.path, databaseDocument );
                }
            }
            
            //log.traceOut( "documents()", result );
            return result;  

        } catch( error ) {

            log.warn( "documents()", "Error reading database objects", error );

            throw new Error( (error as any).message );
        }    
    }

    emptyDocument(documentPath: string): DerivedDocument | undefined {

        //log.traceIn( "newDocument()", documentPath );

        try {

            let handle = this.handles().get( 
                databaseServiceFactory!.get().databaseFactory.uriToPath( documentPath )! ) ;

            if( handle == null ) {
                //log.traceOut( "newDocument()", undefined );
                return undefined;
            }
        
            const databaseDocument = 
                databaseServiceFactory!.get().databaseFactory.newDocumentFromUri( documentPath ) as DerivedDocument;

            if( databaseDocument == null ) {
                this.handles().delete( 
                    databaseServiceFactory!.get().databaseFactory.uriToPath( documentPath )! );

                //log.traceOut( "newDocument()", "not foune", undefined );
                return undefined;
            }

            //databaseDocument.title.setValue( handle.title );

            //log.traceOut( "newDocument()", document );
            return databaseDocument;

        } catch( error ) {
            log.warn( "newDocument()", "Error reading database document", error );

            throw new Error( (error as any).message );
        }
    }

   async document(documentPath: string ): Promise<DerivedDocument | undefined> {

        //log.traceIn( "document()", documentPath );

        try {

            let handle = this.handles().get( 
                databaseServiceFactory!.get().databaseFactory.uriToPath( documentPath )! ) ;

            if( handle == null ) {
                log.traceOut( "document()", undefined );
                return undefined;
            }

            let databaseDocument;
            
            if( handle.databaseDocument == null ) {

                databaseDocument = await databaseServiceFactory!.get().databaseFactory.documentFromUri( documentPath ) as DerivedDocument;

                if( databaseDocument == null ) {

                    this.handles().delete( 
                        databaseServiceFactory!.get().databaseFactory.uriToPath(documentPath )! );

                    throw new Error( "Reference not found with path: " + documentPath );
                }
    
                this.handles().set( 
                    databaseServiceFactory!.get().databaseFactory.uriToPath( documentPath )!, 
                    databaseDocument.referenceHandle() as ReferenceHandle<DerivedDocument> );
            }
            else {
                databaseDocument = handle.databaseDocument as DerivedDocument;
            }
            

            //log.traceOut( "document()", document );
            return handle!.databaseDocument! as DerivedDocument;

        } catch( error ) {
            log.warn( "document()", "Error reading database document", error );

            throw new Error( (error as any).message );
        }
    }

    
    setDocument( referenceHandle : ReferenceHandle<DerivedDocument> ): void {

       log.traceIn( "setDocument()", referenceHandle.referenceHandleTitle );

        try {
            log.debug( "setDocument()", {referenceHandle} );

            const oldHandles = this.handles();

            const newHandles = new Map<string,ReferenceHandle<DerivedDocument>>( oldHandles );

            newHandles.set( 
                referenceHandle.uri, 
                referenceHandle );

            log.debug( "setDocument()", {newHandles}, "set" );

            if( this.onChange( oldHandles, newHandles ) ) {

                this._handles = newHandles;

                super.notify( Observations.Create as Observation, referenceHandle.uri, referenceHandle );

                this.updateDatabaseSubscription(); 

                this.onChanged( oldHandles, this._handles );                  
            }

            log.traceOut( "setDocument()" );

        } catch( error ) {
            log.warn( "setDocument()", "Error adding database object", error );

            throw new Error( (error as any).message );
        }
    }

    setDocuments( referenceHandles : Map<string,ReferenceHandle<DerivedDocument>> ): void {
        
        log.traceIn( "setDocuments()", referenceHandles );

        try {

            const oldHandles = this.handles();

            const newHandles = new Map<string,ReferenceHandle<DerivedDocument>>();
        
            referenceHandles.forEach( referenceHandle => {

                if( referenceHandle.uri != null ) {

                    newHandles.set( 
                        referenceHandle.uri, 
                        referenceHandle.copy() 
                    );
                }
            });

            if( !this.onChange( oldHandles, newHandles ) ) {

                log.traceOut( "setDocuments()" );
                return;

            }

            this._handles = newHandles;

            super.notify( Observations.Create as Observation, this.parentDocument().path() + "/" + this.key(), referenceHandles );

            this.updateDatabaseSubscription();

            this.onChanged( oldHandles, newHandles );

            log.traceOut( "setDocuments()" );

        } catch( error ) {
            log.warn( "setDocuments()", "Error adding database objects", error );

            throw new Error( (error as any).message );
        }
    }

    removeDocument( documentPath: string): boolean {
        log.traceIn( "removeDocument()", documentPath );

        try {

            const oldHandles = this.handles();

            const newHandles = new Map<string,ReferenceHandle<DerivedDocument>>( oldHandles );

            if( newHandles.delete( 
                databaseServiceFactory!.get().databaseFactory.uriToPath( documentPath )! ) ) {

                if( this.onChange( oldHandles, newHandles ) ) {

                    this._handles = newHandles;

                    super.notify( Observations.Delete as Observation, documentPath );

                    this.updateDatabaseSubscription();

                    this.onChanged( oldHandles, newHandles );

                    log.traceOut( "removeDocument()", "removed" );
                    return true;
                }
            }

            log.traceOut( "removeDocument()", "not found" );
            return false;

        } catch( error ) {
            log.warn( "removeDocument()", "Error removing database object", error );

            throw new Error( (error as any).message );
        }
    }

    clearDocuments(): void {

        //log.traceIn( "clearDocuments()" );

        try {
            const handles = this.handles();

            if( handles != null && handles.size > 0 ) {

                if( this.onChange( handles, undefined ) ) { 

                    handles.clear();

                    super.notify( Observations.Delete as Observation, this.parentDocument().path() + "/" + this.key() );

                    this.updateDatabaseSubscription();

                    this.onChanged( handles, undefined );
                }
            }

            //log.traceOut( "clearDocuments()" );

        } catch( error ) {
            log.warn( "clearDocuments()", "Error adding database object", error );

            throw new Error( (error as any).message );
        }
    }

    newDocument(): DerivedDocument | undefined {

        try {

            if( this.parentDocument().collectionDatabase.collectionName() === TemplatesCollection ) {

                return databaseServiceFactory!.get().databaseFactory.newTemplatedDocument( 
                    this.parentDocument() as Template<TemplatedDocument> ) as DerivedDocument;
            }

            return this.primaryDatabase()?.newDocument();

        } catch( error ) {

            log.warn("path()", "Error creating new document on references ", error );

            throw new Error( (error as any).message );
        }  
    }

    databaseAccess(): DatabaseAccess {

        //log.traceIn( "userAccess()" );

        try {
            const sources = this.databases();

            if( sources == null || sources.length === 0 ) {
                //log.traceOut( "userAccess()", "no sources" );
                return DatabaseAccess.allowNone(); 
            }

            //log.traceOut( "userAccess()", "defer to parent document" );
            return this.parentDocument().databaseAccess();

        } catch( error ) {

            log.warn("path()", "Error getting database access on references ", error );

            throw new Error( (error as any).message );
        }  
    }


    collectionName() : string | undefined {

        return this.primaryDatabase()?.collectionName();
    }

    queryDocumentName() : string | undefined {

        return this.primaryDatabase()?.queryDocumentName();
    }

    documentNames() : string[] | undefined {

        return this.primaryDatabase()?.documentNames();

    }

    queryTemplatePath() : string | undefined {

        return this.primaryDatabase()?.queryTemplatePath();
    }


    async options(): Promise<Map<string,ReferenceHandle<DerivedDocument>> | undefined> {
 
        log.traceIn( "options()" );

        const options = this.referenceHandles();

        log.traceOut( "options()", options );
        return options.size > 0 ? options : undefined; 
    }


    async select( params? : { 
        filterValues? : boolean,
        fetch: boolean } ): Promise<Map<string,ReferenceHandle<DerivedDocument>> | undefined> {
 
        log.traceIn( "select()", {params} );

        try {
            const sources = this.databases();

            if( sources == null || sources.length === 0 ) {
                log.traceOut( "referenceOptions()", "empty reference" );
                return undefined;
            }

            const result = new Map<string,ReferenceHandle<DerivedDocument>>();

            const handles = this.handles();

            for( const source of sources ) {

                if( source == null ) {
                    continue;
                }

                const referenceHandles = await source.referenceHandles();

                for( const referenceHandle of referenceHandles.values() ) {

                    if( !!params?.filterValues && handles.has( referenceHandle.uri )) {
                        continue;
                    }

                    if( !!params?.fetch ) {
                        await referenceHandle.fetch();
                    } 

                    result.set( referenceHandle.uri, referenceHandle )
                }
            }

            log.traceOut( "select()", result );
            return result;  

        } catch( error ) {

            log.warn( "select()", "Error reading database objects", error );

            throw new Error( (error as any).message );
        }    
    }

    databases() : (Database<DerivedDocument> | undefined)[] | undefined {

        //log.traceIn( "databases()" );

        if( this._databases !== undefined ) {
            //log.traceOut( "databases()", "not defined" );
            return this._databases == null ? undefined : this._databases;
        }

        const sources = this._onSelectDatabases != null ? this._onSelectDatabases() : undefined;

        if( sources != null ) {
            for( const source of sources ) {

                if( source != null && source.databaseAccess().allowRead ) {
    
                    if( this._databases == null ) {
                        this._databases = [];
                    }
    
                    this._databases.push( source );
                }
            }
        }

        if( sources == null || sources.length === 0 ) {
            this._databases = null;
            //log.traceOut( "databases()", "no sources" );
            return undefined; 
        }
            
        //log.traceOut( "databases()", "sources length",  this._sources?.length );
        return this._databases;
    }

    primaryDatabase() : Database<DerivedDocument> | undefined {

        try {
            const sources = this.databases();

            if( sources == null || sources.length === 0 ) {
                return undefined;
            }

            for( const source of sources ) {

                if( source == null ) {
                    continue;
                }

                return source;
            }

            return undefined;

        } catch( error ) {

            log.warn("primaryDatabase()", "Error getting primary source", error );

            throw new Error( (error as any).message );
        } 
    }


    compareTo( other : DocumentsProperty<DerivedDocument> ) : number {

        if( this.handles().size > (other as AbstractDocumentsProperty<DerivedDocument>).handles().size ) {
            return 1;
        }

        if( this.handles().size < (other as AbstractDocumentsProperty<DerivedDocument>).handles().size ) {
            return -1;
        }

        for( const handle of this.handles() ) {

            const path = handle[0];

            if( !(other as AbstractDocumentsProperty<DerivedDocument>).handles().has( 
                    databaseServiceFactory!.get().databaseFactory.uriToPath( path )! )  ) {
                return 1;
            }
        }

        return 0;
    }

    compareValue( referenceHandles : Map<string,ReferenceHandle<DerivedDocument>> | undefined  ) : number {

        if( referenceHandles == null ) {
            return 1;
        }

        if( this.handles().size > referenceHandles.size ) {
            return 1;
        }

        if( this.handles().size < referenceHandles.size ) {
            return -1;
        }

        for( const referenceHandle of referenceHandles.values() ) {

            if( referenceHandle.uri == null || !this.handles().has( referenceHandle.uri) ) {
                return 1;
            }
        }

        return 0;
    }

    includes( other : DocumentsProperty<DerivedDocument>, matchAny? : boolean  ) : boolean {
        return this.includesValue( other.referenceHandles(), matchAny );
    }

    includesValue( referenceHandles : Map<string,ReferenceHandle<DerivedDocument>> | undefined, matchAny? : boolean ) : boolean {

        log.traceIn( "includesValue", this.handles(), {referenceHandles} )

        if( referenceHandles == null || referenceHandles.size === 0 ) {
            log.traceOut( "includesValue", "No input" )
            return false;
        }

        if( this.handles().size < referenceHandles.size ) {

            log.traceOut( "includesValue", "Too big" )
            return false;
        }

        for( const referenceHandle of referenceHandles.values() ) {

            const compare = referenceHandle != null && this.handles().has( referenceHandle.uri );

            if( compare && !!matchAny ) {
                return true; 
            }

            if( !compare && !matchAny ) {
                return false;
            }
        }

        log.traceOut( "includesValue", "Found" )
        return !matchAny;
    }

    protected async monitor(newMonitor: Monitor): Promise<void> {
        log.traceIn("monitor()");

        try {
            const sources = this.databases();

            if( sources == null || sources.length !== 1 ) {

                throw new Error( "Reference monitoring requires a single database source");
            }  
            
            this._sourceDatabase = sources[0];

            await this.updateDatabaseSubscription();

            if( newMonitor.onNotify != null ) {

                const result = await this.documents();

                await newMonitor.onNotify( this, Observations.Create as Observation, this._sourceDatabase!.uri(), result  );
                
            }

            log.traceOut("monitor()"); 

        } catch (error) {

            log.warn("monitor()", "Error monitoring documents", error);
        }
    }

    protected async release(): Promise<void> {

        try {
            //log.traceIn( "release()" );

            if (this._sourceDatabase != null) {

                await this._sourceDatabase.unsubscribe(this);
            }

            //log.traceOut( "release()" );

        } catch (error) {

            log.warn("release()", "Error releasing database objects", error);

        }
    }

    private async updateDatabaseSubscription(): Promise<void> {

        //log.traceIn( "updateDatabaseSubscription()" ); 

        try {

            if( this._sourceDatabase != null && super.isMonitoring() && this.handles().size > 0) {

                let monitor = this._sourceDatabase.observer( this );

                if( monitor == null || 
                    JSON.stringify( monitor.objectIdsFilter ) !== JSON.stringify(Array.from(this.handles().keys()) ) ) {

                    monitor = {
                        observer: this,
                        onNotify: this.onNotify,
                        observationFilter: super.observationFilter(),
                        objectIdsFilter: Array.from(this.handles().keys())
                    } as Monitor;

                    await this._sourceDatabase.subscribe(monitor);
                }
            }
            else {
                if (this._sourceDatabase != null) {
                    await this._sourceDatabase.unsubscribe(this);
                }
            }

            //log.traceOut( "updateDatabaseSubscription()" );  
        } catch (error) {

            log.warn("release()", "Error releasing database objects", error);
        }
    }

    protected onNotify = async (observable: Observable,
        observation: Observation,
        objectId?: string,
        object?: any): Promise<void> => {

        log.traceIn("onNotify()", observation, objectId, object);

        try {
            switch (observation) {
                case Observations.Create:
                    {
                        if (databaseServiceFactory!.get().databaseFactory.isUriDatabase(objectId!)) {

                            const result = object as Map<string, DerivedDocument>;

                            for (const databaseDocument of result.values()) {

                                let handle = this.handles().get(databaseDocument.path());

                                if (handle != null) {

                                    if (handle.databaseDocument != null) {

                                        await handle.databaseDocument.copyFrom(databaseDocument);
                                    }
                                    else {
                                        handle.databaseDocument = databaseDocument;
                                    }
                                }
                            } 
                        }
                        else {
                            const databaseDocument = object as DerivedDocument;

                            const documentPath = databaseDocument.path();

                            let handle = this.handles().get(documentPath);

                            if (handle != null) {

                                if (handle.databaseDocument != null) {

                                    await handle.databaseDocument.copyFrom(object! as DerivedDocument);
                                }
                                else {
                                    handle.databaseDocument = object! as DerivedDocument;
                                }
                            }
                        }
                        break;
                    }
                case Observations.Update: 
                    {
                        const databaseDocument = object as DerivedDocument;

                        const documentPath = databaseDocument.path();

                        let handle = this.handles().get(documentPath);

                        if (handle != null) {

                            if (handle.databaseDocument != null) {

                                await handle.databaseDocument.copyFrom(databaseDocument);
                            }
                            else {
                                handle.databaseDocument = databaseDocument;
                            }
                        }
                        break;
                    }
                case Observations.Delete: {

                    const databaseDocument = object as DerivedDocument;

                    const documentPath = databaseDocument.path();

                    let handle = this.handles().get(documentPath);

                    if (handle != null) {

                        this.handles().delete(documentPath)
                    }
                    else {
                        log.warn("onNotify()",
                            "Ignoring update for document not held by this handle", documentPath)
                    }
                    break;
                }

                default:
                    break;
            }
            await super.notify(observation, objectId, object);

            log.traceOut("onNotify()");

        } catch (error) {

            log.warn("onNotify()", "Error notifying observers", error);
        }
    }

    async toRecord( documentRecord: DatabaseRecord, force? : boolean ) : Promise<void> { 

        log.traceIn( "toRecord()" );

        try {

            const handles = this.handles();

            if( handles.size === 0 ) {
                log.traceOut(  "toRecord()",  undefined );
                return;
            }

            const documentReferences = [] as any[];

            for( const handle of handles.values()) {

                documentReferences.push( handle.documentReference! );
            }

            documentRecord[this.key()] = documentReferences;
            
            log.traceOut( "toRecord()", {documentReferences});

        } catch( error ) {
            log.warn( "toRecord()", "onNotify()", "Error notifying observers", error );

            throw new Error( (error as any).message );
        }
    }

    fromRecord( documentRecord: DatabaseRecord): void {

        //log.traceIn( "fromRecord()" );

        try {  
            this._handles = new Map<string,ReferenceHandle<DerivedDocument>>();

            const documentReferences = documentRecord[this.key()] as any[];

            if( documentReferences == null ) {
                //log.traceOut( "fromRecord()", this.key(), "No data" );
                return;
            }

            for( const documentReference of documentReferences ) {

                const referenceHandle = new ReferenceHandle<DerivedDocument>({
                    path: documentReference.path,
                    uri: documentReference.uri,
                    documentReference: documentReference 
                });

                this._handles.set( referenceHandle.uri, referenceHandle );
            }
            
            //log.traceOut( "fromRecord()", this.key(), this._handles, this._encryptedReferencesData );
             
        } catch( error ) {
            log.warn( "fromRecord()", "Error reading reference ", error );

            throw new Error( (error as any).message );
        }
    }

    onChange( oldValue : Map<string,ReferenceHandle<DerivedDocument>>| undefined, 
        newValue : Map<string,ReferenceHandle<DerivedDocument>> | undefined ) : boolean {

        if( this.minEntries != null && newValue != null && newValue.size < this.minEntries ) {

            this.error = new Error( "propertyValueRejected" );

            if( oldValue == null || newValue.size >= oldValue.size ) {
                return true;
            }
            else {
                return false;
            }
        }

        if( this.maxEntries != null && newValue != null && newValue.size > this.maxEntries ) {
            
            this.error = new Error( "propertyValueRejected" );
            return false;
        }

        return super.onChange( oldValue, newValue );
    }

    onChanged( oldValue : Map<string,ReferenceHandle<DerivedDocument>> | undefined, 
        newValue : Map<string,ReferenceHandle<DerivedDocument>>| undefined ) : void {

        try {
            //log.traceIn( "onChanged()", {oldValue}, {newValue} );

           this.setPreviousValues( this.previousValues() != null ? 
                this.previousValues().concat(newValue) : [newValue] );

            if( this.minEntries != null && newValue != null && newValue.size >= this.minEntries ) {

                delete this.error;
            }
 
            (this.parent as GenericDatabaseDocument).onChanged( this, oldValue, newValue ); 
            // Note this is async, so will not wait for processing / block

            //log.traceOut( "onChanged()", {oldValue}, {newValue} ); 
            
        } catch( error ) {

            log.warn( "onChanged()", "Error checking change", error );
            
            throw new Error("Error checking change: " + (error as any).message );
        } 
    }

    protected handles(): Map<string,ReferenceHandle<DerivedDocument>> {

        //log.traceIn( "handles()" );

        try {
            //log.traceOut( "handles()");
            return this._handles;

        } catch( error ) {
            log.warn( "handles()", "Error reading handles ", error );

            throw new Error( (error as any).message );
        }
    }

    minEntries? : number;

    maxEntries? : number;

    reciprocalKey?: string;

    protected readonly _onSelectDatabases? : () => (Database<DerivedDocument> | undefined)[];

    protected _databases? : (Database<DerivedDocument> | undefined)[] | null;

    private _documentsDatabase? : DocumentsDatabase<DerivedDocument>; 

    private _sourceDatabase : Database<DerivedDocument> | undefined;

    //private _encryptedReferencesData? : any; 

    private _handles = new Map<string,ReferenceHandle<DerivedDocument>>();  
    

}
