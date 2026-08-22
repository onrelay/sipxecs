
export const DatabaseAccessTypes = {

    List :        "list",

    Create :     "create",

    Read :       "read",

    Update :     "update",

    Delete :      "delete"

}

export type DatabaseAccessType = keyof (typeof DatabaseAccessTypes);


