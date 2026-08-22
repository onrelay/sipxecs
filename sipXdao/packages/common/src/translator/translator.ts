import { Language } from "../types/language";

export type EntityLike = {
  language: {
    value(): Language | undefined;
  };
};

export const defaultNamespace = "translation";
  
export interface Translator  {

    translations( params?: { 
      namespace? : string, 
      language? : Language } ) : Promise<any>,

    exists(  key? : string, params?: { 
      translations? : any, 
      namespace?: string,
      language? : Language } ) : boolean;

    translate( key? : string, params?: { 
      translations? : any, 
      namespace?: string,
      language? : Language } ) : string | undefined;

    activeLanguage() : Language | undefined;

    setActiveLanguage( activeLanguage? : Language ) : void,

    defaultLanguage() : Language;

    language( entity? : EntityLike ) : Language;
}

