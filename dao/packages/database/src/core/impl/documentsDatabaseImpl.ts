import { Monitor, Observable, Observation } from "@dao/common";
import { DatabaseDocument } from "../spec/databaseDocument";
import { DocumentsDatabase } from "../spec/documentsDatabase";
import { ReferenceHandle } from "./referenceHandle";
import { Template } from "../../documents/spec/template";
import { AbstractDatabase } from "../base/abstractDatabase";
import { DatabaseAccess } from "./databaseAccess";
import { DatabaseDocumentNameKey } from "../spec/databaseDocument";
import { TemplatePathKey } from "../spec/databaseService";
import { DocumentsProperty } from "../../properties/spec/documentsProperty";
import { TemplatedDocument } from "../spec/templatedDocument";
import { log } from "../base/abstractDatabaseService";
import { DatabaseType, DatabaseTypes } from "../defs/databaseType";

export class DocumentsDatabaseImpl<DerivedDocument extends DatabaseDocument> 
    extends AbstractDatabase<DerivedDocument> implements DocumentsDatabase<DerivedDocument> { 

    constructor( documentsProperty : DocumentsProperty<DerivedDocument>, template? : Template<TemplatedDocument>) {
 
        super( DatabaseTypes.Documents as DatabaseType,
            documentsProperty.collectionName()!, 
            documentsProperty.queryDocumentName(), 
            documentsProperty.documentNames()!, 
            documentsProperty.parentDocument()!,
            template
         ); 

        this._referencesProperty = documentsProperty;

        this.onNotify = this.onNotify.bind(this);

        //log.traceInOut( "("+this.collectionName()+")", "constructor()" );
    }

    path() : string {

        let path = this.owner()!.path() + "/" + this._referencesProperty.key();

        return path;
    }

    uri() : string {

        let uri = this.path();

        let variables = "";

        if(  this.queryDocumentName() != null  ) {
            variables += DatabaseDocumentNameKey + "=" + this.queryDocumentName();
        }

        if( this.queryTemplatePath() != null  ) {

            variables += (variables.length > 0 ? "&" : "") + TemplatePathKey + "=" + 
                encodeURIComponent( this.queryTemplatePath()! )
        }

        if( variables.length > 0 ) {
            uri += "?" + variables;
        }
        
        return uri;
    }

    documents(): Promise<Map<string,DerivedDocument>> {
        return this._referencesProperty.documents();
    }

    newDocument(documentPath?: string ): DerivedDocument {

        return this._referencesProperty.newDocument()!;
    }

    document(documentPath: string ): Promise<DerivedDocument | undefined> {

        return this._referencesProperty.document( documentPath );
    }


    async referenceHandles(): Promise<Map<string,ReferenceHandle<DerivedDocument>>> {

        return this._referencesProperty.referenceHandles();
    }

    onNotify = async (observable : Observable, 
        observation : Observation, 
        objectId? : string, 
        object? : any) : Promise<void> => {

        await super.notify( observation, objectId, object );
    }

    protected async monitor( newMonitor : Monitor ): Promise<void> {

        await this._referencesProperty.subscribe( { 
            observer: this,
            onNotify: this.onNotify 
          } as Monitor );
    }

    protected async release(): Promise<void> {
        await this._referencesProperty.unsubscribe( this );
    }

    databaseAccess() : DatabaseAccess {

        return this._referencesProperty.databaseAccess(); 
    }

    setDatabaseAccess( userAccess : DatabaseAccess ) {
        log.traceInOut("setUserAccess()", "ignored for documents"); 
    }


    private readonly _referencesProperty : DocumentsProperty<DerivedDocument>

}