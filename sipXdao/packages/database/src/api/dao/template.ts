import { NumberProperty } from "../properties/numberProperty";
import { SubdocumentsProperty } from "../properties/subdocumentsProperty";
import { SymbolicCollectionProperty } from "../properties/symbolicCollectionProperty";
import { TextProperty } from "../properties/textProperty";
import { DatabaseDocument } from "./databaseDocument";
import { DatabaseProperty } from "./databaseProperty";
import { TemplatedDocument } from "./templatedDocument";
import { PropertyDescriptor } from "./propertyDescriptor";
import { ReferenceProperty } from "../properties/referenceProperty";
import { Entity } from "./entity";

export interface Template<Document extends TemplatedDocument> extends DatabaseDocument  {  

    readonly instanceOwner : ReferenceProperty<Entity>,

    readonly instanceCollectionName : TextProperty,

    readonly instanceDocumentName : TextProperty,

    readonly propertyDescriptors : SubdocumentsProperty<PropertyDescriptor<DatabaseProperty<any>>>,

    readonly version : NumberProperty,

    readonly instances : SymbolicCollectionProperty<TemplatedDocument>
}

