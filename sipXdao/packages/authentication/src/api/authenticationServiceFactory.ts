import { ServiceFactory } from "@sipxdao/common"
import { AuthenticationService } from "./authenticationService";

export let authenticationServiceFactory : AuthenticationServiceFactory | undefined;

export class AuthenticationServiceFactory extends ServiceFactory<AuthenticationService> {

    static create( authenticationService : AuthenticationService ) : void {

        authenticationServiceFactory = new AuthenticationServiceFactory();

        authenticationServiceFactory.init( authenticationService );
    }
}