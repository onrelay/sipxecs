import { Configuration, Environment, Platform, Target } from "@dao/common";
import { AbstractSipxApplication } from "@sipxdao/sipx";

export abstract class SipxServerApplication extends AbstractSipxApplication {

    constructor( params: {
        name: string,
        environment: Environment,
        platform: Platform,
        target: Target,
        configuration: Configuration
    } ) {
        super( params );
    }
}
