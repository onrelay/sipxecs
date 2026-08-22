export const MediaTypeName = "mediaType";

export const MediaTypes = {

    Image       : "image",
    
    Video       : "video",

    Document    : "document"

}

export type MediaType = keyof (typeof MediaTypes);
