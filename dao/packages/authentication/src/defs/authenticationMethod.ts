
export const AuthenticationMethods = {

    Open    : "open",

    Basic   : "basic",

    MultiFactor : "multiFactor",

    Certificate  : "certificate",

    Token : "token"

} as const

export type AuthenticationMethod = (typeof AuthenticationMethods)[keyof typeof AuthenticationMethods]; 




