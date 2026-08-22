import { DatabaseDocument } from "../dao/databaseDocument";
import { Template } from "../dao/template";
import { TemplatedDocument } from "../dao/templatedDocument";
import { DocumentProperty } from "./documentProperty";

export interface TemplateProperty<T extends Template<TemplatedDocument>> extends DocumentProperty<T> {

}

