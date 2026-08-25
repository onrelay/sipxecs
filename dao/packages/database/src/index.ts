export { AbstractDatabaseAccessor } from "./core/base/abstractDatabaseAccessor";
export { AbstractDatabaseConverter } from "./core/base/abstractDatabaseConverter";
export { BasicDatabaseConverter } from "./core/impl/configurationConverter";
export { GenericDatabaseDocument } from "./core/impl/genericDatabaseDocument";
export { GenericDatabaseFactory } from "./core/impl/genericDatabaseFactory";
export { AbstractDatabaseManager } from "./core/base/abstractDatabaseManager";
export { AbstractDatabaseProperty } from "./core/base/abstractDatabaseProperty";
export { AbstractDatabaseObject as AbstractDatabaseRecord } from "./core/base/abstractDatabaseObject";
export { AbstractDatabaseService } from "./core/base/abstractDatabaseService";
export { AbstractTemplatedDocument } from "./core/base/abstractTemplatedDocument";
export { Change } from "./documents/spec/change";
export { User as Entity } from "./documents/spec/user";
export { CollectionDatabase } from "./core/spec/collectionDatabase";
export { CollectionGroupDatabase } from "./core/spec/collectionGroupDatabase";
export { DocumentsDatabase } from "./core/spec/documentsDatabase";
export { Database } from "./core/spec/database";
export { DatabaseConverter } from "./core/spec/databaseConverter";
export { DatabaseDocument, ArchivedPropertyKey, DatabaseDocumentNameKey } from "./core/spec/databaseDocument";
export { DatabaseFactory } from "./core/spec/databaseFactory";
export { DatabaseFilter } from "./core/types/databaseFilter";
export { DatabaseManager } from "./core/spec/databaseManager";
export { DatabaseObserver } from "./core/spec/databaseObserver";
export { DatabaseProperty } from "./core/spec/databaseProperty";
export { DatabaseObject } from "./core/spec/databaseObject";
export { DatabaseService, TemplatePathKey, IdSuffix, OwnerIds } from "./core/spec/databaseService";
export { DatabaseServiceFactory, databaseServiceFactory } from "./core/impl/databaseServiceFactory";
export { AbstractFirestoreDatabaseManager } from "./backends/firestore/abstractFirestoreDatabaseManager"
export { FirestoreConverter } from "./backends/firestore/firestoreConverter"
export { PropertyDescriptor } from "./core/spec/propertyDescriptor";
export { PropertiesSelector } from "./core/types/propertiesSelector";
export { ReferenceHandle } from "./core/impl/referenceHandle";
export { TemplatedDocument } from "./core/spec/templatedDocument";
export { TemplatedProperties } from "./core/spec/templatedProperties";
export { Template } from "./documents/spec/template";

export { AttachmentProperty } from "./properties/spec/attachmentProperty";
export { AttachmentsProperty } from "./properties/spec/attachmentsProperty";
export { BasicProperty } from "./properties/spec/basicProperty";
export { BooleanProperty } from "./properties/spec/booleanProperty";
export { CollectionProperty } from "./properties/spec/collectionProperty";
export { ConfirmationProperty } from "./properties/spec/confirmationProperty";
export { CountryProperty } from "./properties/spec/countryProperty";
export { DataProperty } from "./properties/spec/dataProperty";
export { DateProperty } from "./properties/spec/dateProperty";
export { DefinitionProperty } from "./properties/spec/definitionProperty";
export { DefinitionsProperty } from "./properties/spec/definitionsProperty";
export { DocumentProperty } from "./properties/spec/documentProperty";
export { DocumentsProperty } from "./properties/spec/documentsProperty";
export { EmptyProperty } from "./properties/spec/emptyProperty";
export { GeolocationProperty } from "./properties/spec/geolocationProperty";
export { ImageProperty } from "./properties/spec/imageProperty";
export { LinksProperty } from "./properties/spec/linksProperty";
export { LongTextProperty } from "./properties/spec/LongTextProperty";
export { MapProperty } from "./properties/spec/mapProperty";
export { NumberProperty } from "./properties/spec/numberProperty";
export { OwnerProperty } from "./properties/spec/ownerProperty";
export { PhoneNumberProperty } from "./properties/spec/phoneNumberProperty";
export { ReferenceProperty } from "./properties/spec/referenceProperty";
export { ReferencesProperty } from "./properties/spec/referencesProperty";
export { SubdocumentProperty } from "./properties/spec/subdocumentProperty";
export { SymbolicCollectionProperty } from "./properties/spec/symbolicCollectionProperty";
export { SymbolicOwnersProperty } from "./properties/spec/symbolicOwnersProperty";
export { TemplateProperty } from "./properties/spec/templateProperty";
export { TextProperty } from "./properties/spec/textProperty";
export { TextsProperty } from "./properties/spec/textsProperty";

export { Comparator, Comparators, ComparatorName } from "./core/defs/comparator";
export { DatabaseType, DatabaseTypes, DatabaseTypeName } from "./core/defs/databaseType";
export { DatabaseAccess } from "./core/impl/databaseAccess";
export { DatabaseAccessor } from "./core/spec/databaseAccessor";
export { PropertyType, PropertyTypes, } from "./core/defs/propertyType";
export { OptionsSource } from "./core/spec/optionsSource";
export { Address } from "./documents/spec/address";
export { DateRange } from "./core/types/dateRange";
export { TextType } from "./core/defs/textType";

export { log } from "./core/base/abstractDatabaseService"




