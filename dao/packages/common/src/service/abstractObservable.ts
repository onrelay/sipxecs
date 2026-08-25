import { Observations, Observation } from "./observations";
import { Monitor } from "./monitor";
import { Observable } from "./observable";


export abstract class AbstractObservable implements Observable {

    async subscribe( monitor : Monitor ) : Promise<void>
    {
        //log.traceIn( "subscribe()", monitor );

        try {
            if( !this.hasObservers() ) {
                //log.debug( "subscribe()", "Not monitoring");

                this.addObserver( monitor );

                await this.monitor( monitor );

                //log.traceOut( "subscribe()", "First subscriber" );
                return;
            }

            let startObservations : Observation[] = [];
            let stopObservations : Observation[] = [];

            let monitorIds : string[] = [];
            let releaseIds : string[] = [];

            const previousMonitor = this._observers.get( monitor!.observer );

            if( previousMonitor == null ) {
                //log.debug( "subscribe()", "New subscriber" );

                if( monitor.observationFilter != null && monitor.observationFilter.length > 0 ) {
                    startObservations = monitor.observationFilter;
                }
                if( monitor.objectIdsFilter != null && monitor.objectIdsFilter.length > 0 ) {
                    monitorIds = monitor.objectIdsFilter;
                }
            }
            else {
                //log.debug( "subscribe()", "Previous subscriber:", previousMonitor );

                this.removeObserver( monitor!.observer ); // replace previous monitor

                // Find observations to stop
                if( previousMonitor.observationFilter != null && previousMonitor.observationFilter.length > 0 ) {

                    let removedObservations : Observation[] = [];

                    if( monitor.observationFilter == null || monitor.observationFilter.length === 0 ) {
                        removedObservations = previousMonitor.observationFilter;
                    }
                    else {
                        for( const previousObservation of previousMonitor.observationFilter ) {
                            if( monitor.observationFilter.indexOf( previousObservation ) < 0 ) {
                                removedObservations.push( previousObservation );
                            }
                        }
                    } 

                    //log.debug( "subscribe()", "removedObservations:", removedObservations );

                    for( const removedObservation of removedObservations ) {
                        if( !this.isMonitoringObservation( removedObservation) ) {
                            stopObservations.push( removedObservation );
                        }
                    }
                    //log.debug( "subscribe()", "stopObservations:", stopObservations );
                }

                // Find observations to start
                if( monitor.observationFilter != null && monitor.observationFilter.length > 0 ) {

                    let addedObservations : Observation[] = [];

                    if( previousMonitor.observationFilter == null || previousMonitor.observationFilter.length === 0 ) {
                        addedObservations = monitor.observationFilter;
                    }
                    else {
                        for( const observation of monitor.observationFilter ) {
                            if( previousMonitor.observationFilter.indexOf( observation ) < 0 ) {
                                addedObservations.push( observation );
                            }
                        }
                    }

                    //log.debug( "subscribe()", "addedObservations:", addedObservations );

                    for( const addedObservation of addedObservations ) {
                        if( !this.isMonitoringObservation( addedObservation) ) {
                            startObservations.push( addedObservation );
                        }
                    }
                    //log.debug( "subscribe()", "startObservations:", startObservations );
                }

                // Find objectIds to start
                if( monitor.objectIdsFilter != null && monitor.objectIdsFilter.length > 0 ) {

                    let addedMonitoringIds : string[] = [];

                    if( previousMonitor.objectIdsFilter == null || previousMonitor.objectIdsFilter.length === 0 ) {
                        addedMonitoringIds = monitor.objectIdsFilter;
                    }
                    else {
                        //log.debug( "subscribe()", "monitor.objectIds:", monitor.objectIdsFilter );

                        //log.debug( "subscribe()", "previousMonitor.objectIds:", previousMonitor.objectIdsFilter );

                        for( const objectId of monitor.objectIdsFilter ) {
                            if( previousMonitor.objectIdsFilter.indexOf( objectId ) < 0 ) {
                            addedMonitoringIds.push( objectId );
                            }
                        }
                    }
                    //log.debug( "subscribe()", "addedMonitoringIds:", addedMonitoringIds );
    
                    for( const addedMonitoringId of addedMonitoringIds ) {
                        if( !this.isMonitoringId( addedMonitoringId) ) {
                            monitorIds.push( addedMonitoringId );
                        }
                    }
                    //log.debug( "subscribe()", "monitorIds:", monitorIds );
                }

                // Find objectIds to stop
                if( previousMonitor.objectIdsFilter != null && previousMonitor.objectIdsFilter.length > 0 ) {

                    let removedMonitoringIds : string[] = [];
                    
                    if( monitor.objectIdsFilter == null || monitor.objectIdsFilter.length === 0 ) {
                        removedMonitoringIds = previousMonitor.objectIdsFilter;
                    }
                    else {
                        for( const previousMonitoringId of previousMonitor.objectIdsFilter ) {
                            if( monitor.objectIdsFilter.indexOf( previousMonitoringId ) < 0 ) {
                                removedMonitoringIds.push( previousMonitoringId );
                            }
                        }
                    }

                    //log.debug( "subscribe()", "removedMonitoringIds:", removedMonitoringIds );

                    for( const removedMonitoringId of removedMonitoringIds ) {
                        if( !this.isMonitoringId( removedMonitoringId) ) {
                            releaseIds.push( removedMonitoringId );
                        }
                    }
                    //log.debug( "subscribe()", "releaseIds:", releaseIds );
                }
            }
    
            this.addObserver( monitor ); 

            if( stopObservations.length > 0 || releaseIds.length > 0 ) {
                await this.release( stopObservations, releaseIds );
            }

            await this.monitor( monitor );

            //log.traceOut( "subscribe()", "return", result );

        } catch( error ) {
            console.warn( "subscribe()", "Error adding new monitor", monitor, error );

            throw new Error( (error as any).message ); 
        }
    }

    async unsubscribe( observer : object ) : Promise<void>
    {
        //log.traceIn( "unsubscribe()", observer );

        try{ 

            let stopObservations : Observation[] = [];

            let releaseIds : string[] = [];

            const previousMonitor = this._observers.get( observer );

            if( previousMonitor == null ) {
                //log.traceOut( "unsubscribe()", "subscriber not found" );
                return;
            }

            this.removeObserver( observer ); // replace previous monitor

            // Find observations to stop
            if( previousMonitor.observationFilter != null && previousMonitor.observationFilter.length > 0 ) {

                for( const removedObservation of previousMonitor.observationFilter ) {
                    if( !this.isMonitoringObservation( removedObservation) ) {
                        stopObservations.push( removedObservation );
                    }
                }
    
                // Find objectIds to stop
                if( previousMonitor.objectIdsFilter != null && previousMonitor.objectIdsFilter.length > 0 ) {

                    for( const removedMonitoringId of previousMonitor.objectIdsFilter ) {
                        if( !this.isMonitoringId( removedMonitoringId) ) {
                            releaseIds.push( removedMonitoringId );
                        }
                    }
                }
            }
    
            if( !this.hasObservers() || stopObservations.length > 0 || releaseIds.length > 0 ) {
                await this.release( stopObservations, releaseIds );
            }

            //log.traceOut( "unsubscribe()" );

        } catch( error ) {
            console.warn( "unsubscribe()", "Error unsubscring", observer );

            throw new Error( (error as any).message );
        }
    }
    
    async notify( observation : Observation, objectId? : string, object? : object ) : Promise<void> { 

        //log.traceIn( "notify()", Observation[observation], objectId ); 

        try {
            if( this._observers.size === 0 ) {
                //log.traceOut( "notify()", "no observers" );
                return;
            }

            for( const monitor of this._observers.values() ) {
            
                //log.debug( "notify()", monitor);

                if( monitor.observationFilter != null && 
                    monitor.observationFilter.length > 0 &&
                    monitor.observationFilter.indexOf( observation ) < 0 ) {
                        //log.debug( "notify()","skip, observation filtered");
                }
                else if( observation !== Observations.Create && 
                    objectId != null &&
                    monitor.objectIdsFilter != null && 
                    monitor.objectIdsFilter.length > 0 &&
                    monitor.objectIdsFilter.indexOf( objectId ) < 0 ) {
                        //log.debug( "notify()","skip, objectId filtered");
                }
                else {
                    if( monitor.onNotify != null ) {
                        await monitor.onNotify( this, observation, objectId, object ); 
                    }
                }
            }

            //log.traceOut( "notify()" );

        } catch( error ) {

            console.warn( "notify()", "Error notifying observers", error );

            throw new Error( (error as any).message );
        }
    }

    
    protected isMonitoringObservation( observation : Observation ) {

        //log.traceIn( "isMonitoringObservation()", Observation[observation] );

        const result : boolean = this._observationFilter == null || 
            this._observationFilter.indexOf( observation ) > -1;

        //log.traceOut( "isMonitoringObservation()", result );
        return result;
    }

    protected updateObservationsFilter() : void {

        //log.traceIn( "updateObservationsFilter()" );

        this._observationFilter = [];

        for( const monitor of this._observers.values()  ) {

            if( monitor.observationFilter == null || monitor.observationFilter.length === 0 ) {
                this._observationFilter = [];
                //log.traceOut( "updateObservationsFilter()", "no filter" );
                return;
            }
            else {

                monitor.observationFilter.forEach( (observation: Observation) => {

                    if( this._observationFilter.indexOf( observation ) < 0 ) {

                        this._observationFilter.push( observation );
                    }
                } )
            }
        }
        
        //log.traceOut( "updateMonitoringObservationsFilter()", "observationFilter:", this._observationFilter  );
    }

    protected isMonitoringId( objectId : string ) : boolean {

       // log.traceIn( "isMonitoringId()", objectId );

        const result : boolean = this._objectIdsFilter == null || 
            this._objectIdsFilter.indexOf( objectId ) > -1;
        
        //log.traceOut( "isMonitoringId()", result );
        return result;
    }

    private updateMonitoringIdsFilter() : void {

        //log.traceIn( "updateMonitoringIdsFilter()" );

        this._objectIdsFilter = [];

        for( const monitor of this._observers.values()  ) {

            if( monitor.objectIdsFilter == null || monitor.objectIdsFilter.length === 0 ) {
                this._objectIdsFilter = [];
                //log.traceOut( "updateMonitoringIdsFilter()", "no filter" );
                return;
            }
            else {

                monitor.objectIdsFilter.forEach( ( objectId : string )=> {

                    if( this._objectIdsFilter.indexOf( objectId ) < 0 ) {

                        this._objectIdsFilter.push( objectId );
                    }
                } )
            }
        }
        
        //log.traceOut( "objectIdsFilter()", "objectIdsFilter:", this._objectIdsFilter );
    }

    private addObserver( monitor : Monitor ) : void {

        //log.traceIn( "addObserver()", monitor );

        this._observers.set( monitor.observer, monitor );

        this.updateObservationsFilter();
        
        this.updateMonitoringIdsFilter();

       // log.traceOut( "addObserver()" );
    }

    private removeObserver( observer : object ) : void {

        //log.traceIn( "removeObserver()", observer );

        if( this._observers.delete(observer) ) { 

            this.updateObservationsFilter();
            this.updateMonitoringIdsFilter();
        }
       // log.traceOut( "removeObserver()" );
    }

    hasObservers() : boolean {
        return this._observers.size > 0;
    }

    isObserver( observer : Object ) : boolean {
        return this._observers.has( observer );
    }

    observer( observer : object ) : Monitor | undefined {
        return this._observers.get(observer);
    }

    protected observers() : Map<object,Monitor> {
        return this._observers;
    }

    protected observationFilter() : Observation[] {
        return this._observationFilter;
    }

    protected idsFilter() : string[] {
        return this._objectIdsFilter;
    }


    protected isMonitoring() : boolean {
        return this._observers.size > 0;
    }

    protected getMonitors() : Monitor[] {
        return Array.from( this._observers.values() ); 
    }

    protected abstract monitor( newMonitor : Monitor, observationFilter? : Observation[], objectIdsFilter? : string[] ) : Promise<void>; 

    protected abstract release( observationFilter? : Observation[], objectIdsFilter? : string[] ) : Promise<void>; 

    private _observers : Map<object,Monitor> = new Map<object,Monitor>();

    private _observationFilter : Observation[] = [];

    private _objectIdsFilter : string[] = [];

}