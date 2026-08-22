import { BasicProperty } from "./basicProperty";
import { PropertyDescriptor } from "../dao/propertyDescriptor";

export interface CountryProperty extends BasicProperty<string> {

}

export interface CountryPropertyDescriptor extends PropertyDescriptor<CountryProperty>  { 

}



