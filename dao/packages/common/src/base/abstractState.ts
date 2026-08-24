
import { State } from "../spec/state";
import { StateMachine } from "../spec/stateMachine";
import { log } from "./abstractApplication";

export abstract class AbstractState implements State {

    constructor( params: { 
        name : string,
        stateMachine : StateMachine } )  {

        log.traceInOut( "constructor()", params.name, params.stateMachine.name );

        this.name = params.name;

        this.stateMachine = params.stateMachine;
    }

    readonly name : string;

    readonly stateMachine : StateMachine;
}
