import { AuthorizationType } from "../defs/authorizationType";

export type Authorization = {

    readonly namespace : string;

    readonly key? : string;

    readonly authorizationType : AuthorizationType;
}