import { Comparator } from "../types/comparator"

export type DatabaseFilter = {

    property: string,

    comparator: Comparator, 

    value?: any,

    ignoreEmpty?: boolean

}

