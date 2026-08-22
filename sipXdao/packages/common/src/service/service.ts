import { Environment } from "./environment";
import { Observable } from "./observable";
import { Platform } from "./platform";
import { Target } from "./target";


export interface Service extends Observable {

    init() : Promise<void>;

    readonly name : string;

    readonly application : string,

    readonly environment : Environment;

    readonly platform : Platform;

    readonly target : Target;

    isInitialized : boolean;

}