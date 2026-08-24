export const HttpOperationName = "httpOperation";

export const HttpOperations = {

    Get       : "GET",

    Post      : "POST",

    Delete    : "DELETE",

    Update    : "UPDATE"

} as const

export type HttpOperation = (typeof HttpOperations)[keyof typeof HttpOperations]; 



