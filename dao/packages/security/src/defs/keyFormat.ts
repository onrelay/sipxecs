export const KeyFormatName = "keyFormat";

export const KeyFormats = {

    AES256 : "aes256",

    RSA2048 : "rsa2048"
} as const

export type KeyFormat = (typeof KeyFormats)[keyof typeof KeyFormats];
