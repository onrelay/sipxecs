import { Monitor } from "./monitor";
import { Observation } from "./observations";

export interface Observable {

    subscribe( monitor : Monitor ) : Promise<void>

    unsubscribe( observer : object ) : Promise<void>,

    notify( observation : Observation, objectId : string | null | undefined, object : object | null | undefined  ) : Promise<void>,

    hasObservers() : boolean,

    isObserver( observer : Object ) : boolean,

    observer( observer : Object ) : Monitor | undefined

}