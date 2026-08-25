
export const KeyStatusName = "keyStatus";

export const KeyStatuses = {

    Requested : "requested",

    Enabled : "enabled",

    Disabled : "disabled",

    Destroyed: "destroyed"

}

export type KeyStatus = keyof (typeof KeyStatuses);
