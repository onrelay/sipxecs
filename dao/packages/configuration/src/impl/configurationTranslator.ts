import { log } from "@dao/common/src/application/application";
import { Language } from "@dao/common/src/types/language";
import { AbstractTranslator } from "@dao/common/src/translator/abstractTranslator";
import { configurationServiceFactory } from "./configurationServiceFactory";


export class ConfigurationTranslator extends AbstractTranslator { 

        constructor( defaultLanguage : Language ) {

        super();
        
        //log.traceIn( "constructor()");

        try {
            this._defaultLanguage = defaultLanguage;

            //log.traceOut( "constructor()" );
            
        } catch( error ) {

            log.warn( "Error initializing configuration database", error );
            
            throw new Error( (error as any).message );
        }
    }

    activeLanguage() : Language | undefined {

        return this._activeLanguage;
    }


    setActiveLanguage( activeLanguage? : Language ) : void {
        this._activeLanguage = activeLanguage;
    }


    defaultLanguage() : Language {
        return this._defaultLanguage;
    }

    loadTranslations( params: { 
        translations: any,
        namespace : string,
        language? : Language } ) : void {

        try {            
            configurationServiceFactory!.get().load( 
                params.translations!,
                params.namespace!, 
                params.language );

        } catch (error) {

            log.warn("Error loading translations", error);

            throw new Error( (error as any).message );
        }
    }

    translate( key : string, params?: { 
        translations? : any, 
        namespace?: string,
        language? : Language } ) : string | undefined {

        try {   

            if( params != null ) {

                this.loadTranslations( {
                    translations: params.translations,
                    namespace: params.namespace!,
                    language: params.language
                } );
            }
                      
            let translation = 
                configurationServiceFactory!.get().config( params!.namespace!, key, params?.language );

            if( translation != null && typeof translation == "string" ) {
                return translation;
            }

            log.warn("Translation not found for key: ", key );
            return key;

        } catch (error) {

            log.warn("Error loading translations", error);

            throw new Error( (error as any).message );
        }
    }


    private _activeLanguage? : Language;

    private readonly _defaultLanguage : Language;
}