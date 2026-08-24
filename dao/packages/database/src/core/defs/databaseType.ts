
export const DatabaseTypeName = "databaseType";

export const DatabaseTypes = {

    Collection :       "collection",

    CollectionGroup :  "collectionGroup",

    Documents :       "documents"
} as const

export type DatabaseType = (typeof DatabaseTypes)[keyof typeof DatabaseTypes];


