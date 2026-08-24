
export const KeyStatusName = "keyStatus";

export const KeyStatuses = {

    Requested : "requested",

    Enabled : "enabled",

    Disabled : "disabled",

    Destroyed: "destroyed"

} as const

export type KeyStatus = (typeof KeyStatuses)[keyof typeof KeyStatuses];
