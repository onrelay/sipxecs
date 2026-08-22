import { TextType } from "../types/textType";
import { BasicProperty } from "./basicProperty";


export interface TextProperty extends BasicProperty<string> {

    requiresSelect() : boolean,

    select( params? : { filterValue? : boolean } ) : Promise<Map<string,string> | undefined>,

    readonly textType : TextType,
}

