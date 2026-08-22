import { MediaType, MediaTypes } from "@sipxdao/storage";
import { DatabaseObject } from "../../api/dao/databaseObject";
import { ImageProperty } from "../../api/properties/imageProperty";
import { PropertyType, PropertyTypes } from "../../api/types/propertyType";
import { AttachmentPropertyImpl } from "./attachmentPropertyImpl";

export class ImagePropertyImpl extends AttachmentPropertyImpl implements ImageProperty{

    constructor( parent : DatabaseObject ) {

        super( parent, PropertyTypes.Image as PropertyType, MediaTypes.Image as MediaType );  
    }

}