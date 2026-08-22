import {
    AbstractObservable,
    Environment,
    Monitor,
    Observation,
    Platform,
    Target,
} from "@sipxdao/common";
import { MediaManager } from "../api/mediaManager";
import { StorageService } from "../api/storageService";

export class StorageServiceImpl extends AbstractObservable implements StorageService {

    constructor( target : Target, mediaManager : MediaManager ) {

        super();

        //log.traceInOut("constructor()", target ); 

        try {

            this.target = target;

            this.mediaManager = mediaManager;

        } catch (error) {
            console.warn("constructor()", "Error constructing storage service", error);

            throw new Error( (error as any).message );
        }
    }

    readonly name = "storage";

    async init() : Promise<void> {

        try {

            this.mediaManager.init();
            this.isInitialized = true;


        } catch (error) {
            console.warn("init()", "Error initializing storage service", error);

            throw new Error( (error as any).message );
        }
    }


    protected async monitor( newMonitor : Monitor ): Promise<void> {}

    protected async release(observationFilter?: Observation[], objectIdsFilter?: string[]): Promise<void> {}

    readonly target : Target;

    readonly application = "storage";

    readonly environment: Environment = "Server";

    readonly platform: Platform = "Mongo";

    isInitialized = false;

    readonly mediaManager : MediaManager; 

}
