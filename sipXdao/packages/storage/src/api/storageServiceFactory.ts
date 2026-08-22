import { ServiceFactory } from "@sipxdao/common";
import { StorageService } from "./storageService";

export let storageServiceFactory: StorageServiceFactory | undefined;

export class StorageServiceFactory extends ServiceFactory<StorageService> {

    static create( storageService: StorageService ): void {
        storageServiceFactory = new StorageServiceFactory();
        storageServiceFactory.init( storageService );
    }
}
