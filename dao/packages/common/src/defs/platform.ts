export const Platforms = {

    Firebase             : "firebase",

    Atlas                : "atlas",

    Linux               : "linux"
} as const

export type Platform = (typeof Platforms)[keyof typeof Platforms];

export const FirebasePlatform = Platforms.Firebase;

export const AtlasPlatform = Platforms.Atlas;

export const LinuxPlatform = Platforms.Linux;


