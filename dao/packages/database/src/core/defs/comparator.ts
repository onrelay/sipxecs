export const ComparatorName = "comparator";

export const Comparators = {

    Includes: "includes",

    IncludesAny: "includesAny",

    NotIncludes: "notIncludes",

    In: "in",

    NotIn: "notIn",

    Equal : "equal",

    NotEqual : "notEqual",

    LessThan : "lessThan",

    LessThanOrEqual : "lessThanOrEqual",

    GreaterThan: "greaterThan",

    GreaterThanOrEqual: "greaterThanOrEqual",

    Exists: "exists",

    NotExists: 'notExists'

}

export type Comparator = keyof (typeof Comparators);  

export const DefaultComparator = Comparators.Includes as Comparator;
