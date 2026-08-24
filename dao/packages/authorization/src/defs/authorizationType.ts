export const AuthorizationTypeName = "authorizationType";

export const AuthorizationTypes = {

    List : "list",

    Create : "create",

    Read : "read",

    Update : "update",

    Delete : "delete",

    Write : "write" // includes Create, Update, Delete

} as const

export type AuthorizationType = (typeof AuthorizationTypes)[keyof typeof AuthorizationTypes];

export function authorizationTypeImplies( granted : AuthorizationType, required : AuthorizationType ) : boolean {

    if( granted === required ) {
        return true;
    }

    return granted === AuthorizationTypes.Write &&
        ( required === AuthorizationTypes.Create ||
          required === AuthorizationTypes.Update ||
          required === AuthorizationTypes.Delete );
}
