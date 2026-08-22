import { DatabaseSubdocument } from "../dao/databaseSubdocument";
import { DatabaseProperty } from "../dao/databaseProperty";

export interface SubdocumentProperty<DerivedSubdocument extends DatabaseSubdocument> 
    extends DatabaseProperty<DerivedSubdocument> {

    subdocument() : DerivedSubdocument | undefined
}

