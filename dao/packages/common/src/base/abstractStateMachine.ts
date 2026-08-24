
import { State } from "../spec/state";
import { StateMachine } from "../spec/stateMachine";
import { log } from "./abstractApplication";

export abstract class AbstractStateMachine implements StateMachine {

    constructor( params: { 
        name : string, 
        initialState : State } ) {

        log.traceInOut( "constructor()", params.name, params.initialState );

        this.name = params.name;

        this._currentState = params.initialState;
    }

    async signal( method : string, params : any ) : Promise<State> {

        log.traceIn( "signal()", method, params );

        try {

            if( !Object.keys( this.currentState() ).includes( method ) ) {

                throw new Error( "Method " + method + " not found on state " + this.currentState().name )
            }

            const nextState = (this.currentState() as any)[method]( params ) as State;

            this.setCurrentState( nextState! );

            log.traceOut( "signal()", this.currentState().name );
            return this.currentState();

        } catch (error) {

            log.warn("Error signaling state machine", error);

            throw new Error( (error as any).message );
        }

    }

    
    currentState() : State {

        return this._currentState;
    }

   async setCurrentState( state : State ) : Promise<void> {

        log.traceIn( "setCurrentState()", state );

        try {

            this._currentState = state;

        } catch (error) {

            log.warn("Error setting current state", error);

            throw new Error( (error as any).message );
        }
    }

    readonly name : string;

    private _currentState : State;
}
