import { Monitor } from "./monitor";
import { Observation } from "./observations";
import { Service } from "./service";
import { AbstractObservable } from "./abstractObservable";
import { Environment } from "./environment";
import { Target } from "./target";
import { Platform } from "./platform";


export abstract class AbstractService extends AbstractObservable implements Service {

    constructor( params: {
        application: string,
        environment: Environment,
        platform: Platform,
        target: Target
        }) {

        super();

        this.application = params.application;

        this.environment = params.environment;

        this.platform = params.platform;

        this.target = params.target;
    }

    abstract init() : Promise<void>;

    protected async monitor( newMonitor : Monitor ): Promise<void> {}
 
    protected async release(observationFilter?: Observation[], objectIdsFilter?: string[]): Promise<void> {}

    abstract readonly name : string;

    abstract isInitialized : boolean;

    readonly application : string;

    readonly environment : Environment;

    readonly platform : Platform;

    readonly target : Target;
}
