import { Service } from "@sipxdao/common"
import { ConfigurationManager } from "@sipxdao/configuration";

export interface SipxService extends Service {

    init() : Promise<void>;

    readonly configurationManager: ConfigurationManager;

}

