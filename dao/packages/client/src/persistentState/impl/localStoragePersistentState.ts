import { AbstractPersistentState } from "../base/abstractPersistentState";



export default class LocalStoragePersistentState extends AbstractPersistentState {

  constructor() {

    super();

    this._localStorage = localStorage;
  }

  protected persistentStateData( persistentStateKey : string ) : any | undefined {

    try {
    
      if( persistentStateKey == null || persistentStateKey.length === 0 ) {
        throw new Error( "No key" );
      }

      const jsonPersistentStateData = this._localStorage.getItem( persistentStateKey );

      if( jsonPersistentStateData == null) {
        //log.traceOut( "property()", "no persistent state" );
        return undefined;
      }

      const persistentStateData = JSON.parse( jsonPersistentStateData );

      //log.traceOut( "property()", value );
      return persistentStateData;

    } catch( error ) {
      console.warn( "property()", error );

      //throw new Error( "Error getting property"); 
      return undefined;
    }
  }

  protected setPersistentStateData( persistentStateKey : string, persistentStateData : any ) : void {

    //log.traceIn( "setProperty()", key, value );

    try {

      if( persistentStateKey == null || persistentStateKey.length === 0 ) {
        throw new Error( "No key" );
      }

      this._localStorage.setItem( persistentStateKey, JSON.stringify( persistentStateData ) );       

      //log.traceOut( "setProperty()" );

    } catch( error ) {
      console.warn( "setProperty()", error );

      throw new Error( (error as any).message );
    }
  }

  private readonly _localStorage : Storage;

}

