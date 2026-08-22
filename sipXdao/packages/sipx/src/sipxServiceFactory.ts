import { ServiceFactory } from "@sipxdao/common"
import { SipxService } from "./sipxService";

export let sipxServiceFactory : SipxServiceFactory | undefined;

export class SipxServiceFactory extends ServiceFactory<SipxService> {

    static create( sipxService : SipxService ) : void {

        sipxServiceFactory = new SipxServiceFactory();

        sipxServiceFactory.init( sipxService );
    }
}