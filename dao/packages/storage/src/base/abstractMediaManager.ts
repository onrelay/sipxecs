import { AbstractObservable, Monitor, Observation } from "@dao/common";

import { MediaManager } from "../spec/mediaManager";
import { StorageMedia } from "../types/storageMedia";


export abstract class AbstractMediaManager extends AbstractObservable implements MediaManager {

    constructor() {

        super();
        //log.traceIn( "constructor()");

        try {

            //log.traceOut( "constructor()" );
            
        } catch( error ) {

            console.warn( "Error initializing media manager", error );
            
            throw new Error( (error as any).message );
        }
    }

    async init(): Promise<void> {

        //log.traceIn( "constructor()");

        try {

            //log.traceOut( "constructor()" );

        } catch (error) {

            console.warn("Error initializing image manager", error);

            throw new Error( (error as any).message );
        }
    }

    protected async monitor(newMonitor: Monitor, observationFilter?: Observation[], objectIdsFilter?: string[]): Promise<void> {}

    protected async release(observationFilter?: Observation[], objectIdsFilter?: string[]): Promise<void> {}

    abstract upload( media : StorageMedia, monitor? : Monitor ) : Promise<void>;

    abstract downloadUrl( media: StorageMedia ): Promise<string | undefined>;

    abstract download( media : StorageMedia, monitor? : Monitor ) : Promise<void>;

    abstract delete( path : string ) : Promise<void>;

}