import { DatabaseProperty } from "../dao/databaseProperty";
import { PropertyDescriptor } from "../dao/propertyDescriptor";

export interface DataProperty<Data extends Object> extends DatabaseProperty<Data> {

    data() : Data | undefined;

    setData( data : Data ) : void;
}

export interface DataPropertyDescriptor<Data extends Object> extends PropertyDescriptor<DataProperty<Data>>  { 

}