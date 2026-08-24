
export const TextTypeName = "textType";

export const TextTypes = {

    Normal: "normal",

    Capitalized: "capitalized",

    LowerCase: "lowerCase",

    UpperCase: "upperCase",

    Email: "email",

    Password: "password", 

    Url: "url"
} as const

export type TextType = (typeof TextTypes)[keyof typeof TextTypes];

export const DefaultTextType = TextTypes.Normal;