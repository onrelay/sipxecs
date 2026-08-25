
export const AuthenticationMethods = {

    Open    : "open",

    Basic   : "basic",

    MultiFactor : "multiFactor",

    Certificate  : "certificate",

    Token : "token"

}

export type AuthenticationMethod = keyof (typeof AuthenticationMethods); 




