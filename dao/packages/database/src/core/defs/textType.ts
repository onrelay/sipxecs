
export const TextTypeName = "textType";

export const TextTypes = {

    Normal: "normal",

    Capitalized: "capitalized",

    LowerCase: "lowerCase",

    UpperCase: "upperCase",

    Email: "email",

    Password: "password", 

    Url: "url"
}

export type TextType = keyof (typeof TextTypes);

export const DefaultTextType = TextTypes.Normal as TextType;