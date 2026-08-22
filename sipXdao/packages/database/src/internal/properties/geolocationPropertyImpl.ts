import { DatabaseObject } from "../../api/dao/databaseObject";
import { BasicProperty } from "../../api/properties/basicProperty";
import { CountryProperty } from "../../api/properties/countryProperty";
import { DefinitionProperty } from "../../api/properties/definitionProperty";
import { Geolocation, GeolocationProperty } from "../../api/properties/geolocationProperty";
import { Address } from "../../api/types/address";
import { PropertyType, PropertyTypes } from "../../api/types/propertyType";
import { DataPropertyImpl } from "./dataPropertyImpl";
import { LongTextProperty } from "../../api/properties/LongTextProperty";
import { SubdocumentProperty } from "../../api/properties/subdocumentProperty";
import { TextProperty } from "../../api/properties/textProperty";

export class GeolocationPropertyImpl
    extends DataPropertyImpl<Geolocation> implements GeolocationProperty{

    constructor( parent : DatabaseObject ) {
        super( parent, PropertyTypes.Geolocation as PropertyType ); 
    }

    countryProperty() : CountryProperty | undefined {  
        return this._countryProperty;
    }

    setCountryProperty( countryProperty? : CountryProperty ) {
        this._countryProperty = countryProperty;
    }

    typeProperty() : DefinitionProperty<any> | undefined {
        return this._typeProperty;
    }

    setTypeProperty( typeProperty? : DefinitionProperty<any>  ) {
        this._typeProperty = typeProperty;
    }

    levelProperty() : DefinitionProperty<any> | undefined {
        return this._levelProperty;
    }

    setLevelProperty( levelProperty? : DefinitionProperty<any>  ) {
        this._levelProperty = levelProperty;
    }

    titleProperty() : TextProperty | undefined {
        return this._titleProperty;
    }

    setTitleProperty( titleProperty? : TextProperty ) {
        this._titleProperty = titleProperty;
    }

    statusProperty() : BasicProperty<string> | undefined {
        return this._statusProperty;
    }

    setStatusProperty( statusProperty? : TextProperty | LongTextProperty ) {
        this._statusProperty = statusProperty;
    }

    addressProperty() : SubdocumentProperty<Address> | undefined {
        return this._addressProperty;
    }

    setAddressProperty( addressProperty? : SubdocumentProperty<Address> ) {
        this._addressProperty = addressProperty;
    }

    geolocation() : Geolocation | undefined {
        return this.value();
    }

    setGeolocation( geolocation : any | undefined ): void {
        this.setValue( geolocation );
    }

    private _countryProperty? : CountryProperty;

    private _typeProperty? : DefinitionProperty<any>;

    private _levelProperty? : DefinitionProperty<any>;

    private _titleProperty? : TextProperty;

    private _statusProperty? : BasicProperty<string>;

    private _addressProperty? : SubdocumentProperty<Address>;

}