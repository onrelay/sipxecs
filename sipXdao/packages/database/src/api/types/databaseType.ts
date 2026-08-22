
export const DatabaseTypeName = "databaseType";

export const DatabaseTypes = {

    Collection :       "collection",

    CollectionGroup :  "collectionGroup",

    Documents :       "documents"
}

export type DatabaseType = keyof (typeof DatabaseTypes);


