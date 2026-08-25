import { Database } from "../../core/spec/database";
import { DatabaseDocument } from "../../core/spec/databaseDocument";
import { DatabaseProperty } from "../../core/spec/databaseProperty";
import { DocumentsDatabase } from "../../core/spec/documentsDatabase";
import { OptionsSource } from "../../core/spec/optionsSource";
import { ReferenceHandle } from "../../core/impl/referenceHandle";
import { PropertyDescriptor } from "../../core/spec/propertyDescriptor";

export interface DocumentsProperty<DerivedDocument extends DatabaseDocument> 
    extends DatabaseProperty<any>, OptionsSource {

    count() : number,

    hasDocument( documentPath : string ) : boolean; 

    paths() : string[],

    documentsDatabase() : DocumentsDatabase<DerivedDocument>;

    referenceHandles() : Map<string,ReferenceHandle<DerivedDocument>>,

    referenceHandle( documentPath: string ) : ReferenceHandle<DerivedDocument> | undefined,

    title(documentPath: string): string | undefined,

    documents(): Promise<Map<string,DerivedDocument>>,

    emptyDocument(documentPath: string): DerivedDocument | undefined,

    document(documentPath?: string ): Promise<DerivedDocument | undefined>,

    setDocument( referenceHandle : ReferenceHandle<DerivedDocument> ): void,

    setDocuments( referenceHandles : Map<string,ReferenceHandle<DerivedDocument>> ): void,  

    removeDocument( documentPath?: string): boolean,

    clearDocuments(): void,

    newDocument(): DerivedDocument | undefined,

    collectionName() : string | undefined,

    queryDocumentName() : string | undefined,

    queryTemplatePath() : string | undefined,

    documentNames() : string[] | undefined;

    options() : Promise<Map<string,ReferenceHandle<DerivedDocument>> | undefined>,

    select( params? : { 
        filterValues? : boolean,
        fetch: boolean } ) : Promise<Map<string,ReferenceHandle<DerivedDocument>> | undefined>,

    databases() : (Database<DerivedDocument> | undefined)[] | undefined;

    primaryDatabase() : Database<DerivedDocument> | undefined;

    includes( other : DocumentsProperty<DerivedDocument>, matchAny? : boolean  ) : boolean;

    compareTo( other : DocumentsProperty<DerivedDocument> ) : number;

    reciprocalKey? : string,

    minEntries? : number;

    maxEntries? : number;

}

export interface DocumentsPropertyDescriptor<DerivedDocument extends DatabaseDocument>  
    extends PropertyDescriptor<DocumentsProperty<DerivedDocument>>  { 

}

