export { ClientFirestoreDatabaseManager } from "./database/backends/firestore/clientFirestoreDatabaseManager";
export { ClientApiDatabaseManager } from "./database/backends/rest/clientApiDatabaseManager";

export { AbstractPersistentState } from "./persistentState/base/abstractPersistentState";
export { PersistentState } from "./persistentState/spec/persistentState";
export { default as CookiesPersistentState } from "./persistentState/impl/cookiesPersistentState";
export { default as LocalStoragePersistentState } from "./persistentState/impl/localStoragePersistentState";

