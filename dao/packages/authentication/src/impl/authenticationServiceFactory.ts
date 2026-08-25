import { ServiceFactory } from "@dao/common"
import { AuthenticationService } from "../spec/authenticationService";

export let authenticationServiceFactory : AuthenticationServiceFactory | undefined;

export class AuthenticationServiceFactory extends ServiceFactory<AuthenticationService> {

    static create( authenticationService : AuthenticationService ) : void {

        authenticationServiceFactory = new AuthenticationServiceFactory();

        authenticationServiceFactory.init( authenticationService );
    }
}