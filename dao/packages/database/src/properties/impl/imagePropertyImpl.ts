import { MediaType, MediaTypes } from "@dao/storage";
import { DatabaseObject } from "../../core/spec/databaseObject";
import { ImageProperty } from "../spec/imageProperty";
import { PropertyType, PropertyTypes } from "../../core/defs/propertyType";
import { AttachmentPropertyImpl } from "./attachmentPropertyImpl";

export class ImagePropertyImpl extends AttachmentPropertyImpl implements ImageProperty{

    constructor( parent : DatabaseObject ) {

        super( parent, PropertyTypes.Image as PropertyType, MediaTypes.Image as MediaType );  
    }

}