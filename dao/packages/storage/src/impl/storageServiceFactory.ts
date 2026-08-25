import { ServiceFactory } from "@dao/common";
import { StorageService } from "../spec/storageService";

export let storageServiceFactory: StorageServiceFactory | undefined;

export class StorageServiceFactory extends ServiceFactory<StorageService> {

    static create( storageService: StorageService ): void {
        storageServiceFactory = new StorageServiceFactory();
        storageServiceFactory.init( storageService );
    }
}
