import { StateMachine } from "./stateMachine";

export interface State  {
    
    readonly stateMachine : StateMachine;

    readonly name : string;
}