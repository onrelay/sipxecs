import { ConfigurationManager } from "@dao/configuration";
import { AbstractSipxAuthenticationService } from "@sipxdao";

export class SipxServerAuthenticationService extends AbstractSipxAuthenticationService {

    constructor( configurationManager : ConfigurationManager ) {

        super( configurationManager );
    }
}
