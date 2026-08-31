import { Language } from "../types/language";
import { Translator } from "./translator";


export abstract class AbstractTranslator implements Translator { 

    exists( key : string, params?: { 
        translations? : any, 
        namespace?: string,
        language? : Language } ) : boolean {

        return this.translate( key, params ) != null;
    } 

    useLanguage( language? : Language ) : Language {

        try {
            return language != null ? language!: 
                this.activeLanguage() != null ? this.activeLanguage()! :
                this.defaultLanguage();

        } catch (error) {

            console.warn("Error retrieving use language", error);

            throw new Error( (error as any).message );
        }
    }

    abstract defaultLanguage() : Language;

    abstract activeLanguage() : Language | undefined;

    abstract setActiveLanguage( activeLanguage? : Language ) : void;

    abstract loadTranslations( params: { 
        translations: void,
        namespace : string,
        language? : Language
    } ) : void;

    abstract translate( key : string, params?: { 
        translations? : any, 
        namespace?: string,
        language? : Language } ) : string | undefined;
}