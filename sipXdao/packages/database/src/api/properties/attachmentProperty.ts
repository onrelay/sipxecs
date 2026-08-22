import { Monitor } from "@sipxdao/common"
import { MediaType, StorageMedia } from "@sipxdao/storage"

import { PropertyDescriptor } from "../dao/propertyDescriptor";
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


