import { DatabaseRecord } from "../types/databaseRecord";
import { AbstractObservable, Language, Monitor, Observation, Observations } from "@dao/common";
import { DatabaseProperty } from "../spec/databaseProperty";
import { DatabaseObject } from "../spec/databaseObject";
import { PropertyType } from "../defs/propertyType";
import { DatabaseDocument } from "../spec/databaseDocument";
import { DatabaseAccess } from "../impl/databaseAccess";
import { log } from "./abstractDatabaseService";
import { databaseServiceFactory, DatabaseServiceFactory } from "../impl/databaseServiceFactory";
import { AbstractDatabaseObject } from "./abstractDatabaseObject";
import { securityServiceFactory } from "@dao/security";

export abstract class AbstractDatabaseProperty<Value> extends AbstractObservable implements DatabaseProperty<Value> {

    constructor( parent : DatabaseObject, type : PropertyType ) {
        
        super();

        try {
            //log.traceIn( "constructor()", type, parent ); 
        
            this.type = type;

            this.parent = parent;

            //log.traceOut( "constructor()");

        } catch( error ) { 

            log.warn( "constructor()", "Error initializing property", error );
            
            throw new Error( (error as any).message );
        }
    }

    parentDocument() : DatabaseDocument {

        let parent = this.parent;

        while( parent != null && parent.parent != null ) {

            parent = parent.parent;
        }

        if( parent == null ) {
            throw new Error( "Property has no parent document ");
        }

        return parent as DatabaseDocument; 
    }

    key() : string {

        if( this._key != null ) {
            //log.traceInOut( "key()", this._key );
            return this._key;
        }
        //log.traceIn( "key()");

        const parentProperties = Object.values( this.parent );

        let key : string | undefined;

        Object.keys( this.parent ).forEach( (propertyKey, propertyKeyIndex ) => {

            if( parentProperties[propertyKeyIndex] === this ) {
                key = propertyKey;
            };
        });
        if( key == null ) {
            throw new Error( "Key not found in " + Object.keys( this.parent ) );
        }

        this._key = key!;

        //log.traceOut( "key()", this._key );
        return this._key
    }

    isChanged() : boolean {
        return this._previousValues.length > 0;
    }

    lastChange() : Value | undefined {
        return this._previousValues.length === 0 ? 
            undefined :
            this._previousValues[this._previousValues.length - 1];
    }

    onChange( oldValue : Value | undefined, newValue : Value | undefined ) : boolean {

        try {
            //log.traceIn( "onChange()", {oldValue}, {newValue} );

            if( this.compareValue( newValue ) === 0  ) { 
                
                //log.traceOut( "onChange()", {oldValue}, {newValue}, "no change", false );
                return false;
            }

            if( !(this.parent as AbstractDatabaseObject).onChange( this, oldValue, newValue ) ) { 
                
                this.error = new Error( "propertyValueRejected" );

                //log.traceOut( "onChange()", {oldValue}, {newValue}, "rejected by parent", false );
                return false;
            }

            //log.traceOut( "onChange()", {oldValue}, {newValue}, true );
            return true;
            
        } catch( error ) {

            log.warn( "onChange()", "Error checking change", error );
            
            throw new Error("Error checking change: " + (error as any).message );
        }
    }

    onChanged( oldValue : Value | undefined, newValue : Value | undefined ) : void {

        try {
            //log.traceIn( "onChanged()", {oldValue}, {newValue} );

            delete this.error;

            if( !this.trackChanges ) {
                //log.traceOut( "onChanged()", "not tracking", {oldValue}, {newValue} ); 
                return;
            }

            this._previousValues.push( oldValue );

            (this.parent as AbstractDatabaseObject).onChanged( this, oldValue, newValue ); 

            //log.traceOut( "onChanged()", {oldValue}, {newValue} ); 
            
        } catch( error ) {

            log.warn( "onChanged()", "Error checking change", error );
            
            throw new Error("Error checking change: " + (error as any).message );
        }
    }

    undoLastChange() : void {

        if( this._previousValues.length > 0 ) {

            const previousValue = this._previousValues.pop();

            const previousValues = Object.assign( [], this._previousValues );

            this.setValue( previousValue );

            this.clearChanges();
            Object.assign( this._previousValues, previousValues );
        }
    }

    clearChanges() : void {
        this._previousValues.length = 0;

        (this.parent as AbstractDatabaseObject).clearChange( this ); 
    }

    protected previousValues() : Array<Value | undefined> {
        return Object.assign( [], this._previousValues );
    }

    protected setPreviousValues( previousValues : Array<Value | undefined> ) {
        this._previousValues.length = 0;
        Object.assign( this._previousValues, previousValues );
    }

    databaseAccess() : DatabaseAccess {
        let access = this.parent.databaseAccess();

        return access;
    }

    validate() : Error | undefined {
        
        if( this.required && this.value() == null ) {
            const error = new Error( "propertyValueMissing" );
            return error;
        }

        return undefined;
    }

    encrypted() : boolean {

        try {
            //log.traceIn( "encrypted()" );

            const clientEncryption = this.parent.ownerCollection()!.databaseManager.clientEncryption;

            const encrypted = 
                this.encrypt && clientEncryption && this.parent.ownerCollection()!.encrypted

            //log.traceOut( "encrypted()", {encrypted});
            return encrypted;

        } catch( error ) {

            log.warn( "encrypted()", "Error reading encrypted status", error );
            
            throw new Error("Error reading encryption status: " + (error as any).message );
        }
    }

    copyValueFrom( other : DatabaseProperty<Value> ) : boolean {

        if( this === other ) {
            return false;
        }

        let changed = false;

        if( (other as AbstractDatabaseProperty<Value>)._encryptedData != null ) {

            if( this._encryptedData !== (other as AbstractDatabaseProperty<Value>)._encryptedData ) {

                this._encryptedData = (other as AbstractDatabaseProperty<Value>)._encryptedData;
                changed = true;
            }
        }
        else {
            this._encryptedData = undefined;

            if( this.compareTo( other ) !== 0 ) {
                this.setValue( other.value( true ) );
                changed = true;
            }
        }
        return changed;  
    }

    copyStatesFrom( other : DatabaseProperty<Value>  ) : void {

        if( this === other ) {
            return;
        }

        this.setPreviousValues( (other as AbstractDatabaseProperty<Value>)._previousValues );

        this.encrypt = other.encrypt;
        this.required = other.required;
        this.error = other.error;
    }


    protected isEncryptedData( data : any ) : boolean {

        return securityServiceFactory!.get().symmetricCipher.isEncrypted( data );
    }

    protected encryptData( data : any ) : string | undefined {

        try {
            if (this._encryptedData != null) {
                return this._encryptedData;
            }

            return securityServiceFactory!.get().symmetricCipher.encrypt(data);

        } catch (error) {

            log.warn("monitor()", "Error encrypting data ", error);

            return undefined;
        }
    }

    protected decryptData(): void {

        try {

            if (this._encryptedData == null) {
                return;
            }

            const data = {} as any;

            data[this.key()] = securityServiceFactory!.get().symmetricCipher.decrypt(this._encryptedData);

            this.fromRecord(data);

            delete this._encryptedData;

        } catch (error) {

            log.warn("monitor()", "Error decrypting data ", error);

            throw new Error( (error as any).message );

        }

    }

    protected setEncryptedData( encryptedData : any ) {
        this._encryptedData = encryptedData;
    }


    protected encryptedData() {
        return this._encryptedData;
    }

    async onCreate() : Promise<void> {

        try {
            if( this.encrypted() ) {
                await securityServiceFactory!.get().updateCurrentKeys();
            }

        } catch (error) {
            log.warn("onRead()", "Error updating keys", error);
        }
    }

    async onUpdate() : Promise<void> {

        try {
            if( this.encrypted() ) {
                await securityServiceFactory!.get().updateCurrentKeys();
            }

        } catch (error) {
            log.warn("onRead()", "Error updating keys", error);
        }
    }

    async onDelete() : Promise<void> {

        try {
            if( this.encrypted() ) {
                await securityServiceFactory!.get().updateCurrentKeys();
            }

        } catch (error) {
            log.warn("onRead()", "Error updating keys", error);
        }
    }

    async onRead() : Promise<void> {

        try {
            if( this.encrypted() ) {
                await securityServiceFactory!.get().updateCurrentKeys();
            }

        } catch (error) {
            log.warn("onRead()", "Error updating keys", error);
        }
    }
    
    async onCreated() : Promise<void> {
        this.clearChanges();    
    }
    
    async onUpdated() : Promise<void> {
        this.clearChanges();
    }

    defaultValue( ) : Value | undefined {
        throw new Error( "Override");
    }

    setDefaultValue( defaultValue : Value | undefined) : void {
        throw new Error( "Override");
    }


    protected async monitor( newMonitor : Monitor ) : Promise<void> {
        
        try {
            if (newMonitor.onNotify == null) {
                return;
            }

            await newMonitor.onNotify(this,
                Observations.Create as Observation,
                this.key(),
                this.value() );

        } catch( error ) {
            log.warn( "monitor()", "Error monitoring property", error );
            
            throw new Error( (error as any).message );
        }
    }

    protected async release( observationFilter? : Observation[], objectIdsFilter? : string[] ) : Promise<void> {} 

    
    async onDeleted() : Promise<void> {}
    
    abstract value( ignoreDefault? : boolean ) : any | undefined;

    abstract setValue( value : any | undefined) : void;

    abstract compareTo( other : DatabaseProperty<Value>, translator? : ( value? : string ) => string | undefined ) : number;

    abstract compareValue( value : any | undefined, translator? : ( value? : string ) => string | undefined ) : number;

    abstract includes( other : DatabaseProperty<Value>, matchAny? : boolean, translator? : ( value? : string ) => string | undefined ) : boolean;

    abstract includesValue( value : any | undefined, matchAny? : boolean, translator? : ( value? : string ) => string | undefined ) : boolean;

    abstract fromRecord( documentData : DatabaseRecord ) : void; 

    abstract toRecord( documentData : DatabaseRecord, force? : boolean ) : Promise<void>;

    readonly type : PropertyType;

    readonly parent : DatabaseObject;

    required : boolean = false;

    encrypt : boolean = true;

    trackChanges: boolean = true;

    error? : Error;

    prompt? : string; 

    help? : string;

    private readonly _previousValues : Array<Value | undefined> = [];

    private _key : string | undefined;

    private _encryptedData? : string;

    private _keyTranslations : Map<Language,string> | undefined;

}
 