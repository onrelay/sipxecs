import { DatabaseSubdocument } from "../../core/spec/databaseSubdocument";
import { DatabaseProperty } from "../../core/spec/databaseProperty";

export interface SubdocumentProperty<DerivedSubdocument extends DatabaseSubdocument> 
    extends DatabaseProperty<DerivedSubdocument> {

    subdocument() : DerivedSubdocument | undefined
}

