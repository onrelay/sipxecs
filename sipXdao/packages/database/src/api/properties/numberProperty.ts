import { BasicProperty } from "./basicProperty";

export interface NumberProperty extends BasicProperty<number> {

    readonly minValue? : number;

    readonly maxValue? : number;

}

