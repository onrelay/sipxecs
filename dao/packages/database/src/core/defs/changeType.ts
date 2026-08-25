export const ChangeTypeName = "changeType";

export const ChangeTypes = {

    Created      : "created",

    Updated      : "updated",

    Archived     : "archived",

    Restored     : "restored",

    Deleted      : "deleted",

}

export type ChangeType = keyof (typeof ChangeTypes); 




