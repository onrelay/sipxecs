import { Language } from "../types/language";

export const defaultNamespace = "translation";
  
export interface Translator  {

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

    defaultLanguage() : Language;

    useLanguage( language? : Language ) : Language;
}

