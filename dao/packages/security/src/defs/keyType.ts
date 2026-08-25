
export const KeyTypeName = "keyType";

export const KeyTypes = {

    Symmetric : "symmetric",

    Asymmetric : "asymmetric"
}

export type KeyType = keyof (typeof KeyTypes);
