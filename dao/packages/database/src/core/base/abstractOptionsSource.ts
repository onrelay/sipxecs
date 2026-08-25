import { OptionsSource } from "../spec/optionsSource";


export abstract class AbstractOptionsSource implements OptionsSource {

    abstract options() : Promise<Map<string,any>| undefined>; 
}
