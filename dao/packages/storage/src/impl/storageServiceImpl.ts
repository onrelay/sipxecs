import {
    AbstractObservable,
    Context,
    Environment,
    Monitor,
    Observation,
    Platform,
    Target,
} from "@dao/common";
import { MediaManager } from "../spec/mediaManager";
import { StorageService } from "../spec/storageService";

export class StorageServiceImpl extends AbstractObservable implements StorageService {

    constructor( context : Context, mediaManager : MediaManager ) {

        super();

        //log.traceInOut("constructor()", target ); 

        try {

            this.context = context;

            this.mediaManager = mediaManager;

        } catch (error) {
            console.warn("constructor()", "Error constructing storage service", error);

            throw new Error( (error as any).message );
        }
    }

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

    isInitialized = false;

    readonly name = "storage";

    readonly context : Context;

    readonly mediaManager : MediaManager; 

}
