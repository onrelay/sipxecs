import { Language } from "../defs/language";
import { Translator } from "../spec/translator";

export abstract class AbstractTranslator implements Translator { 

    constructor( 
        defautlNamespace : string, 
        defaultLanguage : Language ) {
        
        this.defaultNamespace = defautlNamespace;
        this.defaultLanguage = defaultLanguage;
    }

    load() : void {}
    
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
                this.defaultLanguage;

        } catch (error) {

            console.warn("Error retrieving use language", error);

            throw new Error( (error as any).message );
        }
    }

    useNamespace( namespace? : string ) : string {

        try {
            return namespace != null ? namespace!: 
                this.defaultNamespace;

        } catch (error) {

            console.warn("Error retrieving use language", error);

            throw new Error( (error as any).message );
        }
    }

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

    readonly defaultLanguage;

    readonly defaultNamespace;
}