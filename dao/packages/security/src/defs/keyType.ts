
export const KeyTypeName = "keyType";

export const KeyTypes = {

    Symmetric : "symmetric",

    Asymmetric : "asymmetric"
} as const

export type KeyType = (typeof KeyTypes)[keyof typeof KeyTypes];
