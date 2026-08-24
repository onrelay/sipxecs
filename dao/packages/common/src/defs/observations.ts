export const Observations = {

    Create: "create",

    Update: "update",

    Delete: "delete"
} as const

export type Observation = (typeof Observations)[keyof typeof Observations];


