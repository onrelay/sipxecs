
export const KeyVaultName = "keyVault";

export const KeyVaults = {

    Google : "google",

    Internal : "internal"
} as const

export type KeyVault = (typeof KeyVaults)[keyof typeof KeyVaults];
