import { State } from "./state";

export interface StateMachine  {

    signal( method : string, params : any ) : Promise<State>;

    setCurrentState( state : State ) : Promise<void>;

    currentState() : State;

    readonly name : string;
}