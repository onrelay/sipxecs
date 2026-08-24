export const LanguageName = "language";

export const Languages = {

    English           : "en",

    Norwegian          : "nb"
} as const

export type Language = (typeof Languages)[keyof typeof Languages];
