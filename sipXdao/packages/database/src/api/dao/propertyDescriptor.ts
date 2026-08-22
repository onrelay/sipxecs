import { BooleanProperty } from "../properties/booleanProperty";
import { DefinitionProperty } from "../properties/definitionProperty";
import { LongTextProperty } from "../properties/LongTextProperty";
import { TextsProperty } from "../properties/textsProperty";
import { DatabaseProperty } from "./databaseProperty";
import { DatabaseSubdocument } from "./databaseSubdocument";
import { PropertyType } from "../types/propertyType";
import { TextType } from "../types/textType";

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

