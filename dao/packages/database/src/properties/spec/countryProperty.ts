import { BasicProperty } from "./basicProperty";
import { PropertyDescriptor } from "../../core/spec/propertyDescriptor";

export interface CountryProperty extends BasicProperty<string> {

}

export interface CountryPropertyDescriptor extends PropertyDescriptor<CountryProperty>  { 

}



