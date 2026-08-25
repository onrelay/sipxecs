import { Database } from "../../core/spec/database";
import { DatabaseDocument } from "../../core/spec/databaseDocument";
import { DatabaseProperty } from "../../core/spec/databaseProperty";
import { ReferenceHandle } from "../../core/impl/referenceHandle";
import { PropertyDescriptor } from "../../core/spec/propertyDescriptor";

export interface DocumentProperty<DerivedDocument extends DatabaseDocument> extends DatabaseProperty<ReferenceHandle<DerivedDocument>> {

    id() : string | undefined,

    path() : string | undefined,

    title() : string | undefined,

    referenceHandle() : ReferenceHandle<DerivedDocument> | undefined,

    emptyDocument(): DerivedDocument | undefined,

    document(): Promise<DerivedDocument | undefined>,

    setDocument( referenceHandle : ReferenceHandle<DerivedDocument> ): void,

    clearDocument(): void,

    newDocument(): DerivedDocument | undefined,

    select( params?: { 
        filterValue?: boolean,
        fetch: boolean } ) : Promise<Map<string,ReferenceHandle<DerivedDocument>> | undefined>,

    collectionName() : string | undefined,

    queryDocumentName() : string | undefined,

    documentNames() : string[] | undefined;

    queryTemplatePath() : string | undefined,

    databases() : (Database<DerivedDocument> | undefined)[] | undefined;

    primaryDatabase() : Database<DerivedDocument> | undefined;

    readonly reciprocalKey? : string,

}

export interface DocumentPropertyDescriptor<DerivedDocument extends DatabaseDocument>  
    extends PropertyDescriptor<DocumentProperty<DerivedDocument>>  { 

}

