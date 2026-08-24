import { AbstractTranslator, Language, Languages } from "@dao/common";
import { configurationServiceFactory } from "./configurationServiceFactory";
import { log } from "./configurationServiceImpl";

export const commonNamespace = "common";

import commonEnTranslations from "../data/translations/common/common.en.json";
import commonNbTranslations from "../data/translations/common/common.nb.json";

export class ConfigurationTranslator extends AbstractTranslator { 

    constructor( defaultLanguage : Language ) {

        super( commonNamespace, defaultLanguage );
    }

    load() : void {

        try {
            super.load();

            this.loadTranslations( {
                translations: commonEnTranslations,
                namespace: commonNamespace,
                language: Languages.English
            })

            this.loadTranslations( {
                translations: commonNbTranslations,
                namespace: commonNamespace,
                language: Languages.Norwegian
            })

        } catch( error ) {

            log.warn( "Error loading configuration translator", error );
            
            throw new Error( "Error loading configuration translator" ); 
        }
    }
    

    activeLanguage() : Language | undefined {
        return this._activeLanguage;
    }


    setActiveLanguage( activeLanguage? : Language ) : void {
        this._activeLanguage = activeLanguage;
    }


    loadTranslations( params: { 
        translations: any,
        namespace : string,
        language? : Language } ) : void {

        try {            
            configurationServiceFactory!.get().load( 
                params.namespace!, 
                params.translations!,
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

            // only (re)load when actual translations are supplied, otherwise this is a plain lookup
            if( params?.translations != null ) {

                this.loadTranslations( {
                    translations: params.translations,
                    namespace: params.namespace!,
                    language: params.language
                } );
            }

            const namespace = this.useNamespace( params?.namespace );

            const language = this.useLanguage( params?.language );

            const translation = 
                configurationServiceFactory!.get().config( namespace, key, language );

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
}