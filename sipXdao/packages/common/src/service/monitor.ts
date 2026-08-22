import { Observable } from "./observable";
import { Observation } from "./observations";

export type Monitor = {
    observer : object,

    onNotify? : (observable : Observable, 
        observation : Observation, 
        objectId? : string , 
        object? : any ) => Promise<void>,

    observationFilter? : Observation[],
    
    objectIdsFilter? : string[]
}
