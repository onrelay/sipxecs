import { ServiceFactory } from "@sipxdao/common"
import { ServerService } from "./serverService";

export let serverServiceFactory : ServerServiceFactory | undefined;

export class ServerServiceFactory extends ServiceFactory<ServerService> {

    static create( serverService : ServerService ) : void {

        serverServiceFactory = new ServerServiceFactory();

        serverServiceFactory.init( serverService );
    }
}