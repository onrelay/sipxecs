import { PropertyType } from "../defs/propertyType";


export type PropertiesSelector = {

    includePropertyKeys?: string[]; // Can be used to control order returned from properties() method

    excludePropertyKeys?: string[];

    includePropertyTypes?: PropertyType[]; 

    excludePropertyTypes?: PropertyType[]; 
}; 
