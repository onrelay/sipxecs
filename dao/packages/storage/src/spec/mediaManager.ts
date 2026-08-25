import { Monitor } from "@dao/common";

import { StorageMedia } from "../types/storageMedia";

export interface MediaManager  {

    init() : Promise<void>;

    upload( media : StorageMedia, monitor? : Monitor ) : Promise<void>;

    downloadUrl( media: StorageMedia ): Promise<string | undefined>;

    download( media : StorageMedia, monitor? : Monitor ) : Promise<void>;

    delete( path : string ) : Promise<void>;
}
 
