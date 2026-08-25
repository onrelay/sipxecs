import { DatabaseProperty } from "../../core/spec/databaseProperty";
import { DatabaseSubdocument } from "../../core/spec/databaseSubdocument";

export interface SubdocumentsProperty<DerivedSubdocument extends DatabaseSubdocument> 
    extends DatabaseProperty<Map<string,DerivedSubdocument>> {

    newSubdocument() : DerivedSubdocument;

    setSubdocument( subdocument : DerivedSubdocument ): void;

    removeSubdocument( subdocumentId : string ): boolean;
    
    subdocuments() : Map<string,DerivedSubdocument> | undefined 

}

