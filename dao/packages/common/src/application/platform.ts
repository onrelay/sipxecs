
export const Platforms = {

    Firebase             : "firebase",

    Atlas                : "atlas",

    Linux                : "linux"
}

export type Platform = keyof (typeof Platforms);

export const FirebasePlatform = Platforms.Firebase as Platform;

export const AtlasPlatform = Platforms.Atlas as Platform;

export const LinuxPlatform = Platforms.Linux as Platform;


