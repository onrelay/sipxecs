export const ChangeTypeName = "changeType";

export const ChangeTypes = {

    Created      : "created",

    Updated      : "updated",

    Archived     : "archived",

    Restored     : "restored",

    Deleted      : "deleted",

} as const

export type ChangeType = (typeof ChangeTypes)[keyof typeof ChangeTypes]; 




