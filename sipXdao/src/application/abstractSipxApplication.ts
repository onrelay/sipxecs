import { Application, Configuration, Environment, Platform, Target } from "@dao/common";

export abstract class AbstractSipxApplication extends Application {

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
