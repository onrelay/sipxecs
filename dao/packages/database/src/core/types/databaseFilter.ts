import { Comparator } from "../defs/comparator"

export type DatabaseFilter = {

    property: string,

    comparator: Comparator, 

    value?: any,

    ignoreEmpty?: boolean

}

