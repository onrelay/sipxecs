import { Environment, Platform, Target } from "@sipxdao/common";
import { ConfigurationManager } from "./configurationManager";
import { log } from "../internal/configurationServiceImpl";

export abstract class AbstractConfigurationManager implements ConfigurationManager {

    constructor( application : string, environment : Environment, platform : Platform, target : Target ) {

        //log.traceIn( "constructor()");

        try {
            this.application = application;

            this.environment = environment;

            this.platform = platform;

            this.target = target;

            //log.traceOut( "constructor()" );
            
        } catch( error ) {

            log.warn( "Error creating configuration manager", error );
            
            throw new Error( (error as any).message );
        }
    }

    async init() : Promise<void> {

        //log.traceIn( "init()");

        try {

            if( this._initialized ) {
                return;
            }

            //log.traceOut( "constructor()" );
            
        } catch( error ) {

            log.warn( "Error initializing configuration manager", error );
            
            throw new Error( (error as any).message );
        }
    }

    parse( configData : object, key? : string, language? : string ) : any {

        try {
            let data;

            if (typeof configData === 'object' ) { 

                data = configData;
            } 
            else if (typeof configData === 'string') {
    
                try {
                    data = JSON.parse( configData );
                } catch( error ) {
                    data = configData;
                }
            }

            let targetData = data[this.target]; 

            if( targetData != null ) {

                if( key != null ) {

                    key.split('.').forEach( key => {
                        if( targetData != null ) {
                            targetData = targetData[key]
                        }
                    });
                }

                if( targetData != null ) {

                    if( language != null && targetData[language] != null ) {
                        return targetData[language];
                    }
                    else {
                        return targetData;
                    }
                }
            }

            let genericData = data;
 
            if( key != null ) {
                key.split('.').forEach(key => {
                    if( genericData != null ) {
                        genericData = genericData[key];
                    }
                });
            }

            if( genericData == null ) {
                throw new Error( "Configuration item not found"); 
            }

            if( language != null && genericData[language] != null ) {
                return genericData[language];
            }
            else {
                return genericData;
            }

        } catch (error) {

            console.warn( "Error reading configuration", {key}, {language}, error );

            throw new Error( (error as any).message );
        }
    }

    cached( configName : string, key? : string, language? : string ) : any {  

        try {

            const configData = this._cache!.get( configName )!;

            return this.parse( configData, key, language ); 

        } catch (error) {

            console.warn( "Error reading cached configuration", {configName}, {key}, {language}, error );

            throw new Error( (error as any).message );
        }
    }

    cache( configName : string, configData : object ) : void {
       
        try {
            this._cache.set( configName, configData );

            // console.debug( "Added to cache:", {configName} );

        } catch (error) {

            console.warn( "Error caching configuration", {configName}, error );

            throw new Error( (error as any).message );
        }
    }

    readonly application : string;

    readonly environment : Environment; 

    readonly platform : Platform;

    readonly target : Target;
    
    private _initialized : boolean = false; 

    private readonly _cache = new Map<string,any>();

}