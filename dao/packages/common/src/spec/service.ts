import { Context } from "../types/context";
import { Observable } from "./observable";

export interface Service extends Observable {

    init() : Promise<void>;

    isInitialized : boolean;

    readonly name : string;

    readonly context : Context;
}