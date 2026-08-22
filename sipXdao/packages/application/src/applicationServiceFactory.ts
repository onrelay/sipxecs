import { ServiceFactory } from "@sipxdao/common"
import { ApplicationService } from "./applicationService";

export let applicationServiceFactory : ApplicationServiceFactory | undefined;

export class ApplicationServiceFactory extends ServiceFactory<ApplicationService> {

    static create( applicationService : ApplicationService ) : void {

        applicationServiceFactory = new ApplicationServiceFactory();

        applicationServiceFactory.init( applicationService );
    }
}