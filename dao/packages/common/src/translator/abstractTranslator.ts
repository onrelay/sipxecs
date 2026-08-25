import { Language } from "../types/language";
import { EntityLike, Translator } from "./translator";



export abstract class AbstractTranslator implements Translator { 


    exists( key? : string, params?: { 
        translations? : any, 
        namespace?: string,
        language? : Language } ) : boolean {

        return this.translate( key, params ) != null;
    } 

    language( entity? : EntityLike ) : Language {

        try {

            return entity?.language.value() != null ? entity.language.value()!: 
                this.activeLanguage() != null ? this.activeLanguage()! :
                this.defaultLanguage();

        } catch (error) {

            console.warn("Error retrieving language for organization and user", error);

            throw new Error( (error as any).message );
        }
    }

    abstract defaultLanguage() : Language;

    abstract activeLanguage() : Language | undefined;

    abstract setActiveLanguage( activeLanguage? : Language ) : void;

    abstract translations( params: { 
        namespace? : string, 
        language? : Language 
    } ) : Promise<any>;

    abstract translate( key? : string, params?: { 
        translations? : any, 
        namespace?: string,
        language? : Language } ) : string | undefined;



}