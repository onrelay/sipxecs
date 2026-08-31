export const HttpOperationName = "httpOperation";

export const HttpOperations = {

    Get       : "GET",

    Post      : "POST",

    Delete    : "DELETE",

    Update    : "UPDATE"

}

export type HttpOperation = keyof (typeof HttpOperations); 



