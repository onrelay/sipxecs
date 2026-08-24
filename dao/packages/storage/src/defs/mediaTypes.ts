export const MediaTypeName = "mediaType";

export const MediaTypes = {

    Image       : "image",
    
    Video       : "video",

    Document    : "document"

} as const

export type MediaType = (typeof MediaTypes)[keyof typeof MediaTypes];
