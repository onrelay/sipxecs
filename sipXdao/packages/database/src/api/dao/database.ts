import { AbstractObservable, Observable, Observation } from "@sipxdao/common";

import { DatabaseDocument } from "./databaseDocument";
import { ReferenceHandle } from "./referenceHandle";
import { DatabaseAccess } from "../types/databaseAccess";
import { DatabaseType } from "../types/databaseType";

export interface Database<DerivedDocument extends DatabaseDocument>  extends AbstractObservable {

    path() : string;

    uri() : string;

    newDocument( documentPath?: string ): DerivedDocument,

    documents(): Promise<Map<string,DerivedDocument>>,

    referenceHandles(): Promise<Map<string,ReferenceHandle<DerivedDocument>>>,

    document( documentPath: string ): Promise<DerivedDocument | undefined>, 

    databaseAccess() : DatabaseAccess;

    setDatabaseAccess( databaseAccess : DatabaseAccess ) : void;

    owner() : DatabaseDocument | undefined;

    collectionName() : string;

    queryDocumentName() : string | undefined;

    queryTemplatePath() : string | undefined;

    documentNames() : string[];

    notifyMonitors( observation : Observation, databaseDocuments : Map<string,DerivedDocument> ) : Promise<void>;

    defaultDocumentName() : string;

    readonly databaseType : DatabaseType;

}