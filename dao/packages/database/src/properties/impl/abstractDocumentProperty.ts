import { DatabaseRecord } from "../../core/types/databaseRecord";
import { GenericDatabaseDocument } from "../../core/impl/genericDatabaseDocument";
import { AbstractDatabaseProperty } from "../../core/base/abstractDatabaseProperty";
import { Database } from "../../core/spec/database";
import { DatabaseDocument } from "../../core/spec/databaseDocument";
import { DatabaseObject } from "../../core/spec/databaseObject";
import { ReferenceHandle } from "../../core/impl/referenceHandle";
import { DocumentProperty } from "../spec/documentProperty";
import { PropertyType } from "../../core/defs/propertyType";
import { DatabaseAccess } from "../../core/impl/databaseAccess";
import { databaseServiceFactory } from "../../core/impl/databaseServiceFactory";
import { log } from "../../core/base/abstractDatabaseService";

export abstract class AbstractDocumentProperty<DerivedDocument extends DatabaseDocument> 
    extends AbstractDatabaseProperty<ReferenceHandle<DerivedDocument>> implements DocumentProperty<DerivedDocument> {

    constructor( parent : DatabaseObject, 
        type : PropertyType, 
        onSelectDatabases? : () => (Database<DerivedDocument> | undefined)[],
        reciprocalKey? : keyof DerivedDocument ) {
    
        super( parent, type ); 

        try { 

            if( reciprocalKey != null && parent.parent != null ) {
                throw new Error( "Reciprocal keys can only be used for documents" );
            }

            this._onSelectDatabases = onSelectDatabases;

            this.reciprocalKey = reciprocalKey as string;

           //log.traceOut( "constructor()" );
        } catch( error ) {
            
            log.warn( "constructor()", "Error initializing document reference", error );

            throw new Error( (error as any).message );
        }
    }

    value() {
        return this.referenceHandle();
    }

    setValue( value : ReferenceHandle<DerivedDocument> | undefined ) : void {

        if( this.compareValue( value ) !== 0 ) {

            if( value == null ) {
                this.clearDocument();
            }
            else {
                this.setDocument( value );
            }
        }
    }

    id() : string | undefined {

        const referenceHandle = this.referenceHandle();

        if( referenceHandle == null ) {
            return undefined
        }

        const documentId = 
            databaseServiceFactory!.get().databaseFactory.documentId( referenceHandle.uri );

        return documentId;
    }

    path() : string | undefined {

        return this.referenceHandle()?.path;
    }

    title() : string | undefined {

        return this.referenceHandle()?.referenceHandleTitle;
    }

    emptyDocument(): DerivedDocument | undefined {

        //log.traceIn( "emptyDocument()" );

        try {
            const referenceHandle = this.referenceHandle();

            if( referenceHandle == null ) {
                //log.traceOut( "emptyDocument()", "no path", undefined);
                return undefined;
            }

            const result = 
                databaseServiceFactory!.get().databaseFactory.newDocumentFromUri( referenceHandle.uri ) as DerivedDocument;

            if( result == null ) {

                this.clearDocument();

                //log.traceOut( "emptyDocument()", "not found", undefined);
                return undefined;
            }

            //result.title.setValue( this.title() );

            //log.traceOut( "emptyDocument()", result );
            return result; 

        } catch( error ) {

            log.warn( "newDocument()", "Error reading database objects", error );

            throw new Error( (error as any).message );
        }    
    }


    async document(): Promise<DerivedDocument | undefined> { 

        log.traceIn( "document()" );

        try {
            const referenceHandle = this.referenceHandle();

            if( referenceHandle == null ) {
                log.traceOut( "document()", "no path", undefined);
                return undefined;
            }

            let result;

            if( referenceHandle.databaseDocument != null ) {

                result = referenceHandle.databaseDocument;
            }
            else {
                
                const databaseDocument = 
                    await databaseServiceFactory!.get().databaseFactory.documentFromUri( referenceHandle.uri ) as DerivedDocument;

                if( databaseDocument == null ) {

                    this.clearDocument();

                    throw new Error( "Reference document not found: " + referenceHandle.uri );
                }

                this.updateHandle( 
                    databaseDocument.path(), 
                    databaseDocument.uri(), 
                    databaseDocument.title.value() != null ? databaseDocument.title.value() : referenceHandle.referenceHandleTitle, 
                    databaseDocument.referenceDateProperty()?.value() != null ? databaseDocument.referenceDateProperty()?.value() : referenceHandle.date, 
                    databaseDocument );

                result = databaseDocument;
            }


            log.traceOut( "document()", referenceHandle.uri );
            return result; 

        } catch( error ) {

            log.warn( "document()", "Error reading database objects", error );

            throw new Error( (error as any).message );
        }    
    }

    setDocument( referenceHandle : ReferenceHandle<DerivedDocument> ): void {

        //log.traceIn( "setDocument()", referenceHandle.title );

        try {

            const existingReferenceHandle = this.referenceHandle()?.copy();

            if( this.onChange( existingReferenceHandle, referenceHandle ) ) {

                this.updateHandle( 
                    referenceHandle.path, 
                    referenceHandle.uri, 
                    referenceHandle.referenceHandleTitle, 
                    referenceHandle.date,
                    referenceHandle.databaseDocument ); 
    
                this.onChanged( existingReferenceHandle, this._referenceHandle );
            }
                            
           //log.traceOut( "setDocument()" );

        } catch( error ) {
            log.warn( "setDocument()", "Error updating document", error );

            throw new Error( (error as any).message );
        }
    }

    
    clearDocument(): void {

        log.traceIn("clearDocument()" );

        try {
            const referenceHandle = this.referenceHandle();

            if( this.onChange( referenceHandle, undefined ) ) {

                delete this._referenceHandle;

                this.onChanged( referenceHandle, undefined ); 
            }

            log.traceOut("clearDocument()" );

        } catch( error ) {
            log.warn( "clearDocument()", "Error adding database object", error );

            throw new Error( (error as any).message );
        }
    }

    newDocument(): DerivedDocument | undefined {

        try {

            const sources = this.databases();

            if( sources == null || sources.length === 0 ) {
                return undefined;
            }

            for( const source of sources ) {

                if( source == null ) {
                    continue;
                }

                if( typeof source === "string" ) {

                }
                else {
                    return source.newDocument();
                }
            }

            return undefined;

        } catch( error ) {

            log.warn("path()", "Error creating new document on reference ", error );

            throw new Error( (error as any).message );
        }  
    }


    async select( params? : { 
        filterValue? : boolean,
        fetch: boolean } ): Promise<Map<string,ReferenceHandle<DerivedDocument>> | undefined> {
 
        log.traceIn( "select()", {params} );

        try {
            const databases = this.databases();

            if( databases == null || databases.length === 0 ) {
                log.traceOut( "select()", "empty databases" );
                return undefined;
            }

            const result = new Map<string,ReferenceHandle<DerivedDocument>>();

            const value = this.value();

            for( const source of databases ) {

                if( source == null ) {
                    continue;
                }

                const referenceHandles = await source.referenceHandles();

                for( const referenceHandle of referenceHandles.values() ) {

                    if( !!params?.filterValue && 
                        value?.path != null && 
                        referenceHandle.path != null &&
                        referenceHandle.uri != null &&
                        value.compareTo( referenceHandle ) === 0 ) {
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

            log.warn("path()", "Error getting user access on references ", error );

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


    async toRecord( documentRecord: DatabaseRecord, force? : boolean ) : Promise<void> {
        
        //log.traceIn( "toRecord()", this.key(), this._handle );

        try {
            if( this._referenceHandle?.documentReference == null ) {
                //log.traceOut( "toRecord()", undefined );
                return;
            }

            documentRecord[this.key()] = this._referenceHandle?.documentReference;

            //log.traceOut( "toRecord()", result );

        } catch( error ) {
            log.warn( "toRecord()", "Error converting reference to json ", error );

            throw new Error( (error as any).message );
        }
    }

    fromRecord( documentRecord: DatabaseRecord): void {

        //log.traceIn( "fromRecord()" );

        try {
            delete this._referenceHandle;

            const documentReference = documentRecord[this.key()] as {
                path?: string;
                uri?: string;
            };

            if( documentReference == null ) {
                //log.traceIn( "fromRecord()", "empty" );
                return;
            }

            if( documentReference.path == null || documentReference.uri == null ) {
                throw new Error( "Invalid document reference" );
            }

            this._referenceHandle = new ReferenceHandle<DerivedDocument>({
                    path: documentReference.path,
                    uri: documentReference.uri,
                    documentReference: documentReference 
                });

            //log.traceOut( "fromRecord()", this._handle );
             
        } catch( error ) {
            log.warn( "fromRecord()", "Error reading reference: " + this.parentDocument().path(), error );

            throw new Error( (error as any).message );
        }
    }

    referenceHandle() : ReferenceHandle<DerivedDocument> | undefined {
        //log.traceIn( "referenceHandle()" );

        try {

            /*
            if( this._referenceHandle == null && this._encryptedReferenceData == null ) {
                return undefined;
            }
            
            if (this._encryptedReferenceData != null) {

                delete this._referenceHandle;

                for (const entry of Object.entries(this._encryptedReferenceData)) {

                    const documentReference = typeof entry[0] === "string" ? undefined : entry[0] as any; 

                    const encryptedReferenceHandle = entry[1] as string;

                    const jsonReferenceHandle = Factory.get().securityService.symmetricCipher.decrypt(encryptedReferenceHandle);

                    if( jsonReferenceHandle == null ) {
                        return undefined;
                    }
                    const referenceHandle = new ReferenceHandle<DerivedDocument>( JSON.parse(jsonReferenceHandle) );

                    if (referenceHandle != null) {

                        this._referenceHandle = referenceHandle;

                        this._referenceHandle.documentReference = documentReference;
                    }
                    break;
                }
                delete this._encryptedReferenceData;
            }

            */ 

            if (this._referenceHandle?.path == null) {
                //log.traceOut( "referenceHandle()", "no handle or path" );
                return undefined;
            }

            const referenceHandle = this._referenceHandle.copy();

            //log.traceOut( "referenceHandle()", {referenceHandle} );
            return referenceHandle;

        } catch (error) {

            log.warn("referenceHandle()", "Error reading reference ", error);

            return undefined;
        }
    }


    compareTo( other : DocumentProperty<DerivedDocument> ) : number {
        return this.compareValue( other.value() );
    }

    compareValue( otherValue : ReferenceHandle<DerivedDocument> | undefined ) : number {

        const value = this.value();

        if( value == null ) {
            return otherValue == null ? 0 : -1;
        }

        return value.compareTo( otherValue );
    }
 

    includes( other : DocumentProperty<DerivedDocument>  ) : boolean {
        return this.includesValue( other.value() );
    }

    includesValue( value : ReferenceHandle<DerivedDocument> | undefined ) : boolean {
        return this.compareValue( value ) === 0;
    }

    protected updateHandle( 
        path : string, 
        uri: string,
        title? : string, 
        date? : Date,
        databaseDocument? : DerivedDocument ) : void {

        if( path == null ) {

            delete this._referenceHandle;
        }
        else if( path !== this._referenceHandle?.path ) {

            this._referenceHandle = new ReferenceHandle<DerivedDocument>({
                    path: path,
                    uri: uri,
                    title: title,
                    date: date
                });

            if( databaseDocument != null ) {

                this._referenceHandle.databaseDocument = databaseDocument;

                this._referenceHandle.documentReference = databaseDocument.documentReference();

                this._referenceHandle.referenceHandleTitle = databaseDocument.title.value();
            }
        }
        else {

            this._referenceHandle.databaseDocument = databaseDocument; 
            
            if( this._referenceHandle.databaseDocument != null ) {

                this._referenceHandle.documentReference = this._referenceHandle.databaseDocument.documentReference();

                this._referenceHandle.referenceHandleTitle = this._referenceHandle.databaseDocument.title.value();
            }    
        }    
    }

    databases() : (Database<DerivedDocument> | undefined)[] | undefined {

        if( this._databases !== undefined ) {
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
            return undefined;
        }
            
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

    readonly reciprocalKey? : string;

    protected readonly _onSelectDatabases? : () => (Database<DerivedDocument> | undefined)[];

    protected _databases? : (Database<DerivedDocument> | undefined)[] | null;

    protected _referenceHandle? : ReferenceHandle<DerivedDocument>;

    //private _encryptedReferenceData? : any; 

}
