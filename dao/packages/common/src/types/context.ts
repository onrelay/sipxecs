import { Environment } from "../defs/environment";
import { Platform } from "../defs/platform";
import { Target } from "../defs/target";
import { Translator } from "../spec/translator";
import { Configuration } from "./configuration";

export type Context = {
    
    readonly environment : Environment;

    readonly platform : Platform;

    readonly target : Target;

    readonly configuration: Configuration;

    readonly translator: Translator;
}