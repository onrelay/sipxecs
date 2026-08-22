import { Database } from "../dao/database";
import { DatabaseDocument } from "../dao/databaseDocument";
import { DatabaseProperty } from "../dao/databaseProperty";
import { ReferenceHandle } from "../dao/referenceHandle";


export interface OwnerProperty<DerivedDocument extends DatabaseDocument> 
    extends DatabaseProperty<ReferenceHandle<DerivedDocument>> { 

    collectionName() : string;

    documentName() : string;

    id() : string | undefined, // ID for last owner

    ids() : string[] | undefined,

    path() : string | undefined,  // path for last owner

    paths() : string[] | undefined,

    depth() : number | undefined,

    referenceHandle() : ReferenceHandle<DerivedDocument> | undefined,  // reference handle for last owner

    referenceHandles() : Map<string,ReferenceHandle<DerivedDocument>> | undefined,

    emptyDocument(): DerivedDocument | undefined, // empty document for last owner

    document(): Promise<DerivedDocument | undefined>, // document for last owner

    documents(): Promise<Map<string,DerivedDocument> | undefined>,

    setDocument( referenceHandle : ReferenceHandle<DerivedDocument> ): void, // reference handle for last owner

    clearDocuments(): void,

    options() : Promise<Map<string,ReferenceHandle<DerivedDocument>> | undefined>,

    select( params? : { 
        collectionGroup? : boolean,
        fetch: boolean }) : Promise<Map<string,ReferenceHandle<DerivedDocument>> | undefined>,

    selectDatabase( collectionGroup? : boolean ) : Database<DatabaseDocument> | undefined,

    cleared : boolean
}


