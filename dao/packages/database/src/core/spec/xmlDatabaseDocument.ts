import { DatabaseDocument } from "./databaseDocument";

export interface XmlDatabaseDocument extends DatabaseDocument {

    fromXml( xml: string ): void;

    toXml(): string;
}
