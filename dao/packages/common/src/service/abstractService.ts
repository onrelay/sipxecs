import { Monitor } from "./monitor";
import { Observation } from "./observations";
import { Service } from "./service";
import { AbstractObservable } from "./abstractObservable";


export abstract class AbstractService extends AbstractObservable implements Service {

    abstract init() : Promise<void>;

    protected async monitor( newMonitor : Monitor ): Promise<void> {}
 
    protected async release(observationFilter?: Observation[], objectIdsFilter?: string[]): Promise<void> {}

    abstract readonly name : string;

    abstract isInitialized : boolean;

}
