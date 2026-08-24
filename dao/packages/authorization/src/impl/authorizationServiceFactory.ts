import { ServiceFactory } from "@dao/common"
import { AuthorizationService } from "../spec/authorizationService";

export let authorizationServiceFactory : AuthorizationServiceFactory | undefined;

export class AuthorizationServiceFactory extends ServiceFactory<AuthorizationService> {

    static create( authorizationService : AuthorizationService ) : void {

        authorizationServiceFactory = new AuthorizationServiceFactory();

        authorizationServiceFactory.init( authorizationService );
    }
}
