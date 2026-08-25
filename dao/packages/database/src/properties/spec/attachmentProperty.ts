import { Monitor } from "@dao/common"
import { MediaType, StorageMedia } from "@dao/storage"

import { PropertyDescriptor } from "../../core/spec/propertyDescriptor";
import { DataProperty } from "./dataProperty";

export interface AttachmentProperty extends DataProperty<StorageMedia> {

    downloadUrl(): Promise<string | undefined>;

    download( monitor? : Monitor ): Promise<StorageMedia | undefined>;

    upload( monitor? : Monitor ): Promise<void>;

    delete(): Promise<void>;

    mediaType? : MediaType;
}

export interface AttachmentPropertyDescriptor extends PropertyDescriptor<AttachmentProperty>  { 

}


