import { TextProperty, TextsProperty, User, XmlDatabaseDocument } from "@dao/database";

export const SipxUserDocumentName = "sipxUser";

export interface SipxUser extends User, XmlDatabaseDocument {

    readonly userName : TextProperty;

    readonly pin : TextProperty;

    readonly sipPassword : TextProperty;

    readonly groups : TextsProperty;

    readonly aliases : TextsProperty;
}

