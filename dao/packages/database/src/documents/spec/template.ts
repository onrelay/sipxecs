import { NumberProperty } from "../../properties/spec/numberProperty";
import { SubdocumentsProperty } from "../../properties/spec/subdocumentsProperty";
import { SymbolicCollectionProperty } from "../../properties/spec/symbolicCollectionProperty";
import { TextProperty } from "../../properties/spec/textProperty";
import { DatabaseDocument } from "../../core/spec/databaseDocument";
import { DatabaseProperty } from "../../core/spec/databaseProperty";
import { TemplatedDocument } from "../../core/spec/templatedDocument";
import { PropertyDescriptor } from "../../core/spec/propertyDescriptor";
import { ReferenceProperty } from "../../properties/spec/referenceProperty";
import { User } from "./user";

export interface Template<Document extends TemplatedDocument> extends DatabaseDocument  {  

    readonly instanceOwner : ReferenceProperty<User>,

    readonly instanceCollectionName : TextProperty,

    readonly instanceDocumentName : TextProperty,

    readonly propertyDescriptors : SubdocumentsProperty<PropertyDescriptor<DatabaseProperty<any>>>,

    readonly version : NumberProperty,

    readonly instances : SymbolicCollectionProperty<TemplatedDocument>
}

