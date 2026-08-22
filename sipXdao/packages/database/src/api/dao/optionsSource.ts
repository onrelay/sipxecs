import { OptionsSource } from "../types/optionsSource";


export abstract class AbstractOptionsSource implements OptionsSource {

    abstract options() : Promise<Map<string,any>| undefined>; 
}
