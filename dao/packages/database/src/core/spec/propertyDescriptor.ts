import { BooleanProperty } from "../../properties/spec/booleanProperty";
import { DefinitionProperty } from "../../properties/spec/definitionProperty";
import { LongTextProperty } from "../../properties/spec/LongTextProperty";
import { TextsProperty } from "../../properties/spec/textsProperty";
import { DatabaseProperty } from "./databaseProperty";
import { DatabaseSubdocument } from "./databaseSubdocument";
import { PropertyType } from "../defs/propertyType";
import { TextType } from "../defs/textType";

export const PropertyDescriptorName = "propertyDescriptor";

export const PropertyDescriptorsName = "propertyDescriptors";

export interface PropertyDescriptor<Property extends DatabaseProperty<any>> extends DatabaseSubdocument  { 

    readonly propertyType  : DefinitionProperty<PropertyType>;

    textType?  : DefinitionProperty<TextType>; 

    prompt? : LongTextProperty;

    help? : LongTextProperty;

    options? : TextsProperty;  

    minValue? : DatabaseProperty<any>; 

    maxValue? : DatabaseProperty<any>;

    defaultValue? : DatabaseProperty<any>;

    propertyDisabled? : BooleanProperty; 
    
    propertyRequired? : BooleanProperty;   

}

