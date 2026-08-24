import { Service } from "@dao/common"
import { AuthenticatedEntity } from "../types/authenticatedEntity";

export const AuthenticationServiceName = "authentication";

export interface AuthenticationService extends Service {

    init() : Promise<void>;

    authenticatedEntity() : AuthenticatedEntity | undefined;
}

