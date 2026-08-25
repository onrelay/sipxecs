import { Observable } from "./observable";

export interface Service extends Observable {

    init() : Promise<void>;

    readonly name : string;

    isInitialized : boolean;
}