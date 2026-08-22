import { Service } from "@sipxdao/common";

import { MediaManager } from "./mediaManager";

export interface StorageService extends Service {

    init() : Promise<void>;

    readonly mediaManager : MediaManager; 
}

