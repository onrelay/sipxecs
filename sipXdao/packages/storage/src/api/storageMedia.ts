import { MediaType } from "./mediaTypes";

export type StorageMedia = {

    path: string,

    mediaType?: MediaType,

    data?: Blob | File | Uint8Array | string,

    bytesTransferred? : number,

    totalBytes? : number,

    metadata?: any,

    downloadUrl?: string

};
