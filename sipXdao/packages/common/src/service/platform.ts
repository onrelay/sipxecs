
export const Platforms = {

    Firebase             : "firebase",

    Mongo                : "mongo"
}

export type Platform = keyof (typeof Platforms);

export const FirebasePlatform= Platforms.Firebase as Platform;

export const MongoPlatform = Platforms.Mongo as Platform;




