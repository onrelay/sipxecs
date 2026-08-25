
import { MapProperty } from "./mapProperty";
import { PropertyDescriptor } from "../../core/spec/propertyDescriptor";


export interface LinksProperty extends MapProperty<string> {

    setLink( title : string, url : string ): void;

    removeLink( title : string ): boolean;
}

export interface LinksPropertyDescriptor extends PropertyDescriptor<LinksProperty>  { 

}

