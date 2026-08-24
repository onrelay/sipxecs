
export const SortOrientations = {

    Ascending: "ascending",

    Descending: "descending"
} as const

export type SortOrientation = (typeof SortOrientations)[keyof typeof SortOrientations];  

