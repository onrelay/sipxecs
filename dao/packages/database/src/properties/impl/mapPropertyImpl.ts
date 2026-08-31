import { DatabaseRecord } from "../../core/types/databaseRecord";
import { GenericDatabaseDocument } from "../../core/impl/genericDatabaseDocument";
import { AbstractDatabaseProperty } from "../../core/base/abstractDatabaseProperty";
import { log } from "../../core/base/abstractDatabaseService";
import { MapProperty } from "../spec/mapProperty";

export class MapPropertyImpl<Data extends Object> extends AbstractDatabaseProperty<Map<string,Data>> implements MapProperty<Data> {

    value() : Map<string,Data> | undefined {

        this.decryptData();

        return this._map;
    }

    setValue( value : Map<string,Data> | undefined ): void {

        this.decryptData();

        const oldValue = this._map;

        if( this.onChange( oldValue, value ) ) {

            this._map = value;

            this.onChanged( oldValue, value );
        }
    }

    setEntry( key: string, entry: Data ) : void {

        this.decryptData();

        const oldValue = this._map;

        const newMap = new Map<string,Data>( this._map );

        newMap.set( key, entry );

        if( this.onChange( oldValue, newMap ) ) {

            this._map = newMap;

            this.onChanged( oldValue, newMap );
        }
    }

    removeEntry( key: string ) : boolean {

        this.decryptData();

        const oldMap = this._map;

        const newMap = new Map<string,Data>( this._map );  

        if( !newMap.delete( key ) ) {

            return false;
        }

        if( this.onChange( oldMap, newMap ) ) {

            this._map = newMap;

            this.onChanged( oldMap, newMap );

            return true;
        }

        return false;
 
    }

    fromRecord( propertyData: DatabaseRecord): void {

        if( this.isEncryptedData( propertyData[this.key()] ) ) { 

            this.setEncryptedData( propertyData[this.key()] );
        }
        else {

            const map = propertyData[this.key()] as any;

            if( map == null ) {
                this._map = undefined;
                return;
            }
    
            this._map = new Map<string,Data>();
    
            Object.keys(map).forEach( key => {
    
                this._map!.set( key, JSON.parse( map[key] ) as Data );
            });
        }
    }

    async toRecord( propertyData: DatabaseRecord, force? : boolean ) : Promise<void> {

        if( !!force ) {
            this.decryptData();
        }
        
        if( this.encryptedData() != null ) {
            
            propertyData[this.key()] = this.encryptedData();
        }
        else if( this._map != null && this._map.size > 0 ) {

            const map = {} as any;

            this._map.forEach( (value,key) => {

                map[key] = JSON.stringify( value )
            });

            let data;

            if( this.encrypted() ) {
                data = this.encryptData( map )
            }
            else {
                data = map;
            }

            propertyData[this.key()] = data;
        }
    }

    compareTo( other : MapProperty<Data> ) : number {

        return this.compareValue( other.value() );
    }

    compareValue( otherMap : Map<string,Data> | undefined ) : number {

        const thisMap = this.value();

        if( thisMap == null && otherMap == null ) {
            return 0;
        }

        if( thisMap != null && otherMap == null ) {
            return 1;
        }

        if( thisMap == null && otherMap != null ) {
            return -1;
        }

        if( thisMap!.size > otherMap!.size ) {
            return 1;
        }

        if( thisMap!.size < otherMap!.size ) {
            return -1;
        }

        for( const keyValuePair of otherMap! ) {

            const key = keyValuePair[0];
            const otherValue = keyValuePair[1];

            const thisValue = thisMap!.get( key );

            if( thisValue == null ) {
                return 1;
            }

            const compare = JSON.stringify( thisValue ).localeCompare( JSON.stringify( otherValue ) )

            if( compare !== 0 ) {
                return compare;
            }
        }

        return 0;
    }

    includes( other : MapProperty<Data>, matchAny? : boolean ) : boolean {
        return this.includesValue( other.value(), matchAny );
    }

    includesValue( map : Map<string,Data> | undefined, matchAny? : boolean ) : boolean {

        const thisMap = this.value();

        if( thisMap == null && map == null ) {
            return false;
        }

        if( thisMap != null && map == null ) {
            return false;
        }

        if( thisMap == null && map != null ) {
            return false;
        }

        if( thisMap!.size < map!.size && !matchAny ) {
            return false;
        }

        for( const keyValuePair of map! ) {

            const key = keyValuePair[0];
            const otherValue = keyValuePair[1];

            const thisValue = thisMap!.get( key );

            if( thisValue == null ) {
                return false;
            }

            const compare = JSON.stringify( thisValue ).localeCompare( JSON.stringify( otherValue ) ) === 0;

            if( compare && !!matchAny ) {
                return true;
            }

            if( !compare && !matchAny ) {
                return false;
            }
        }
        return !matchAny;
    }

    onChange( oldValue : Map<string,Data> | undefined, newValue : Map<string,Data> | undefined ) : boolean {

        if( this.minEntries != null && newValue != null && newValue.size < this.minEntries ) {

            this.error = new Error( "propertyValueRejected" );

            if( oldValue == null || newValue.size >= oldValue.size ) {
                return true;
            }
            else {
                return false;
            }
        }

        if( this.maxEntries != null && newValue != null && newValue.size > this.maxEntries ) {
            
            this.error = new Error( "propertyValueRejected" );
            return false;
        }

        return super.onChange( oldValue, newValue );
    }

    onChanged( oldValue : Map<string,Data> | undefined, newValue : Map<string,Data> | undefined ) : void {

        try {
            //log.traceIn( "onChanged()", {oldValue}, {newValue} );

           this.setPreviousValues( this.previousValues() != null ? 
                this.previousValues().concat(newValue) : [newValue] );

            if( this.minEntries != null && newValue != null && newValue.size >= this.minEntries ) {

                delete this.error;
            }
 
            (this.parent as GenericDatabaseDocument).onChanged( this, oldValue, newValue ); // Note this is async, so will not wait for processing / block

            //log.traceOut( "onChanged()", {oldValue}, {newValue} ); 
            
        } catch( error ) {

            log.warn( "onChanged()", "Error checking change", error );
            
            throw new Error("Error checking change: " + (error as any).message );
        }
    }

    minEntries? : number;

    maxEntries? : number;

    protected _map : Map<string,Data> | undefined;

}