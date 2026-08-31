export { ClientFirestoreDatabaseManager } from "./database/backends/firestore/clientFirestoreDatabaseManager";
export { ClientRestDatabaseManager } from "./database/backends/rest/clientRestDatabaseManager";
export { ClientRestDatabaseTransport as ClientRestTransport } from "./database/backends/rest/clientRestDatabaseTransport";

export { AbstractPersistentState } from "./persistentState/base/abstractPersistentState";
export { PersistentState } from "./persistentState/spec/persistentState";
export { default as CookiesPersistentState } from "./persistentState/impl/cookiesPersistentState";
export { default as LocalStoragePersistentState } from "./persistentState/impl/localStoragePersistentState";

