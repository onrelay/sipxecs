import { DatabaseDocument } from "../../core/spec/databaseDocument";
import { Template } from "../../documents/spec/template";
import { TemplatedDocument } from "../../core/spec/templatedDocument";
import { DocumentProperty } from "./documentProperty";

export interface TemplateProperty<T extends Template<TemplatedDocument>> extends DocumentProperty<T> {

}

