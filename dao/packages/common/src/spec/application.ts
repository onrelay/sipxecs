import { Context } from "../types/context";

export interface Application  {

    init() : Promise<void>;

    isInitialized : boolean;

    readonly name : string;

    readonly context : Context;
}