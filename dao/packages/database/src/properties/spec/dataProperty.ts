import { DatabaseProperty } from "../../core/spec/databaseProperty";
import { PropertyDescriptor } from "../../core/spec/propertyDescriptor";

export interface DataProperty<Data extends Object> extends DatabaseProperty<Data> {

    data() : Data | undefined;

    setData( data : Data ) : void;
}

export interface DataPropertyDescriptor<Data extends Object> extends PropertyDescriptor<DataProperty<Data>>  { 

}