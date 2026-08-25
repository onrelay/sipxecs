import { Monitor } from "@dao/common";
import { StorageMedia, MediaType } from "@dao/storage";
import { MapProperty } from "./mapProperty";
import { PropertyDescriptor } from "../../core/spec/propertyDescriptor";

export interface AttachmentsProperty extends MapProperty<StorageMedia> {

    addAttachment( attachment : StorageMedia ): void;

    removeAttachment( attachment : StorageMedia ): boolean;

    downloadUrls(): Promise<Map<string,string> | undefined>;

    download( monitor? : Monitor ): Promise<Map<string,StorageMedia> | undefined>;

    upload( monitor? : Monitor ): Promise<void>;

    mediaType? : MediaType;
}

export interface AttachmentsPropertyDescriptor extends PropertyDescriptor<AttachmentsProperty>  { 

}


