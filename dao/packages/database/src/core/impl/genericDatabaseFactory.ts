import { DatabaseRecord } from "../types/databaseRecord";
import { ConfigurationManager, configurationServiceFactory } from "@dao/configuration";
import { DatabaseManager } from "../spec/databaseManager";
import { CollectionGroupDatabase } from "../spec/collectionGroupDatabase";
import { DatabaseDocument, DatabaseDocumentNameKey } from "../spec/databaseDocument";
import { CollectionDatabase } from "../spec/collectionDatabase";
import { CollectionGroupDatabaseImpl } from "./collectionGroupDatabaseImpl";
import { CollectionDatabaseImpl } from "./collectionDatabaseImpl";
import { log } from "../base/abstractDatabaseService";
import { Template } from "../../documents/spec/template";
import { TemplatedDocument } from "../spec/templatedDocument";
import { CollectionsConfigurationName, DatabaseFactory } from "../spec/databaseFactory";
import { CollectionGroupPathSuffix, TemplatePathKey } from "../spec/databaseService";
import { Database } from "../spec/database";
import { PropertyTypes } from "../defs/propertyType";
import { CollectionProperty } from "../../properties/spec/collectionProperty";
import { GenericDatabaseDocument } from "./genericDatabaseDocument";
import { AbstractTemplatedDocument } from "../base/abstractTemplatedDocument";
import { DatabaseObserver } from "../spec/databaseObserver";
import { DatabaseQuery } from "../types/databaseQuery";
import { DatabaseObserverImpl } from "./databaseObserverImpl";
import { ReferenceHandle } from "./referenceHandle";
import { TemplatedProperties } from "../spec/templatedProperties";
import { SubdocumentPropertyImpl } from "../../properties/impl/subdocumentPropertyImpl";
import { TemplatedPropertiesImpl } from "./templatedPropertiesImpl";
import { PropertyDescriptorImpl } from "./propertyDescriptorImpl";
import { DatabaseProperty } from "../spec/databaseProperty";

export class GenericDatabaseFactory implements DatabaseFactory {

    static registerDocument( documentName : string, onNewDocument : ( 
        documentName : string,
        collectionDatabase : CollectionDatabase<DatabaseDocument>,
        documentUri? : string ) => DatabaseDocument ) : void {

        //log.traceInOut( "registerDocument()", documentName );

        GenericDatabaseFactory._onNewDocuments.set( documentName, onNewDocument );
    }

    constructor( configurationManager : ConfigurationManager, databaseManager : DatabaseManager ) {
        
        //log.traceIn( "constructor()");

        try {
            // Configuration data

            this.configurationManager = configurationManager;

            this.databaseManager = databaseManager;

            const collectionsConfig = configurationServiceFactory!.get().config( CollectionsConfigurationName ) as any;

            for( const collectionConfigEntries of Object.entries( collectionsConfig ) ) {

                const collectionName = collectionConfigEntries[0];

                const collectionConfig = collectionConfigEntries[1] as any;

                for( const documentName of collectionConfig.documentNames ) {

                    this._documentNamesToCollectionNames.set( documentName, collectionName );
                }

                if( !!collectionConfig.rootCollection ) {
                    this._rootCollectionNames.push( collectionName );
                }

            }

             //log.traceOut( "constructor()");

        } catch( error ) {
            log.warn( "constructor()", "Error initializing database service", error );

            throw new Error( (error as any).message );
        }
    }


    documentId( uri : string ) : string | undefined {

        //log.traceIn( "documentId()", documentPath );

        const pathElements = uri.split("?")[0].split("/");

        if( pathElements.length < 2 ) {
            //log.traceOut( "documentId()", "not a document path" );
            return undefined;
        }

        let documentId = pathElements[pathElements.length-1];

        //log.traceOut( "documentId()", documentId );
        return documentId;
    }


    documentReference( uri : string ) : any | undefined {

        return this.databaseManager.documentReference( uri );

    }

    documentUri( documentReference : any ) : string | undefined {
        return this.databaseManager.documentUri( documentReference );
    }


    encodeUriQuery( queryParams : Map<string,string> ) : string {

        let query = "";

        try {
            if( queryParams.size == 0 ) {
                return "";
            }

            query += "?";

            for( const queryParamElement of queryParams ) {

                if( !query.endsWith("?") ) {
                    query += "&";
                }

                query += encodeURIComponent( queryParamElement[0] ) + "=" + encodeURIComponent( queryParamElement[1] );
            }

            return query;

        } catch( error ) {

            log.warn( "uriParamsFromUri()", "Invalid uri params", error );

            return query;
        }
    }

    decodeUriQuery( uri : string ) : Map<string,string> {

        const queryParams = new Map<string,string>();

        try {

            const uriElements = uri.split("?");

            if( uriElements.length < 2 ) {
                return queryParams;
            }

            const querylements = uriElements[1].split("&");

            for( const querylement of querylements ) {

                const querylementEntry = querylement.split("=");

                if( querylementEntry.length != 2 ) {

                    throw new Error( "Invalid query param: " + querylementEntry );
                }

                queryParams.set( decodeURIComponent( querylementEntry[0] ), decodeURIComponent( querylementEntry[1] ) );
            }

            return queryParams;

        } catch( error ) {

            log.warn( "uriParamsFromUri()", "Invalid uri params", error );

            return queryParams;
        }
    }

    isUriDatabase( uri : string  ) : boolean {

        //log.traceIn( "isUriDatabase()");

        try {
            const uriElements = this.pathElementsFromUri( uri );

            const result = uriElements != null && uriElements.length > 0 && uriElements.length % 2 === 1;

            //log.traceOut( "isUriDatabase()", {result});
            return result;

        } catch( error ) {
            log.warn( "isUriDatabase()", "Error checking URL is connection", error );

            return false;
        }
    }

    isUriDocument( uri : string  ) : boolean {
        //log.traceIn( "isUriDocument()");

        try {
            const uriElements = this.pathElementsFromUri( uri );

            const result = uriElements != null && uriElements.length > 0 && uriElements.length % 2 === 0;

            //log.traceOut( "isUriDocument()", {result});,
            return result;

        } catch( error ) {
            log.warn( "isUriDocument()", "Error checking URL is document", error );

            return false;
        }
    }

    uriToPath( uri? : string ) : string | undefined {

        return uri?.split("?")[0];
    }

    equalUris( uri1? : string, uri2? : string ) : boolean {
        return this.uriToPath( uri1 ) === this.uriToPath( uri2 );
    }

    documentNameFromUri( uri : string  ) : string | undefined {

        //log.traceIn( "documentNameFromUri()", {url} );

        try {
            if( !uri.includes("?") || uri.endsWith("?") ) {
                //log.traceOut( "documentNameFromUri()", "URL has no parameters", {url} );
                return undefined;
            }

            let uriParameters = uri.split("?")[1];

            if( !uriParameters.includes( DatabaseDocumentNameKey + "=" ) ) {
                //log.traceOut( "documentNameFromUri()", "Document name parameter not found in URL", url );
                return undefined;
            }

            let documentName = uriParameters.split( DatabaseDocumentNameKey + "=" )[1];

            documentName = documentName.split( "?" )[0];

            documentName = documentName.split( "&" )[0];

            if( documentName != null && documentName.length > 0 ) {
                //log.traceOut( "documentNameFromUri()", {documentName} );
                return documentName;
            }
            
            //log.traceOut( "documentNameFromUri()", "empty document name in URL", url );
            return undefined;

        } catch( error ) {
            log.warn( "documentNameFromUri()", "Error reading document name from path", error );

            throw new Error( (error as any).message );
        }
    }

    collectionNameFromUri( uri : string  ) : string | undefined {

        //log.traceIn( "collectionNameFromUri()", url );

        try {

            const elements = this.pathElementsFromUri( uri.split(CollectionGroupPathSuffix)[0] ) 

            for( let i = elements.length-1; i >=0; i-- ) {

                if( this.collectionNames().indexOf( elements[i] ) !== -1 ) {

                    const result = elements[i];

                    //log.traceOut( "collectionNameFromUri()", result );
                    return result;
                }
            }

            //log.traceOut( "collectionNameFromUri()", "not found" );
            return undefined;

        } catch( error ) {
            log.warn( "collectionNameFromUri()", "Error reading collection group from path", error );

            throw new Error( (error as any).message );
        }
    }

    collectionPathFromUri( uri : string, collectionName? : string ) : string | undefined {

        //log.traceIn( "collectionPathFromUri()", url );

        try {
            let result;

            let path = "";

            const elements = this.pathElementsFromUri( uri.split(CollectionGroupPathSuffix)[0] ) 

            if( this.isUriDatabase( uri ) ) {

                for( let i = 0; i < elements.length; i++ ) {

                    path += "/" + elements[i];

                    if( collectionName != null && collectionName === elements[i] ) {
                        result = path;
                    }
                }
            }
            else if( this.isUriDocument( uri ) ) {

                for( let i = 0; i < elements.length - 1; i++ ) {

                    path += "/" + elements[i];

                    if( collectionName != null && collectionName === elements[i] ) {
                        result = path;
                    }
                }
            }
            else {
                throw new Error( "Invalid URL: " + uri);
            }

            if( collectionName == null ) {
                result = path;
            }

            //log.traceOut( "collectionPathFromUri()", result ); 
            return result;

        } catch( error ) {
            log.warn( "collectionPathFromUri()", "Error reading collection path from path", error );

            throw new Error( (error as any).message );
        }
    }

    documentPathFromUri( uri : string, collectionName? : string ) : string | undefined { 

        //log.traceIn( "documentPathFromUri()", url );

        try {
            let result;

            let path = "";

            const elements = this.pathElementsFromUri( uri.split(CollectionGroupPathSuffix)[0] ) 

            if( this.isUriDatabase( uri ) ) {

                for( let i = 0; i < elements.length - 1; i++ ) {
                    
                    path += "/" + elements[i];

                    if( collectionName != null && collectionName === elements[i] ) {
                        result = path + "/" + elements[i+1];
                    }
                }
            }
            else if( this.isUriDocument( uri ) ) {

                for( let i = 0; i < elements.length; i++ ) {

                    path += "/" + elements[i];

                    if( collectionName != null && collectionName === elements[i-1] ) {
                        result = path;
                    }
                }
            }
            else {
                throw new Error( "Invalid URL: " + uri);
            }

            if( collectionName == null ) {
                result = path;
            }

            //log.traceOut( "documentPathFromUri()", result ); 
            return result;

        } catch( error ) {
            log.warn( "documentPathFromUri()", "Error reading document path from path", error );

            throw new Error( (error as any).message );
        }
    }

    databaseFromUri( uri : string ) : Database<DatabaseDocument> | undefined {

        log.traceIn( "databaseFromUri()", {url: uri} );

        const strippedUrl = uri.split("?")[0];

        if( strippedUrl.endsWith( CollectionGroupPathSuffix ) ) {
            return this.collectionGroupFromUri( uri );
        }
        else {
            return this.collectionFromUri( uri );
        } 
    }

    collectionGroupFromUri( url : string ) : CollectionGroupDatabase<DatabaseDocument>  | undefined {

        //log.traceIn( "collectionGroupFromUri()", {url} );

        try {
            const strippedUrl = url.split("?")[0];

            const documentName = this.documentNameFromUri( url );
        
            if( !strippedUrl.endsWith( CollectionGroupPathSuffix ) ) {
                throw new Error( "Collection group path must end with " + CollectionGroupPathSuffix );
            }

            const elements = this.pathElementsFromUri( strippedUrl.split(CollectionGroupPathSuffix)[0] )  

            let ownerPath = "";

            let collectionName;

            if( elements.length % 2 === 0 && elements.length > 1 ) {

                for( let i = 0; i < elements.length - 2; i++) {

                    ownerPath += "/" + elements[i];
                }

                collectionName = elements[elements.length-2];
            }
            else if( elements.length % 2 === 1 && elements.length > 0 ) {

                for( let i = 0; i < elements.length - 1; i++) {

                    ownerPath += "/" + elements[i];
                }
                collectionName = elements[elements.length-1];
            }
            else {
                log.warn( "collectionGroupFromUri()", "The path has no collections" );
                return undefined;
            }

            const owner = this.newDocumentFromUri( ownerPath ); 

            let collectionGroupDatabase;

            if( documentName != null ) {
                collectionGroupDatabase = this.collectionGroupDatabaseFromDocumentName( 
                    documentName, owner ) as CollectionGroupDatabase<DatabaseDocument> ;
            }
            else {
                collectionGroupDatabase = this.collectionGroupDatabaseFromCollectionName( 
                    collectionName, owner ) as CollectionGroupDatabase<DatabaseDocument> ;
            }

            //log.traceOut( "collectionGroupFromUri()", collectionGroupDatabase!.databasePath() );
            return collectionGroupDatabase!;

        } catch( error ) {
            log.warn( "collectionGroupFromUri()", "Error reading collection group from path", error );

            return undefined;
        }
    }

    collectionFromUri( url : string ) : CollectionDatabase<DatabaseDocument>  | undefined {

        //log.traceIn( "collectionFromUri()", {url} );

        try {
            const strippedUrl = url.split("?")[0];

            const documentName = this.documentNameFromUri( url );

            //log.debug( "collectionFromUri()", {documentName} );

            const elements = this.pathElementsFromUri( strippedUrl )  

            let ownerPath = "";

            let collectionName;

            if( elements.length % 2 === 0 && elements.length > 1 ) {

                for( let i = 0; i < elements.length - 2; i++) {

                    ownerPath += "/" + elements[i];
                }

                collectionName = elements[elements.length-2];
            }
            else if( elements.length % 2 === 1 && elements.length > 0 ) {

                for( let i = 0; i < elements.length - 1; i++) {

                    ownerPath += "/" + elements[i];
                }
                collectionName = elements[elements.length-1];
            }
            else {
                //log.traceOut( "collectionFromUri()", "The path has no collections" );
                return undefined;   
            }      

            const owner = this.newDocumentFromUri( ownerPath ); 

            let collectionDatabase;

            if( documentName != null ) {
                collectionDatabase = this.collectionDatabaseFromDocumentName( 
                    documentName, owner ) as CollectionDatabase<DatabaseDocument> ;
            }
            else {
                collectionDatabase = this.collectionDatabaseFromCollectionName( 
                    collectionName, owner ) as CollectionDatabase<DatabaseDocument> ;
            }

            //log.traceOut( "collectionFromUri()", collectionDatabase!.databasePath( true ) );
            return collectionDatabase!;

        } catch( error ) {
            log.warn( "collectionFromUri()", "Error reading collection name from path", error );

            throw new Error( (error as any).message );
        }
    }

    collectionInUri( uri : string, collectionName : string ) : CollectionDatabase<DatabaseDocument> | undefined {

        //log.traceIn( "collectionInUri()", uri, collectionName );

        try {
            let collection;

            let collectionParent = this.newDocumentFromUri( uri );;

            while( collectionParent != null ) {

                const collectionProperty = collectionParent.property( collectionName );

                if( collectionProperty != null &&
                    collectionProperty.type === PropertyTypes.Collection ) {

                    collection = 
                        (collectionProperty as CollectionProperty<DatabaseDocument>).collection();

                    //log.traceOut( "collectionInUri()", "found", collection );
                    return collection;
                }

                collectionParent = collectionParent.collectionDatabase.owner();
            }

            //log.traceOut( "collectionInUri()", "no collection found" );
            return undefined;

        } catch( error ) {
            log.warn( "collectionInUri()", "Error reading document from path", error );

            throw new Error( (error as any).message );
        }
    }


    newDocumentFromUri( url : string ) : DatabaseDocument | undefined {

        //log.traceIn( "newDocumentFromUri()", url );

        try {
            const elements = this.pathElementsFromUri( url )  // remove leading "/" and split the rest

            //log.debug( "newDocumentFromUri()", "elements" );

            const documentName = this.documentNameFromUri( url );

            //log.debug( "newDocumentFromUri()", "documentName" );

            let databaseDocument : DatabaseDocument | undefined;

            let documentPath : string = "";

            for( let i = 0; i < elements.length;) {

                let collectionDatabase = this.collectionDatabaseFromCollectionName( elements[i], databaseDocument )!;

                //log.debug( "newDocumentFromUri()", "collectionDatabase" );

                documentPath += "/" + elements[i];

                if( i === elements.length - 1 ) {

                    if( documentName != null ) {
                        collectionDatabase = this.collectionDatabaseFromDocumentName( documentName, databaseDocument )!;

                        //log.debug( "newDocumentFromUri()", "collectionDatabase 2" );

                    }

                    databaseDocument = this.newDocument( collectionDatabase )! as DatabaseDocument;

                    //log.debug( "newDocumentFromUri()", "databaseDocument" );

                    break;
                }
                i++;

                documentPath += "/" + elements[i];

                if( i === elements.length - 1 ) {

                    if( documentName != null ) {
                        collectionDatabase = this.collectionDatabaseFromDocumentName( documentName, databaseDocument )!;

                        //log.debug( "newDocumentFromUri()", "collectionDatabase 3" );

                    }
                } 

                databaseDocument = this.newDocument( collectionDatabase, documentPath )! as DatabaseDocument;

                //log.debug( "newDocumentFromUri()", "databaseDocument 2" );

                i++;
            }

            //log.traceOut( "newDocumentFromUri()" ); 
            return databaseDocument!;

        } catch( error ) {
            log.warn( "newDocumentFromUri()", "Error reading document from path", error );

            return undefined;
        }
    }    

    async documentFromUri( url : string ) : Promise<DatabaseDocument | undefined> {

        //log.traceIn( "documentFromUri()", url );

        try {

            let databaseDocument = this.newDocumentFromUri( url );

            if( databaseDocument == null ) {
                log.warn( "documentFromUri()", "not found", {url} );
                return undefined;
            }

            if( databaseDocument.id.value() != null ) {
                await databaseDocument.read(); 
            }

            //log.traceOut( "documentFromUri()", databaseDocument != null ? databaseDocument.referenceHandle().title: undefined );
            return databaseDocument!;

        } catch( error ) {
            log.warn( "documentFromUri()", "Error reading document from path", error );

            throw new Error( (error as any).message );
        }
    }

    async documentFromReference( documentReference : any ): Promise<DatabaseDocument | undefined> {

        //log.traceIn("documentFromReference()", documentReference);

        try {

            if (documentReference == null) {

                log.warn("documentFromReference()", "empty document reference" );
                return undefined;
            }

            const documentUri = await this.databaseManager.documentUri( documentReference );

            if( documentUri == null ) {

                log.warn("documentFromReference()", "empty document URI" );
                return undefined;
            }

            const databaseDocument = await this.documentFromUri( documentUri );

            //log.traceOut("documentFromReference()", "OK");
            return databaseDocument;  

        } catch (error) {
            log.warn("documentFromReference()", "Error reading document reference", error);

            throw new Error( (error as any).message );
        }
    }

    async documentFromRecord( uri: string, record: DatabaseRecord): Promise<DatabaseDocument | undefined> {

        //log.traceIn("documentFromRecord()", data);

        try {

            if (uri == null) {
                log.warn("documentFromRecord()", "empty document path");

                //log.traceOut("documentFromRecord()", undefined);
                return undefined;
            }

            if (record == null) {
                log.warn("documentFromRecord()", "no document data for path: " + uri );

                //log.traceOut("documentFromRecord()", undefined);
                return undefined;
            }

            const pathElements = uri.split("/");

            const documentId = pathElements[pathElements.length-1];

            let url = uri;

            if( record.name != null ) {
                url += "?" + DatabaseDocumentNameKey + "=" + record.name;
            }

            const databaseDocument = this.newDocumentFromUri( url )! as GenericDatabaseDocument;

            if( databaseDocument == null ) {
                log.warn("documentFromRecord()", "no document found at path: " + uri );

                //log.traceOut("documentFromRecord()", "not found", undefined);
                return undefined
            }

            databaseDocument.fromRecord(record);

            if( databaseDocument instanceof AbstractTemplatedDocument ) {

                const templatedDocument = databaseDocument as TemplatedDocument;

                const templateReferenceHandle = templatedDocument.template.value();

                if( templateReferenceHandle != null && templatedDocument.templatedProperties == null ) {
    
                const template = await templatedDocument.template.document();
    
                if( template != null ) {
                    const templatedProperties = this.updateTemplatedProperties( templatedDocument, template! );

                        if( templatedProperties != null ) {

                            templatedProperties.fromRecord( record[templatedProperties.recordName()] as DatabaseRecord );
                        }
                    }
                }
            }

            databaseDocument.id.setValue(documentId);

            await databaseDocument.onRead(); 

            //log.traceOut("documentFromRecord()", "OK");
            return databaseDocument;

        } catch (error) {
            log.warn("monitorCollection()", "Error reading document", error);

            throw new Error( (error as any).message );
        }
    }

    protected pathElementsFromUri( url : string ) : string[] {

        //log.traceIn( "elementsFromUri()", url );

        try {
            let elements : string[] = [];

            if( url == null || url.length === 0 ) {
                //log.traceOut( "elementsFromUri()", "no elements" );
                return elements;
            }

            let path = url.split( "?" )[0];

            const pathElements = path.startsWith("/") ? 
            path.substring(1).split("/") : path.split("/");  // remove leading "/" and split the rest

            if( pathElements.length === 0 ) {
                //log.traceOut( "elementsFromUri()", "empty" );
                return elements;
            }

            let foundCollection = false;

            for( const urlElement of pathElements ) {

                if( !foundCollection ) {

                    if( this.collectionNames().includes( urlElement.split( CollectionGroupPathSuffix )[0] ) ) {

                        foundCollection = true;
                    }
                }

                if( foundCollection ) {
                    elements.push( urlElement ); 
                }
            }

            //log.traceOut( "elementsFromUri()", elements );
            return elements;
        } catch( error ) {
            log.warn( "elementsFromUri()", "Error database path from url", error );

            throw new Error( (error as any).message );
        }
    }

    parentCollections(database: Database<DatabaseDocument>): Database<DatabaseDocument>[] {

        try {
            let databases: Database<DatabaseDocument>[] = [];

            const ownerCollections = database.owner() != null ?
                database.owner()!.parentCollections(database.collectionName() ) as Database<DatabaseDocument>[] : undefined;

            if (ownerCollections != null) {

                for (const ownerCollection of ownerCollections) {

                    if (ownerCollection.databaseAccess().allowRead) {
                        databases = databases.concat(ownerCollection);
                    }
                }
            }

            const rootCollection = this.collectionDatabaseFromCollectionName(database.collectionName())!;

            if (rootCollection.allowRootCollection && rootCollection.databaseAccess().allowRead) {
                databases = databases.concat(rootCollection);
            }

            return databases;

        } catch( error ) {
            log.warn( "parentCollections()", "Error reading parent collections", error );

            throw new Error( (error as any).message );
        }
    }

    newDatabaseObserver( databaseQuery? : DatabaseQuery<DatabaseDocument> ) : DatabaseObserver<DatabaseDocument> {

        return new DatabaseObserverImpl( databaseQuery ); 
    }

    templatePathFromUri( uri : string  ) : string | undefined {

        //log.traceIn( "templatePathFromUri()", {url} );

        try {
            if( !uri.includes("?") || uri.endsWith("?") ) {
                //log.traceOut( "templatePathFromUri()", "URL has no parameters", {url} );
                return undefined;
            }

            let urlParameters = uri.split("?")[1];

            if( !urlParameters.includes( TemplatePathKey + "=" ) ) {
                //log.traceOut( "templatePathFromUri()", "Template parameter not found in URL", url );
                return undefined;
            }

            let templatePath = urlParameters.split( TemplatePathKey + "=" )[1]; 

            templatePath = templatePath.split( "?" )[0];

            templatePath = templatePath.split( "&" )[0];

            if( templatePath != null && templatePath.length > 0 ) {
                //log.traceOut( "templatePathFromUri()", {templatePath} );
                return decodeURIComponent( templatePath );
            }
            
            //log.traceOut( "templatePathFromUri()", "empty template path in URL", url );
            return undefined;

        } catch( error ) {
            log.warn( "templatePathFromUri()", "Error reading template from path", error );

            throw new Error( (error as any).message );
        }
    }

    templateReferenceHandleFromUri( templateUri : string  ) : ReferenceHandle<Template<TemplatedDocument>> | undefined {

        //log.traceIn( "templateReferenceHandleFromUri()", {templateUri} );

        try {
            const templatePath = this.templatePathFromUri( templateUri );

            if( templatePath != null ) {

                const templateReferenceHandle = new ReferenceHandle<Template<TemplatedDocument>>({ 
                    path: templatePath,
                    uri: templateUri 
                });

                //log.traceOut( "templateReferenceHandleFromUri()", {templateReferenceHandle} );
                return templateReferenceHandle;
            }
            
            //log.traceOut( "templateReferenceHandleFromUri()", undefined );
            return undefined;

        } catch( error ) {
            log.warn( "templateReferenceHandleFromUri()", "Error reading template from path", error );

            throw new Error( (error as any).message );
        }
    }


    newTemplatedDocument( template: Template<TemplatedDocument> ) : DatabaseDocument {

        try {
            log.traceIn("newTemplatedDocument()", {template} );

            const owner = template.instanceOwner.emptyDocument()!;

            const collectionName = template.instanceCollectionName.value()!;

            const templateCollection = this.collectionDatabaseFromCollectionName( collectionName, owner )!;

            let newDocument = templateCollection.newDocument()! as TemplatedDocument;

            /*
            const documentName = template.instanceDocumentName.value()!;

            if( documentName != null ) {

                const path = newDocument.databasePath() + "?" + DatabaseDocumentNameKey + "=" + documentName; 

                newDocument = this.newDocument( newDocument.collectionDatabase, path );

                //newDocument.name.setValue( documentName );
            }
            */

            this.updateTemplatedProperties( newDocument, template, true );

            newDocument.template.setDocument( template.referenceHandle() as ReferenceHandle<Template<TemplatedDocument>> );

            log.traceOut("newTemplatedDocument()", {newDocument}, newDocument.template.value() ); 
            return newDocument;

        } catch (error) {
            log.warn("Error handling new template instance ", error); 

            throw new Error( (error as any).message  );
        }
    }

    updateTemplatedProperties( templatedDocument : TemplatedDocument, 
        template: Template<TemplatedDocument>,
        applyEditRestrictions? : boolean ) : TemplatedProperties | undefined {

        try {
            //log.traceIn("updateTemplatedProperties()", {databaseDocument}, {template} );

            templatedDocument.templatedProperties = new SubdocumentPropertyImpl( templatedDocument, 
                () => new TemplatedPropertiesImpl( templatedDocument, "templatedProperties")
            );

            const templatedPropertiesSubdocument = 
                templatedDocument.templatedProperties.subdocument()!;

            const propertyDescriptors = template.propertyDescriptors.value();

            if( propertyDescriptors == null ) {

                //log.traceOut("updateTemplatedProperties()", "No property descriptors"); 
                return undefined;
            }

            for( const propertyDescriptor of propertyDescriptors.values() ) {

                const propertyKey = propertyDescriptor.title.value()!;

                const property = 
                    (propertyDescriptor as PropertyDescriptorImpl<DatabaseProperty<any>>).newProperty( 
                        templatedPropertiesSubdocument, 
                        propertyDescriptor.propertyType.value()!,
                        applyEditRestrictions )!;

                (templatedPropertiesSubdocument as any)[propertyKey] = property;

            }
            
            //log.traceOut("updateTemplatedProperties()", {templatedPropertiesSubdocument}); 
            return templatedPropertiesSubdocument;

        } catch (error) {
            log.warn("Error adding templated properties ", error); 

            throw new Error( (error as any).message  );
        }
    }

    collectionGroupDatabaseFromCollectionName(  
        collectionName: string, 
        owner?: DatabaseDocument,
        template? : Template<TemplatedDocument> ): CollectionGroupDatabase<DatabaseDocument> | undefined {

        try {
            let collectionGroup;

            if( owner == null ) {

                collectionGroup = this._collectionGroups.get( collectionName );

                if( collectionGroup == null ) {

                    const collectionConfig = 
                        configurationServiceFactory!.get().config( CollectionsConfigurationName, collectionName ) as any;

                    if( !collectionConfig.rootCollection ) {
                        throw new Error( "Not a root collection: " + collectionName );
                    }

                    collectionGroup = new CollectionGroupDatabaseImpl<DatabaseDocument>(
                        this.databaseManager, 
                        collectionName,
                        collectionConfig.queryDocumentName,
                        collectionConfig.documentNames,
                        collectionConfig.rootCollection,
                        collectionConfig.encrypted,
                        undefined,
                        template
                    );

                    this._collectionGroups.set( collectionName, collectionGroup );
                }
            }
            else {
                let subcollectionGroups = this._subcollectionGroups.get( collectionName );

                if( subcollectionGroups == null ) {

                    subcollectionGroups = new Map<string,CollectionGroupDatabase<DatabaseDocument>>();

                    this._subcollectionGroups.set( collectionName, subcollectionGroups );
                }

                collectionGroup = subcollectionGroups.get( owner.path() );

                if( collectionGroup == null ) {

                    const collectionConfig = configurationServiceFactory!.get().config( CollectionsConfigurationName, collectionName ) as any;

                    collectionGroup = new CollectionGroupDatabaseImpl<DatabaseDocument>(
                        this.databaseManager, 
                        collectionName,
                        collectionConfig.queryDocumentName,
                        collectionConfig.documentNames,
                        collectionConfig.rootCollection,
                        collectionConfig.encrypted,
                        owner,
                        template
                    ) as CollectionGroupDatabase<DatabaseDocument>;

                    subcollectionGroups.set( owner.path(), collectionGroup );
                }
            }

            return collectionGroup;

        } catch( error ) {
            log.warn( "collectionGroupDatabaseFromCollectionName()", "Error retrieving collection group from: " + collectionName, error );

            throw new Error( (error as any).message );
        }
    }

    collectionGroupDatabaseFromDocumentName( 
        documentName : string, 
        owner? : DatabaseDocument,
        template? : Template<TemplatedDocument> ) : CollectionGroupDatabase<DatabaseDocument> | undefined {
        
         try {
            const collectionName = this._documentNamesToCollectionNames.get( documentName );

            if( collectionName == null ) {
                throw new Error( "No collection found for: " + documentName );
            }
            
            const collectionGroup = this.collectionGroupDatabaseFromCollectionName(
                collectionName,
                owner,
                template
            );

            return collectionGroup;

        } catch( error ) {
            log.warn( "collectionGroupDatabaseFromCollectionName()", "Error retrieving collection group from: " + documentName, error );

            throw new Error( (error as any).message );
        }    
    }

    collectionDatabaseFromCollectionName(
        collectionName: string, 
        owner?: DatabaseDocument,
        template? : Template<TemplatedDocument>): CollectionDatabase<DatabaseDocument> | undefined {

        try {
            let collection;

            if( owner == null ) {

                collection = this._collections.get( collectionName );

                if( collection == null ) {

                    const collectionConfig = 
                        configurationServiceFactory!.get().config( CollectionsConfigurationName, collectionName ) as any;

                    if( !collectionConfig.rootCollection ) {
                        throw new Error( "Not a root collection: " + collectionName );
                    }

                    collection = new CollectionDatabaseImpl<DatabaseDocument>(
                        this.databaseManager, 
                        collectionName,
                        collectionConfig.queryDocumentName,
                        collectionConfig.documentNames,
                        collectionConfig.rootCollection,
                        collectionConfig.encrypted,
                        undefined,
                        template
                    );

                    this._collections.set( collectionName, collection );
                }
            }
            else {
                let subcollections = this._subcollections.get( collectionName );

                if( subcollections == null ) {

                    subcollections = new Map<string,CollectionDatabase<DatabaseDocument>>();

                    this._subcollections.set( collectionName, subcollections );
                }

                collection = subcollections.get( owner.path() );

                if( collection == null ) {

                    const collectionConfig = 
                        configurationServiceFactory!.get().config( CollectionsConfigurationName, collectionName ) as any;

                    collection = new CollectionDatabaseImpl<DatabaseDocument>(
                        this.databaseManager, 
                        collectionName,
                        collectionConfig.queryDocumentName,
                        collectionConfig.documentNames,
                        collectionConfig.rootCollection,
                        collectionConfig.encrypted,
                        owner,
                        template
                    ) as CollectionDatabase<DatabaseDocument>;

                    subcollections.set( owner.path(), collection );
                }
            }

            return collection;

        } catch( error ) {
            log.warn( "collectionDatabaseFromCollectionName()", "Error retrieving collection from: " + collectionName, error );

            throw new Error( (error as any).message );
        }
    }

    collectionDatabaseFromDocumentName( 
        documentName : string, 
        owner? : DatabaseDocument,
        template? : Template<TemplatedDocument> ) : CollectionDatabase<DatabaseDocument> | undefined {
        
        try {
            const collectionName = this._documentNamesToCollectionNames.get( documentName );

            if( collectionName == null ) {
                throw new Error( "No collection found for: " + documentName );
            }
            
            const collection = this.collectionDatabaseFromCollectionName(
                collectionName,
                owner,
                template
            );

            return collection;

        } catch( error ) {
            log.warn( "collectionDatabaseFromCollectionName()", "Error retrieving collection from: " + documentName, error );

            throw new Error( (error as any).message );
        }    
    }

    newDocument( collectionDatabase : CollectionDatabase<DatabaseDocument>, documentUri? : string ) : DatabaseDocument | undefined {

        let documentName = collectionDatabase.defaultDocumentName();

        let uriTemplateReferenceHandle;

        if( documentUri != null ) {

            const uriDocumentName = this.documentNameFromUri( documentUri );

            uriTemplateReferenceHandle = this.templateReferenceHandleFromUri( documentUri );

            if( uriDocumentName != null ) {
                documentName = uriDocumentName;
            }
        }

        if( documentName == null ) {
            throw new Error( "No document name for database collection or documentUri: " + documentUri );
        }

        const onNewDocument = GenericDatabaseFactory._onNewDocuments.get( documentName );

        if( onNewDocument == null ) {
            throw new Error( "No constructor registered for: " + documentName );
        }

        const databaseDocument = onNewDocument( 
            documentName,
            collectionDatabase,
            documentUri );

        if( uriTemplateReferenceHandle != null ) {

            (databaseDocument as TemplatedDocument).template.setValue( uriTemplateReferenceHandle );
        }

        return databaseDocument;
    }

    collectionNames() : string[] {
        return Array.from( this._documentNamesToCollectionNames.values() );
    }

    rootCollectionNames() : string[] {
        return this._rootCollectionNames;
    }

    documentNames() : string[] {
        return Array.from( this._documentNamesToCollectionNames.keys() );
    }

    readonly configurationManager : ConfigurationManager;

    readonly databaseManager : DatabaseManager;

    protected readonly _collectionGroups = new Map<string,CollectionGroupDatabase<DatabaseDocument>>();
    protected readonly _subcollectionGroups = new Map<string,Map<string,CollectionGroupDatabase<DatabaseDocument>>>();
    protected readonly _collections = new Map<string,CollectionDatabase<DatabaseDocument>>();
    protected readonly _subcollections = new Map<string,Map<string,CollectionDatabase<DatabaseDocument>>>();

    protected readonly _documentNamesToCollectionNames = new Map<string,string>();
    protected readonly _rootCollectionNames = [] as string[];

    protected static readonly _onNewDocuments = new Map<string,( 
        documentName : string,
        collectionDatabase : CollectionDatabase<DatabaseDocument>,
        documentUri? : string ) => DatabaseDocument>();
    

}
