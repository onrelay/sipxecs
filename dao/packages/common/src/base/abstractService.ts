import { Monitor } from "../types/monitor";
import { Observation } from "../defs/observations";
import { AbstractObservable } from "../base/abstractObservable";
import { Context } from "../types/context";
import { Service } from "../spec/service";


export abstract class AbstractService extends AbstractObservable implements Service {

    constructor( context : Context ) {

        super();

        this.context = context;
    }

    abstract init() : Promise<void>;

    protected async monitor( newMonitor : Monitor ): Promise<void> {}
 
    protected async release(observationFilter?: Observation[], objectIdsFilter?: string[]): Promise<void> {}

    abstract readonly name : string;

    abstract isInitialized : boolean;

    readonly context : Context;
}
