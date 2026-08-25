import { KeyStatus } from "../defs/keyStatus";

export type SymmetricKey = {

    id: string;

    status?: KeyStatus,

    versions : any
}

