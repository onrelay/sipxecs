import { DatabaseProperty } from "../dao/databaseProperty";
import { DatabaseSubdocument } from "../dao/databaseSubdocument";

export interface SubdocumentsProperty<DerivedSubdocument extends DatabaseSubdocument> 
    extends DatabaseProperty<Map<string,DerivedSubdocument>> {

    newSubdocument() : DerivedSubdocument;

    setSubdocument( subdocument : DerivedSubdocument ): void;

    removeSubdocument( subdocumentId : string ): boolean;
    
    subdocuments() : Map<string,DerivedSubdocument> | undefined 

}

