import { Service } from "@sipxdao/common"
import { AuthenticatedEntity } from "./authenticatedEntity";

export interface AuthenticationService extends Service {

    init() : Promise<void>;

    authenticatedEntity? : AuthenticatedEntity;
}

