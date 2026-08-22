import { Service } from "@sipxdao/common"
import { ConfigurationManager } from "@sipxdao/configuration";

export interface ServerService extends Service {

    init() : Promise<void>;

    readonly configurationManager: ConfigurationManager;

}

