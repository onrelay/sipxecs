
export const KeyVaultName = "keyVault";

export const KeyVaults = {

    Google : "google",

    Internal : "internal"
}

export type KeyVault = keyof (typeof KeyVaults);
