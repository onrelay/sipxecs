
export const AuthenticatedEntityTypes = {

    Administrator     : "administrator",

    User              : "user",

    Device            : "device",

    Service           : "service"

}

export type AuthenticatedEntityType = keyof (typeof AuthenticatedEntityTypes); 




