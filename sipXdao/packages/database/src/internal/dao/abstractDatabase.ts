import { AbstractObservable, Monitor, Observable, Observation } from "@sipxdao/common";
import { DatabaseDocument } from "../../api/dao/databaseDocument";
import { Database } from "../../api/dao/database";
import { Template } from "../../api/dao/template";
import { DatabaseAccess } from "../../api/types/databaseAccess";
import { ReferenceHandle } from "../../api/dao/referenceHandle";
import { TemplatedDocument } from "../../api/dao/templatedDocument";
import { databaseServiceFactory } from "../../api/dao/databaseServiceFactory";
import { log } from "../../api/dao/abstractDatabaseService";
import { DatabaseType } from "../../api/types/databaseType";


export abstract class AbstractDatabase<DerivedDocument extends DatabaseDocument> 
    extends AbstractObservable implements Database<DerivedDocument> {

    constructor(
        databaseType : DatabaseType,
        collectionName: string,
        queryDocumentName: string | undefined,
        documentNames : string[],
        owner? : DatabaseDocument,
        template? : Template<TemplatedDocument> ) {

        super();

        this.databaseType = databaseType;

        this._collectionName = collectionName;

        this._queryDocumentName = queryDocumentName;

        this._documentNames = documentNames;

        this._owner = owner;

        this._template = template;

        //log.traceInOut( "("+this.collectionName()+")", "constructor()" );
    }


    owner() : DatabaseDocument | undefined {
        return this._owner;
    }

    collectionName() : string {
        return this._collectionName;
    }

    queryDocumentName() : string | undefined {
        return this._queryDocumentName;
    }

    documentNames() : string[] {
        return this._documentNames;
    }

    defaultDocumentName() : string {
        return this._documentNames[0]!; 
    }

    queryTemplatePath() : string | undefined {
        return this._template?.path();
    }

    queryTemplateUri() : string | undefined {
        return this._template?.uri();
    }

    async notifyMonitors( observation : Observation, databaseDocuments : Map<string,DerivedDocument> ) : Promise<void>  {
    
        for( const monitor of super.observers().values() ) { 

            this.notifyMonitor( monitor, observation, databaseDocuments );
        }
    }


    protected async notifyMonitor( monitor : Monitor, observation : Observation, databaseDocuments : Map<string,DerivedDocument> ) : Promise<void> {
    
        log.traceIn("(" + this.collectionName()+ ")", "notifyMonitor()", {monitor}, {observation}, {databaseDocuments} );

        try {
            if( monitor.onNotify != null ) {

                let result;
                if( monitor.objectIdsFilter != null && monitor.objectIdsFilter.length > 0 ) {

                    result = new Map<string,DerivedDocument>();

                    for( const filteredObjectId of monitor.objectIdsFilter ) {

                        log.debug("(" + this.collectionName()+ ")", "notifyMonitor()", {filteredObjectId});

                        const filteredDocument = databaseDocuments.get( this.documentCacheKeyFromUri( filteredObjectId ) );

                        log.debug("(" + this.collectionName()+ ")", "notifyMonitor()", {filteredDocument});

                        if( filteredDocument != null ) {
                            result.set( filteredObjectId, filteredDocument );
                        }
                    }
                }
                else {
                    result = new Map<string,DerivedDocument>( [...databaseDocuments] );
                }

                if( result.size === 1 ) {

                    const resultDocument = Array.from( result.values() )[0];

                    await monitor.onNotify( this, observation, resultDocument.uri(), resultDocument  );

                    log.debug("(" + this.collectionName()+ ")", "notifyMonitor()", {resultDocument} );

                }
                else {
                    await monitor.onNotify( this, observation, this.uri(), result  );

                    log.debug("(" + this.collectionName()+ ")", "notifyMonitor()", {result} );
                }
            } 

            log.traceOut("(" + this.collectionName()+ ")", "notifyMonitors()");

        } catch (error) {

            log.warn("Error notyfying monitors for", this.collectionName(), error);
        }
    }

    protected documentCacheKeyFromUri( uri : string ) {
        return databaseServiceFactory!.get().databaseFactory.uriToPath( uri )!;
    }

    databaseAccess() : DatabaseAccess {

        if( this._databaseAccess != null ) {
            return this._databaseAccess;
        }

        return databaseServiceFactory!.get().databaseAccessor.databaseAccess( this.uri() );
    }

    setDatabaseAccess( databaseAccess : DatabaseAccess ) {
        this._databaseAccess = databaseAccess;
    }

    abstract newDocument( documentPath?: string ): DerivedDocument;

    abstract path() : string;

    abstract uri() : string;

    abstract documents(): Promise<Map<string,DerivedDocument>>;

    abstract referenceHandles(): Promise<Map<string,ReferenceHandle<DerivedDocument>>>;

    abstract document( documentPath: string ): Promise<DerivedDocument | undefined>;

    readonly databaseType : DatabaseType;
    
    private _databaseAccess? : DatabaseAccess;

    private readonly _owner? : DatabaseDocument;

    private readonly _template?: Template<TemplatedDocument>;

    private readonly _collectionName : string;

    private readonly _queryDocumentName: string | undefined;

    private readonly _documentNames : string[];

}