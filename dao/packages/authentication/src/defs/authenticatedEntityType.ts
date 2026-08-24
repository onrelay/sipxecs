
export const AuthenticatedEntityTypes = {

    Administrator     : "administrator",

    User              : "user",

    Device            : "device",

    Service           : "service"

} as const

export type AuthenticatedEntityType = (typeof AuthenticatedEntityTypes)[keyof typeof AuthenticatedEntityTypes]; 




