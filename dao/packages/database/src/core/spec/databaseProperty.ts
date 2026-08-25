import { Language, Observable } from "@dao/common";
import { PropertyType } from "../defs/propertyType";
import { DatabaseDocument } from "./databaseDocument";
import { DatabaseObject } from "./databaseObject";
import { DatabaseAccess } from "../impl/databaseAccess";

export interface DatabaseProperty<Value> extends Observable {

    readonly type : PropertyType,

    readonly parent : DatabaseObject,

    required : boolean,

    encrypt : boolean,

    trackChanges: boolean,

    error? : Error,

    prompt? : string; 

    help? : string;

    parentDocument() : DatabaseDocument; 

    key() : string,

    value( ignoreDefault? : boolean ) : Value | undefined,

    setValue( value : Value | undefined ) : void,

    defaultValue() : Value | undefined,

    setDefaultValue( defaultValue : Value | undefined ) : void,

    isChanged() : boolean,

    lastChange() : Value | undefined,

    onChange( oldValue : Value | undefined, newValue : Value | undefined ) : boolean,

    onChanged( oldValue : Value | undefined, newValue : Value | undefined ) : void,

    undoLastChange() : void,

    clearChanges() : void,

    databaseAccess() : DatabaseAccess,

    copyValueFrom( other : DatabaseProperty<Value> ) : boolean; 

    copyStatesFrom( other : DatabaseProperty<Value>  ) : void;

    compareTo( other : DatabaseProperty<Value>, translator? : ( value? : string ) => string | undefined ) : number,

    compareValue( value : Value | undefined, translator? : ( value? : string ) => string | undefined ) : number,

    includes( other : DatabaseProperty<Value>, matchAny? : boolean, translator? : ( value?: string ) => string | undefined ) : boolean,

    includesValue( value : Value | undefined, matchAny? : boolean, translator? : ( value? : string ) => string | undefined ) : boolean,

    validate() : Error | undefined;

    fromRecord( documentRecord : Record<string, any> ) : void,

    toRecord( documentRecord : Record<string, any>, force? : boolean ) : Promise<void>,

    encrypted() : boolean
}

 