import { Service } from "@sipxdao/common"
import { PersistentState } from "./persistentState/api/persistentState";
import { ConfigurationManager } from "@sipxdao/configuration";

export interface ClientService extends Service {

    init() : Promise<void>;

    readonly configurationManager: ConfigurationManager;

    readonly persistentState: PersistentState;
}

