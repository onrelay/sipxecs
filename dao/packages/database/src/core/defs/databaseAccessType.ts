
export const DatabaseAccessTypes = {

    List :        "list",

    Create :     "create",

    Read :       "read",

    Update :     "update",

    Delete :      "delete"

} as const

export type DatabaseAccessType = (typeof DatabaseAccessTypes)[keyof typeof DatabaseAccessTypes];


