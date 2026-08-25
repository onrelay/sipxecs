import { PersistentState } from "../spec/persistentState";
import { authenticationServiceFactory } from "@dao/authentication";
import { log } from "@dao/database";

const PersistentStateKey = "persistentState";

export abstract class AbstractPersistentState implements PersistentState {

    property(key: string): any | undefined {

        //log.traceIn( "property()", key );

        try {

            if (key == null || key.length === 0) {
                throw new Error( "empty key" );
            }

            const cachedValue = this._cache.get(key);

            if (cachedValue != null) {
                //log.traceOut( "property()", "from cache", cachedValue );
                return cachedValue
            }

            const persistentStateData = this.persistentStateData( this.persistentStateKey( key ) );

            if (persistentStateData == null || persistentStateData === "undefined") {
                //log.traceOut( "property()", "no persistent state" );
                return undefined;
            }

            let data = persistentStateData;

            const subkeys = key.split(".");

            const last = subkeys.length - 1;

            for (let i = 0; i < last; i++) {

                data = data[subkeys[i]];

                if (data == null) {
                    //log.traceOut( "property()", "key not found" );
                    return undefined;
                }
            }

            const value = data[subkeys[last]];

            this._cache.set(key, value);

            //log.traceOut( "property()", value );
            return value;

        } catch (error) {
            log.warn("property()", error);

            //throw new Error( (error as any).message );
            return undefined;
        }
    }

    setProperty(key: string, value: any | undefined): void {

        //log.traceIn( "setProperty()", key, value );

        try {

            if (localStorage == null) {
                log.warn("setProperty()", "No cookies");
                //log.traceOut( "setProperty()", undefined );
                return;
            }

            const cachedValue = this._cache.get(key);

            if (cachedValue == null && value == null) {
                //log.traceOut( "property()", "no change" );
                return;
            }

            if (cachedValue != null && value != null) {

                if (typeof value === 'object') {

                    if (JSON.stringify(cachedValue) === JSON.stringify(value)) {
                        //log.traceOut( "property()", "no change to object" );
                        return;
                    }
                }
                else {
                    if (cachedValue === value) {
                        //log.traceOut( "property()", "no change to primitive value" );
                        return;
                    }
                }
            }

            if (value == null) {
                this._cache.delete(key);
            }
            else {
                this._cache.set(key, value);
            }

            let persistentStateData = this.persistentStateData(this.persistentStateKey( key ));

            //log.debug( "setProperty()", "persistentStateData", persistentStateData );

            if (persistentStateData == null || persistentStateData === "undefined") {

                persistentStateData = {};
            }

            let data = persistentStateData;

            const subkeys = key.split(".");

            const last = subkeys.length - 1;

            for (let i = 0; i < last; i++) {

                if (data[subkeys[i]] == null) {
                    data[subkeys[i]] = {};
                }
                data = data[subkeys[i]];
            }

            if (value == null) {

                delete data[subkeys[last]];
            }
            else {
                data[subkeys[last]] = value;
            }

            this.setPersistentStateData(this.persistentStateKey( key ), persistentStateData );

            //log.traceOut( "setProperty()", "changed" );

        } catch (error) {
            log.warn("setProperty()", error);

            //throw new Error( (error as any).message );
        }
    }

    clearProperty(key: string): void {

        this.setProperty(key, undefined);
    }


    private persistentStateKey( key? : string ): string {

        let persistentStateKey = PersistentStateKey;

        const authenticatedUser = authenticationServiceFactory?.get()?.authenticatedEntity;

        if (authenticatedUser != null) {
            persistentStateKey += "." + authenticatedUser.authenticationId;
        }

        if( key != null ) {
            persistentStateKey +=  "." + key;
        }

        return persistentStateKey;
    }

    protected abstract persistentStateData( persistentStateKey : string ) : any | undefined;

    protected abstract setPersistentStateData( persistentStateKey : string, persistentStateData : any ) : void;

    private readonly _cache = new Map<string, any>();

}