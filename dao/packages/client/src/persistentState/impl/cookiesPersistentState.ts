import { Cookies } from 'react-cookie';
import { AbstractPersistentState } from '../base/abstractPersistentState';


export default class CookiesPersistentState extends AbstractPersistentState {

  setCookies( cookies : Cookies ) {
    this._cookies = cookies;
  }

  cookies() : Cookies | undefined {

    return this._cookies;
  }


  protected persistentStateData( persistentStateKey : string ) : any | undefined {

    try {
    
      if( this._cookies == null ) {
        throw new Error( "No cookies" );
      }

      if( persistentStateKey == null || persistentStateKey.length === 0 ) {
        throw new Error( "No key" );
      }

      const persistentStateData = this._cookies.get( persistentStateKey );

      if( persistentStateData == null || persistentStateData === "undefined" ) {
        //log.traceOut( "property()", "no persistent state" );
        return undefined;
      }

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

      if( this._cookies == null ) {
        throw new Error( "No cookies" );
      }

      if( persistentStateKey == null || persistentStateKey.length === 0 ) {
        throw new Error( "No key" );
      }

      let data = this._cookies.get( persistentStateKey );

      //log.debug( "setProperty()", "persistentStateData", persistentStateData );

      if( data == null || data === "undefined" ) {

        data = {};
      }
      else {
        data = persistentStateData;
      }

      this._cookies.set( persistentStateKey, data, { path: "/"} );       

      //log.traceOut( "setProperty()" );

    } catch( error ) {
      console.warn( "setProperty()", error );

      throw new Error( (error as any).message );
    }

  }

   private _cookies : Cookies | undefined; 
  
}

