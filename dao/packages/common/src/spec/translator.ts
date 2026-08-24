import { Language } from "../defs/language";

export const defaultNamespace = "translation";
  
export interface Translator  {

    load() : void;

    loadTranslations( params: { 
      translations : any, 
      namespace : string,
      language? : Language } ) : void,

    exists( key : string, params?: { 
      translations? : any, 
      namespace?: string,
      language? : Language } ) : boolean;

    translate( key : string, params?: { 
      translations? : any, 
      namespace?: string,
      language? : Language } ) : string | undefined;

    activeLanguage() : Language | undefined;

    setActiveLanguage( activeLanguage? : Language ) : void,

    useLanguage( language? : Language ) : Language;

    useNamespace( namespace? : string ) : string;

    readonly defaultLanguage : Language;

    readonly defaultNamespace : string;

}

