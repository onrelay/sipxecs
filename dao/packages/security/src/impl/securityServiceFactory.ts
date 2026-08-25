import { ServiceFactory } from "@dao/common";
import { SecurityService } from "../spec/securityService";

export let securityServiceFactory : SecurityServiceFactory | undefined;

export class SecurityServiceFactory extends ServiceFactory<SecurityService> {

    static create( securityService : SecurityService ) : void {

        securityServiceFactory = new SecurityServiceFactory();

        securityServiceFactory.init( securityService );
    }
}