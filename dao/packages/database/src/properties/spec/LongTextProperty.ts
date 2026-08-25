import { BasicProperty } from "./basicProperty";

export interface LongTextProperty extends BasicProperty<string> {

    requiresSelect() : boolean;

}


