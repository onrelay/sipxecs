export const DatabasePlatformName = "databasePlatform";

export const DatabasePlatforms = {

    Configuration : "configuration",

    Firestore : "firestore",

    Mongo : "mongo",

    PostgreSql : "postgresql",

    Rest : "rest"

} as const

export type DatabasePlatform = (typeof DatabasePlatforms)[keyof typeof DatabasePlatforms];
