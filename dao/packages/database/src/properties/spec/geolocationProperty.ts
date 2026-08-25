import { Address } from "../../documents/spec/address";
import { BasicProperty } from "./basicProperty";
import { CountryProperty } from "./countryProperty";
import { DataProperty } from "./dataProperty";
import { DefinitionProperty } from "./definitionProperty";
import { SubdocumentProperty } from "./subdocumentProperty";
import { TextProperty } from "./textProperty";
import { PropertyDescriptor } from "../../core/spec/propertyDescriptor";

export type Geolocation = {

    lat: number,

    lng: number,

    zoom?: number
}

export interface GeolocationProperty extends DataProperty<Geolocation> {

    countryProperty() : CountryProperty | undefined;

    typeProperty() : DefinitionProperty<any> | undefined;

    levelProperty() : DefinitionProperty<any> | undefined;

    titleProperty() : TextProperty | undefined;

    statusProperty() : BasicProperty<string> | undefined;

    addressProperty() : SubdocumentProperty<Address> | undefined;

    geolocation() : Geolocation | undefined,

    setGeolocation( value : Geolocation | undefined ) : void,
}

export interface GeolocationPropertyDescriptor extends PropertyDescriptor<GeolocationProperty>  { 

}
