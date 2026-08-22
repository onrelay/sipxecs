import { ServiceFactory } from "@sipxdao/common"
import { ClientService } from "./clientService";

export let clientServiceFactory : ClientServiceFactory | undefined;

export class ClientServiceFactory extends ServiceFactory<ClientService> {

    static create( clientService : ClientService ) : void {

        clientServiceFactory = new ClientServiceFactory();

        clientServiceFactory.init( clientService );
    }
}