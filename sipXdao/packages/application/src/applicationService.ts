import { Service, Translator } from "@sipxdao/common"
import { ConfigurationManager } from "@sipxdao/configuration";

export interface ApplicationService extends Service {

    init() : Promise<void>;

    readonly translator : Translator;

    readonly configurationManager : ConfigurationManager;

}

