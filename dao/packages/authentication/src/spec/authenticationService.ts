import { Service } from "@dao/common"
import { AuthenticatedEntity } from "../types/authenticatedEntity";

export interface AuthenticationService extends Service {

    init() : Promise<void>;

    authenticatedEntity? : AuthenticatedEntity;
}

