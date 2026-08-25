import { Observation, Observations } from "@dao/common";
import { AbstractDatabaseProperty } from "../../core/base/abstractDatabaseProperty";
import { DatabaseDocument } from "../../core/spec/databaseDocument";
import { DatabaseObject } from "../../core/spec/databaseObject";
import { ReferenceHandle } from "../../core/impl/referenceHandle";
import { OwnerProperty } from "../spec/ownerProperty";
import { PropertyType, PropertyTypes } from "../../core/defs/propertyType";
import { Database } from "../../core/spec/database";
import { CollectionGroupDatabase } from "../../core/spec/collectionGroupDatabase";
import { CollectionDatabase } from "../../core/spec/collectionDatabase";
import { databaseServiceFactory } from "../../core/impl/databaseServiceFactory";
import { IdsSuffix, IdSuffix } from "../../core/spec/databaseService";
import { log } from "../../core/base/abstractDatabaseService";

export class OwnerPropertyImpl<DerivedDocument extends DatabaseDocument> 
    extends AbstractDatabaseProperty<ReferenceHandle<DerivedDocument>> implements OwnerProperty<DerivedDocument> {

        constructor( parent : DatabaseObject, collectionName : string, documentName : string ) {
        
        super( parent, PropertyTypes.Owner as PropertyType );  
        
        //log.traceIn( "constructor()", parent.title, collectionName );

        try {

            this._collectionName = collectionName;

            this._documentName = documentName;

           //log.traceOut( "constructor()" ); 
        } catch( error ) { 
            
            log.warn( "constructor()", "Error initializing document owner", error );

            throw new Error( (error as any).message );
        }
    }

    collectionName() : string {
        return this._collectionName;
    }

    documentName() : string {
        return this._documentName;
    }


    value() : ReferenceHandle<DerivedDocument> | undefined {
        return this.referenceHandle();
    }


    setValue( value : ReferenceHandle<DerivedDocument> | undefined ) : void {

        if( value == null ) {
            this.clearDocuments();
        }
        else {
            this.setDocument( value );
        }
    }

    count() : number {

        const result = this._handles != null ? this._handles.size : 0;

        //log.traceInOut( "count()", result );
        return result;
    }

    hasDocument( uri : string ) : boolean {

        const handles = this.handles();

        if( handles == null ) {
            //log.traceInOut( "hasDocument()", "No handles" );
            return false;
        }

        const result = handles.has( 
            databaseServiceFactory!.get().databaseFactory.uriToPath( uri )! );

        //log.traceInOut( "hasDocument()", result );
        return result;
    }

    id() : string | undefined {

        const ids = this.ids();

        if( ids == null || ids.length === 0 ) {
            return undefined;
        }

        return ids[ids.length -1];
    }
 

    ids() : string[] | undefined {

        try {
            //log.traceIn( "ids()" );

            let result : string[] = [];

            const handles = this.handles();

            if( handles == null ) {
                //log.traceOut( "ids()", "No handles" );
                return undefined;
            }

            handles.forEach( handle => {

                if( handle.path == null ) {
                    throw new Error( "Unexpected empty path");
                }

                const documentId = 
                    databaseServiceFactory!.get().databaseFactory.documentId( handle.path );

                result.push( documentId! );
            })
            
            //log.traceOut( "ids()", result );
            return result;   

        } catch( error ) {
            
            log.warn( "paths()", "Error reading database object ids", error );

            throw new Error( (error as any).message );
        }
    }

    path( ignoreCleared? : boolean ) : string | undefined {

        //log.traceIn( "path()" );

        const paths = this.paths( ignoreCleared );

        if( paths == null || paths.length === 0 ) {
            return undefined;
        }

        const path = paths[paths.length -1];

        //log.traceOut( "path()", path );
        return path;
    }
 

    paths( ignoreCleared? : boolean ) : string[] | undefined {

        try {
            //log.traceIn( "paths()" );

            const handles = this.handles( ignoreCleared );

            if( handles == null ) {
                //log.traceOut( "paths()", "No handles" );
                return undefined;
            }

            let result = Array.from( handles.keys() );

            //log.traceOut( "paths()", result );
            return result;   

        } catch( error ) {
            
            log.warn( "paths()", "Error reading database object ids", error );

            throw new Error( (error as any).message );
        }
    }



    titles() : string[] | undefined {

        try {
            //log.traceIn( "titles()" );

            const referenceHandles = this.referenceHandles();

            if( referenceHandles == null ) {
                //log.traceOut( "titles()", "No handles" );
                return undefined;
            }

            let result : string[] = [];

            referenceHandles.forEach( referenceHandle => {
                result.push( referenceHandle.referenceHandleTitle != null ? referenceHandle.referenceHandleTitle : "" );
            })
            
            //log.traceOut( "titles()", result );
            return result;   

        } catch( error ) {
            
            log.warn( "titles()", "Error reading database object ids", error );

            return undefined;
        }
    }

    referenceHandle( ignoreCleared? : boolean ) : ReferenceHandle<DerivedDocument> | undefined {

        const referenceHandles = this.referenceHandles( ignoreCleared );

        if( referenceHandles == null || referenceHandles.size === 0 ) {
            return undefined;
        }

        let result : ReferenceHandle<DerivedDocument>;

        referenceHandles.forEach( referenceHandle => {

            if( result == null || referenceHandle.path.length > result.path.length ) {
                result = referenceHandle;
            }
        })

        return result!;
    }

    depth() : number | undefined {

        const handles = this.handles();

        if( handles == null || handles.size === 0 ) {
            return undefined;
        }

        return handles.size;
    }

    async documents(): Promise<Map<string,DerivedDocument> | undefined> {
 
        //log.traceIn( "documents()", monitor );

        try {
            const handles = this.handles();

            if( handles == null ) {
                //log.traceOut( "documents()", "No handles" );
                return undefined;
            }

            let result = new Map<string,DerivedDocument>();

            handles.forEach(async ( handle ) => {

                let databaseDocument = handle.databaseDocument as DerivedDocument | undefined;

                if ( databaseDocument == null) {

                    databaseDocument = 
                        await databaseServiceFactory!.get().databaseFactory.documentFromUri( handle.uri ) as DerivedDocument;

                    if( databaseDocument == null ) {

                        throw new Error( "Owner document does not exist with uri: " + handle.uri );
                    }

                    handles.set( handle.path, 
                        this.getNewHandle( handle.path, databaseDocument.uri(), databaseDocument.title.value(), databaseDocument ) );
                }  

                result.set( handle.path, databaseDocument );
                
            });
            //delete this._encryptedTitles;
            
            //log.traceOut( "documents()", result );
            return result;  

        } catch( error ) {

            log.warn( "documents()", "Error reading database objects", error );

            throw new Error( (error as any).message );
        }    
    }

    emptyDocument(): DerivedDocument | undefined {

        //log.traceIn( "emptyDocument()", documentPath );

        try {

            const path = this.path();

            if( path == null ) {
                //log.traceOut( "emptyDocument()", "No handles" );
                return undefined;
            }
        
            const databaseDocument = 
                databaseServiceFactory!.get().databaseFactory.newDocumentFromUri( path ) as DerivedDocument;
    
            //log.traceOut( "emptyDocument()", document );
            return databaseDocument;

        } catch( error ) {
            log.warn( "emptyDocument()", "Error reading database document", error );

            throw new Error( (error as any).message );
        }
    }

   async document( ignoreCleared? : boolean ): Promise<DerivedDocument | undefined> {

        //log.traceIn( "document()", documentPath );

        try {

            const path = this.path( ignoreCleared );

            if( path == null ) {
                //log.traceOut( "document()", "No handles" );
                return undefined;
            }

            const handles = this.handles( ignoreCleared )!;

            let handle = handles.get( path )!;

            let databaseDocument;

            if( handle.databaseDocument == null ) {

                databaseDocument = await databaseServiceFactory!.get().databaseFactory.documentFromUri( 
                    handle.uri ) as DerivedDocument;

            }
            else {
                databaseDocument = handle.databaseDocument as DerivedDocument;
            }

            if( databaseDocument != null ) {

                handle = this.getNewHandle( path, databaseDocument.uri(), databaseDocument.title.value(), databaseDocument );

                handles.set( path, handle );

                //delete this._encryptedTitles;
            }

            //log.traceOut( "document()", document );
            return databaseDocument as DerivedDocument;

        } catch( error ) {
            log.warn( "document()", "Error reading database document", error );

            throw new Error( (error as any).message );
        }
    }

    setDocument( referenceHandle : ReferenceHandle<DerivedDocument> ): void {
        log.traceIn( "setDocument()", referenceHandle.referenceHandleTitle );

        try {

            this.cleared = false;

            const oldHandles = this.handles( true )!;

            const oldHandle = oldHandles != null ? Array.from(oldHandles.values()).pop() : undefined;

            if( oldHandle != null && oldHandle.path === referenceHandle.path ) {
    
                if( referenceHandle.referenceHandleTitle != null && referenceHandle.referenceHandleTitle.length > 0 ) {
    
                    const handle = oldHandles.get( referenceHandle.path );
    
                    //log.debug( "setDocument()", {handle} );
    
                    if( handle != null ) {
                        handle.referenceHandleTitle = referenceHandle.referenceHandleTitle; 
                    }
                }
    
                this.clearChanges();

                super.notify( Observations.Update as Observation, referenceHandle.path, referenceHandle );

                log.traceOut( "setDocument()", "same path" );
                return;
            }

            const changed = oldHandle?.path !== referenceHandle.path;
                
            const newHandles = new Map<string,ReferenceHandle<DerivedDocument>>();

            const pathElements = referenceHandle.path.startsWith("/") ? 
                referenceHandle.path.substring(1).split("/") : referenceHandle.path.split("/");  // remove leading "/" and split the rest

            let ownerPath = "";

            let newHandle;

            for( let i = 0; i < pathElements.length; i++ ) {

                ownerPath += "/" + pathElements[i];

                if( i > 0 && this.collectionName()=== pathElements[i - 1] ) {

                    let title;

                    let databaseDocument;

                    if( databaseServiceFactory!.get().databaseFactory.equalUris( ownerPath, referenceHandle.path ) ) {
                        title = referenceHandle.referenceHandleTitle;
                    }

                    const ownerHandle = oldHandles?.get( ownerPath );

                    if( title == null ) {

                        title = ownerHandle?.referenceHandleTitle;
                    }

                    databaseDocument = ownerHandle?.databaseDocument != null ? ownerHandle?.databaseDocument : null;

                    const ownerUri = databaseDocument != null ? databaseDocument.path() : ownerPath;

                    newHandle = this.getNewHandle( ownerPath, ownerUri, title, databaseDocument );

                    newHandles.set( ownerUri, newHandle );   
                }
            }

            if( changed ) {

                if( this.onChange( oldHandle, newHandle )) {

                    this._handles = newHandles;

                    delete this._selectCollection;
                    delete this._selectCollectionGroup;

                    this.onChanged( oldHandle, newHandle );

                }
            }

            log.traceOut( "setDocument()", newHandles.size );

        } catch( error ) {
            log.warn( "setDocuments()", "Error adding database objects", error );

            throw new Error( (error as any).message );
        }
    }


    clearDocuments(): void {

        //log.traceIn( "clearDocuments()", this._handles );

        try {
            //delete this._encryptedTitles;

            const existingValue = this.value();

            if( this.onChange( existingValue, undefined ) ) {

                this.cleared = true;

                delete this._selectCollection;
                delete this._selectCollectionGroup;

                this.onChanged( existingValue, undefined )
            }

            //log.traceOut( "clearDocuments()", this._handles );

        } catch( error ) {
            log.warn( "clearDocuments()", "Error adding database object", error );

            throw new Error( (error as any).message );
        }
    }
    
    async options(): Promise<Map<string,ReferenceHandle<DerivedDocument>> | undefined> {
 
        log.traceIn( "options()" );

        const options = this.referenceHandles();

        log.traceOut( "options()", options );
        return options != null && options.size > 0 ? options : undefined;
    }

    async select( params? : { 
        collectionGroup? : boolean,
        fetch: boolean } ): Promise<Map<string,ReferenceHandle<DerivedDocument>> | undefined> {
 
        log.traceIn( "select()", {params} );

        let result = new Map<string,ReferenceHandle<DerivedDocument>>();
        try {

            const ignoreCleared = true;

            let thisReferenceHandle = this.referenceHandle( ignoreCleared );

            if( thisReferenceHandle != null ) {

                if( thisReferenceHandle.referenceHandleTitle == null ) {

                    await thisReferenceHandle.fetch();
                }

                result.set( thisReferenceHandle!.path, thisReferenceHandle! );
            }

            const database = this.selectDatabase( params?.collectionGroup );

            if( database == null ) {
                log.traceOut( "select()", "No select database" );
                return result;
            }

            const referenceHandles = await database.referenceHandles();

            for( const referenceHandle of referenceHandles.values() ) {

                if( !databaseServiceFactory!.get().databaseFactory.equalUris( 
                        thisReferenceHandle?.path, referenceHandle.path )) {

                    await referenceHandle.fetch();

                    result.set( referenceHandle.path, referenceHandle );
                }
            }

            log.traceOut( "select()", "from root" );
            return result;  

        } catch( error ) {

            log.warn( "select()", "Error reading database objects", error );

            throw new Error( (error as any).message );
        }    
    }

    selectDatabase( collectionGroup? : boolean ) : Database<DerivedDocument> | undefined {

        log.traceIn("selectDatabase()", {collectionGroup} );

        try {
            if( !!collectionGroup ) {
                if( this._selectCollectionGroup == null ) {
                    this._selectCollectionGroup = this.parent.parentCollectionGroup( this._collectionName ) as CollectionGroupDatabase<DerivedDocument>;
                }
                return this._selectCollectionGroup;
            }
            else {
                if( this._selectCollection == null ) {
                    this._selectCollection = this.parent.parentCollection( this._collectionName ) as CollectionDatabase<DerivedDocument>;
                }
                return this._selectCollection;
            }
            //log.traceOut("selectDatabase()", "found" );

        } catch (error) {
            log.warn("selectDatabase()", "Error selecting database", error);

            throw new Error( (error as any).message );
        }
    }


    async toRecord( documentData: Record<string, any>, force? : boolean ) : Promise<void> {

        //log.traceIn( "toRecord()" );

        try {

            if( !!force ) {
                this.referenceHandles();
            }

            
            if( this.encrypted() && this.encryptedData() != null ) {
            
                documentData[this.key()] = this.encryptedData();
                return;
            }

            const key = this.key();

            const ids = this.ids();

            if( ids != null && key != null ) {
                documentData[key + IdsSuffix] = ids;
            }
    
            const id = this.id();

            if( id != null && key != null ) {
                documentData[key + IdSuffix] = id;
            }

            //log.traceOut( "toRecord()", documentData );

        } catch( error ) {
            log.warn( "toRecord()", "Error converting to record", error );

            throw new Error( (error as any).message );
        }

    }

    fromRecord( documentData: Record<string, any>): void {

        //log.traceIn( "fromRecord()" );

        // We don't read owner ids from data as they are read from path. Only written to DB for group query purposes

        //log.traceOut( "fromRecord()", this );
    } 

    referenceHandles( ignoreCleared? : boolean ) : Map<string,ReferenceHandle<DerivedDocument>> | undefined {

        try {
            //log.traceIn( "referenceHandles()" );

            const handles = this.handles( ignoreCleared );

            if( handles == null ) {
                //log.traceOut( "titles()", "No handles" );
                return undefined;
            }
            const result = new Map<string,ReferenceHandle<DerivedDocument>>();

            handles.forEach( handle => {

                result.set( handle.path, 
                    new ReferenceHandle<DerivedDocument>( {
                        path: handle.path,
                        uri: handle.uri,
                        title: handle.referenceHandleTitle,
                        date: handle.date,
                        databaseDocument: handle.databaseDocument,
                        documentReference: handle.databaseDocument?.documentReference()
                    })
                )
            })

            //log.traceOut( "referenceHandles()", result );
            return result;   

        } catch( error ) {
            
            log.warn( "referenceHandles()", "Error reading reference handles", error );

            throw new Error( (error as any).message );
        }
    }

    compareTo( other : OwnerProperty<DerivedDocument> ) : number {

        return this.compareValue( other.value() );

    }

    compareValue( otherValue : ReferenceHandle<DerivedDocument> | undefined ) : number {

        const value = this.value();

        if( value == null ) {
            return otherValue == null ? 0 : -1;
        }

        return value.compareTo( otherValue );
    }

    includes( other : OwnerProperty<DerivedDocument>  ) : boolean {
        return this.includesValue( other.value() );
    }

    includesValue( otherValue : ReferenceHandle<DerivedDocument> | undefined ) : boolean {  

        const path = this.path();

        if( path == null && otherValue == null ) {
            return false;
        }

        if( path == null && otherValue != null ) {
            return false;
        }

        if( path != null && otherValue == null ) {
            return false;
        }

        const otherPath = otherValue!.path;

        if( path == null && otherPath == null ) {
            return false;
        }

        if( path == null && otherPath != null ) {
            return false;
        }

        if( path != null && otherPath == null ) {
            return false;
        }

        return path!.split("?")[0].startsWith( otherPath!.split("?")[0] );    

    }

    copyValueFrom( other : OwnerProperty<DerivedDocument> ) : boolean {

        //log.traceIn( "copyValueFrom()", this._handles, other._handles );

        if( this.compareTo( other ) === 0 ) {
            return false;
        }

        this.cleared = other.cleared;    

        delete this._selectCollection;
        delete this._selectCollectionGroup;

        const ignoreCleared = true;

        const existingHandles = this.handles( ignoreCleared );

        const otherHandles = (other as OwnerPropertyImpl<DerivedDocument>).handles( ignoreCleared );

        if( existingHandles != null && 
            otherHandles != null &&
            existingHandles.size === otherHandles.size ) {

            const existingHandlesArray = Array.from( existingHandles.values() );
            const otherHandlesArray = Array.from( otherHandles.values() );

            for( let i = 0; i < existingHandlesArray.length; i++ ) {

                if( existingHandlesArray[i].referenceHandleTitle == null ) {  

                    existingHandlesArray[i].referenceHandleTitle = otherHandlesArray[i].referenceHandleTitle;

                    existingHandlesArray[i].databaseDocument = otherHandlesArray[i].databaseDocument;
                }
            }
        }
        
        //log.traceOut( "copyValueFrom()" );
        return true;
    } 

    protected handles( ignoreCleared? : boolean ) : Map<string,ReferenceHandle<DerivedDocument>> | undefined {

        //log.traceIn( "handles()" );

        try {
            if( this.cleared && !ignoreCleared ) {
                //log.traceOut( "handles()", "cleared" );
                return undefined;
            }

            if( this._handles != null ) {
                //log.traceOut( "handles()", "existing", this._handles );
                return this._handles;
            }

            this._handles = new Map<string,ReferenceHandle<DerivedDocument>>();

            const paths = this.parent.ownerPaths( this._collectionName );

            //log.trace( "handles()", "paths", paths );

            if( paths == null || paths.length === 0 ) {
                //log.traceOut( "handles()", "no owner paths");
                return undefined;
            }

            for( const path of paths ) {

                this._handles.set( path, this.getNewHandle( path, path, undefined, null ) );
            }

            //log.traceOut( "handles()", "built handles", this._handles );
            return this._handles;

        } catch( error ) {
            log.warn( "handles()", "Error getting handles", error );

            throw new Error( (error as any).message );
        }
    }

    protected getNewHandle( path : string, uri : string, title : string | undefined, databaseDocument : DerivedDocument | null ) : ReferenceHandle<DerivedDocument> { 

        const databaseHandle = new ReferenceHandle<DerivedDocument>({

            path : path,

            uri : uri,

            databaseDocument : databaseDocument != null ? databaseDocument : undefined,

            documentReference: databaseDocument?.documentReference()

        });

        if( title != null  ) {
            databaseHandle.referenceHandleTitle = title;
        }

        return databaseHandle;
    } 
    cleared : boolean = false;

    private readonly _collectionName : string;

    private readonly _documentName : string;

    private _selectCollection? : CollectionDatabase<DerivedDocument>;

    private _selectCollectionGroup? : CollectionGroupDatabase<DerivedDocument>;
    
    private _handles : Map<string,ReferenceHandle<DerivedDocument>> | undefined;

    //private _encryptedTitles? : string;


}
