// Core Definitions
export { ChangeType, ChangeTypes, ChangeTypeName } from "./core/defs/changeType";
export { Comparator, Comparators, ComparatorName } from "./core/defs/comparator";
export { DatabaseAccessType, DatabaseAccessTypes } from "./core/defs/databaseAccessType";
export { DatabaseType, DatabaseTypes, DatabaseTypeName } from "./core/defs/databaseType";
export { HttpOperation, HttpOperations, HttpOperationName } from "./core/defs/httpOperation";
export { PropertyType, PropertyTypes, PropertyTypeName } from "./core/defs/propertyType";
export { SortOrientation, SortOrientations } from "./core/defs/sortOrientation";
export { TextType, TextTypes, TextTypeName } from "./core/defs/textType";

// Core Types
export { DatabaseAccessFilter } from "./core/types/databaseAccessFilter";
export { DatabaseFilter } from "./core/types/databaseFilter";
export { DatabaseQuery } from "./core/types/databaseQuery";
export { DatabaseRecord } from "./core/types/databaseRecord";
export { DatabaseSortOrder } from "./core/types/databaseSortOrder";
export { DateRange } from "./core/types/dateRange";
export { PropertiesSelector } from "./core/types/propertiesSelector";

// Core Specifications (Interfaces & Descriptors)
export { CollectionDatabase } from "./core/spec/collectionDatabase";
export { CollectionGroupDatabase } from "./core/spec/collectionGroupDatabase";
export { ConfigurationDatabaseManager } from "./core/spec/configurationDatabaseManager";
export { Database } from "./core/spec/database";
export { DatabaseAccessor } from "./core/spec/databaseAccessor";
export { DatabaseConverter } from "./core/spec/databaseConverter";
export { DatabaseDocument, ArchivedPropertyKey, DatabaseDocumentNameKey } from "./core/spec/databaseDocument";
export { DatabaseFactory, CollectionsConfigurationName } from "./core/spec/databaseFactory";
export { DatabaseManager } from "./core/spec/databaseManager";
export { DatabaseObject } from "./core/spec/databaseObject";
export { DatabaseObserver } from "./core/spec/databaseObserver";
export { DatabaseProperty } from "./core/spec/databaseProperty";
export { DatabaseService, DatabaseConfigurationName, DatabaseServiceName, TemplatePathKey, IdSuffix, OwnerIds, TemplatesCollection } from "./core/spec/databaseService";
export { DatabaseSubdocument } from "./core/spec/databaseSubdocument";
export { DocumentsDatabase } from "./core/spec/documentsDatabase";
export { XmlDatabaseDocument } from "./core/spec/xmlDatabaseDocument";
export { OptionsSource } from "./core/spec/optionsSource";
export { PropertyDescriptor } from "./core/spec/propertyDescriptor";
export { TemplatedDocument } from "./core/spec/templatedDocument";
export { TemplatedProperties } from "./core/spec/templatedProperties";

// Core Base Classes
export { AbstractDatabase } from "./core/base/abstractDatabase";
export { AbstractDatabaseAccessor } from "./core/base/abstractDatabaseAccessor";
export { AbstractDatabaseConverter } from "./core/base/abstractDatabaseConverter";
export { AbstractDatabaseManager } from "./core/base/abstractDatabaseManager";
export { AbstractDatabaseObject, AbstractDatabaseObject as AbstractDatabaseRecord } from "./core/base/abstractDatabaseObject";
export { AbstractDatabaseProperty } from "./core/base/abstractDatabaseProperty";
export { AbstractDatabaseService, log } from "./core/base/abstractDatabaseService";
export { AbstractOptionsSource } from "./core/base/abstractOptionsSource";
export { AbstractTemplatedDocument } from "./core/base/abstractTemplatedDocument";

// Core Implementations
export { CollectionDatabaseImpl } from "./core/impl/collectionDatabaseImpl";
export { CollectionGroupDatabaseImpl } from "./core/impl/collectionGroupDatabaseImpl";
export { BasicDatabaseConverter } from "./core/impl/configurationConverter";
export { ConfigurationDatabaseManagerImpl } from "./core/impl/configurationDatabaseManagerImpl";
export { DatabaseAccess } from "./core/impl/databaseAccess";
export { DatabaseObserverImpl } from "./core/impl/databaseObserverImpl";
export { DatabaseServiceFactory, databaseServiceFactory } from "./core/impl/databaseServiceFactory";
export { DocumentsDatabaseImpl } from "./core/impl/documentsDatabaseImpl";
export { GenericDatabaseDocument } from "./core/impl/genericDatabaseDocument";
export { GenericDatabaseFactory } from "./core/impl/genericDatabaseFactory";
export { GenericDatabaseSubdocument } from "./core/impl/genericDatabaseSubdocument";
export { OptionsReference } from "./core/impl/optionsReference";
export { PhoneNumber } from "./core/impl/phoneNumber";
export { PropertyDescriptorImpl } from "./core/impl/propertyDescriptorImpl";
export { ReferenceHandle } from "./core/impl/referenceHandle";
export { TemplatedPropertiesImpl } from "./core/impl/templatedPropertiesImpl";
export { UnrestrictedDatabaseAccessor } from "./core/impl/unrestrictedDatabaseAccessor";

// Documents Specifications, Base Classes & Implementations
export { Address } from "./documents/spec/address";
export { Change, ChangeTypePropertyKey } from "./documents/spec/change";
export { Entity, EntityDocumentName } from "./documents/spec/entity";
export { Key, KeyDocumentName } from "./documents/spec/key";
export { Template } from "./documents/spec/template";
export { User, UserDocumentName } from "./documents/spec/user";

export { AbstractEntity } from "./documents/base/abstractEntity";
export { AbstractUser } from "./documents/base/abstractUser";

export { ChangeImpl } from "./documents/impl/changeImpl";
export { KeyImpl } from "./documents/impl/keyImpl";
export { TemplateImpl } from "./documents/impl/templateImpl";

// Properties Specifications
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
export { SubdocumentsProperty } from "./properties/spec/subdocumentsProperty";
export { SymbolicCollectionProperty } from "./properties/spec/symbolicCollectionProperty";
export { SymbolicOwnersProperty } from "./properties/spec/symbolicOwnersProperty";
export { TemplateProperty } from "./properties/spec/templateProperty";
export { TextProperty } from "./properties/spec/textProperty";
export { TextsProperty } from "./properties/spec/textsProperty";

// Properties Implementations & Base Classes
export { AbstractBasicProperty } from "./properties/impl/abstractBasicProperty";
export { AbstractDocumentProperty } from "./properties/impl/abstractDocumentProperty";
export { AbstractDocumentsProperty } from "./properties/impl/abstractDocumentsProperty";
export { AttachmentPropertyImpl } from "./properties/impl/attachmentPropertyImpl";
export { AttachmentsPropertyImpl } from "./properties/impl/attachmentsPropertyImpl";
export { BooleanPropertyImpl } from "./properties/impl/booleanPropertyImpl";
export { CollectionPropertyImpl } from "./properties/impl/collectionPropertyImpl";
export { ConfirmationPropertyImpl } from "./properties/impl/confirmationPropertyImpl";
export { CountryPropertyImpl } from "./properties/impl/countryPropertyImpl";
export { DataPropertyImpl } from "./properties/impl/dataPropertyImpl";
export { DatePropertyImpl } from "./properties/impl/datePropertyImpl";
export { DefinitionPropertyImpl } from "./properties/impl/definitionPropertyImpl";
export { DefinitionsPropertyImpl } from "./properties/impl/definitionsPropertyImpl";
export { EmptyPropertyImpl } from "./properties/impl/emptyPropertyImpl";
export { GeolocationPropertyImpl } from "./properties/impl/geolocationPropertyImpl";
export { ImagePropertyImpl } from "./properties/impl/imagePropertyImpl";
export { LinksPropertyImpl } from "./properties/impl/linksPropertyImpl";
export { LongTextPropertyImpl } from "./properties/impl/longTextPropertyImpl";
export { MapPropertyImpl } from "./properties/impl/mapPropertyImpl";
export { NumberPropertyImpl } from "./properties/impl/numberPropertyImpl";
export { OwnerPropertyImpl } from "./properties/impl/ownerPropertyImpl";
export { PhoneNumberPropertyImpl } from "./properties/impl/phoneNumberPropertyImpl";
export { ReferencePropertyImpl } from "./properties/impl/referencePropertyImpl";
export { ReferencesPropertyImpl } from "./properties/impl/referencesPropertyImpl";
export { SubdocumentPropertyImpl } from "./properties/impl/subdocumentPropertyImpl";
export { SubdocumentsPropertyImpl } from "./properties/impl/subdocumentsPropertyImpl";
export { SymbolicCollectionPropertyImpl } from "./properties/impl/symbolicCollectionPropertyImpl";
export { SymbolicOwnersPropertyImpl } from "./properties/impl/symbolicOwnersPropertyImpl";
export { TemplatePropertyImpl } from "./properties/impl/templatePropertyImpl";
export { TextPropertyImpl } from "./properties/impl/textPropertyImpl";
export { TextsPropertyImpl } from "./properties/impl/textsPropertyImpl";

// Backends
export { AbstractFirestoreDatabaseManager } from "./backends/firestore/abstractFirestoreDatabaseManager";
export { FirestoreConverter } from "./backends/firestore/firestoreConverter";





