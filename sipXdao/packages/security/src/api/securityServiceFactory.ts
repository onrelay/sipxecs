import { ServiceFactory } from "@sipxdao/common";
import { SecurityService } from "./securityService";

export let securityServiceFactory : SecurityServiceFactory | undefined;

export class SecurityServiceFactory extends ServiceFactory<SecurityService> {

    static create( securityService : SecurityService ) : void {

        securityServiceFactory = new SecurityServiceFactory();

        securityServiceFactory.init( securityService );
    }
}