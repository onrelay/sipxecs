import { MediaType, StorageMedia, storageServiceFactory } from "@sipxdao/storage";
import { AttachmentProperty } from "../../api/properties/attachmentProperty";
import { DataPropertyImpl } from "./dataPropertyImpl";
import { DatabaseObject } from "../../api/dao/databaseObject";
import { PropertyType } from "../../api/types/propertyType";
import { Monitor } from "@sipxdao/common";
import { log } from "../../api/dao/abstractDatabaseService";

const Factory = { get: () => ({ storageService: storageServiceFactory?.get() }) };


export class AttachmentPropertyImpl extends DataPropertyImpl<StorageMedia> implements AttachmentProperty{

    constructor( parent : DatabaseObject, type : PropertyType, mediaType? : MediaType ) {

        super( parent, type );

        this.mediaType = mediaType;
    }

    setValue( data : StorageMedia | undefined ) : void { 

        this.setData( data );
    }

    setData( data : StorageMedia | undefined ): void {

        //log.traceIn( "setData()", {data} );

        const previousData = this.data();

        if( data == null && previousData?.path != null ) {

            //log.debug( "setData()", "pending deletion" );

            this._pendingDeletePath = previousData.path;
        }
        else if ( data != null && this._pendingDeletePath != null ) {

            //log.debug( "setData()", "remove pending deletion" );

            delete this._pendingDeletePath;
        }

        super.setData( data );

        //log.traceOut( "setData()" );

    }

    async downloadUrl(): Promise<string | undefined> {
 
        //log.traceIn( "downloadUrl()" );

        try {

            if( storageServiceFactory?.get() == null ) { 
                throw new Error( "No storage service");
            }

            const attachment = super.value();

            if( attachment == null || attachment.path == null ) {
                //log.traceOut( "downloadUrl()", "no attachment" );
                return undefined;
            }

            const downloadUrl = await storageServiceFactory!.get().mediaManager.downloadUrl( attachment );
            
            const previousValues = this.previousValues();

            super.setValue( attachment );

            this.setPreviousValues( previousValues );
            
            //log.traceOut( "downloadUrl()", downloadUrl );
            return downloadUrl;  

        } catch( error ) {

            log.warn( "downloadUrl()", "Error getting download URL ", error );

            throw new Error( (error as any).message );
        }    
    }

    async download( monitor? : Monitor ): Promise<StorageMedia | undefined> {
 
        //log.traceIn( "download()" );

        try {

            if( storageServiceFactory?.get() == null ) {
                throw new Error( "No storage service");
            }

            const attachment = super.value();

            if( attachment == null || attachment.path == null ) {
                //log.traceOut( "download()", "no attachment" );
                return undefined;
            }

            await storageServiceFactory!.get().mediaManager.download( attachment, monitor );

            const previousValues = this.previousValues();

            super.setValue( attachment );

            this.setPreviousValues( previousValues );

            //log.traceOut( "download()", attachment.path );
            return attachment;  

        } catch( error ) {

            log.warn( "download()", "Error downloading attachment", error );

            throw new Error( (error as any).message );
        }    
    }

    async upload( monitor? : Monitor ): Promise<void> {
 
        //log.traceIn( "upload()" );

        try {

            if( Factory.get().storageService == null ) {
                throw new Error( "No storage service");
            }

            const media = this.value();

            if( media == null || media.data == null ) {
                throw new Error( "Nothing to upload");
            }

            media.path = this.mediaPath();

            media.mediaType = this.mediaType;

            await Factory.get().storageService!.mediaManager.upload( media, monitor );
            
            delete media.data;

            const previousValues = this.previousValues();

            super.setValue( media );

            this.setPreviousValues( previousValues );

           // log.traceOut( "upload()", "OK" );

        } catch( error ) {

            log.warn( "upload()", "Error uploading attachment", error );

            throw new Error( (error as any).message );
        }
    }

    async delete(): Promise<void> {
 
        //log.traceIn( "delete()" );

        try {

            if( Factory.get().storageService == null ) {
                throw new Error( "No storage service");
            }

            if( this.data()?.path == null ) {

                //log.traceOut( "delete()", "Nothing to delete" );
                return;
            }

            const mediaPath = this.mediaPath();

            await Factory.get().storageService!.mediaManager.delete( mediaPath ); 

            super.setValue( undefined );

            //log.traceOut( "delete()", "OK" );

        } catch( error ) {

            log.warn( "delete()", "Error uploading attachment", error );

            throw new Error( (error as any).message );
        }
    }

    async onCreate(): Promise<void> {

        try {
            //log.traceIn( "onCreate()" );

            await super.onCreate();

            const media = this.value();

            if( media?.data != null ) {

                await this.upload();
            }

            //log.traceOut( "onCreate()" );

        } catch (error) {

            log.warn("onCreate()", "Error handling created notification", error);

            throw new Error( (error as any).message );
        }
    }

    async onUpdate(): Promise<void> {
        try {
            //log.traceIn( "onUpdate()" );

            await super.onUpdate();

            if( this._pendingDeletePath != null ) {

                await Factory.get().storageService!.mediaManager.delete( this._pendingDeletePath ); 

                delete this._pendingDeletePath;

                //log.traceOut( "onUpdate()", "pending delete" );
                return;
            }

            const media = this.value();

            if( media?.data != null ) {

                await this.upload();
            }

            //log.traceOut( "onUpdate()" );

        } catch (error) {

            log.warn("onUpdated()", "Error handling update notification", error);

            throw new Error( (error as any).message );
        }
    }

    async onDelete(): Promise<void> {

        try {
            //log.traceIn( "onDelete()" );

            await super.onDelete();

            await this.delete();

            //log.traceOut( "onDelete()" );

        } catch (error) {

            log.warn("onDeleted()", "Error handling delete notification", error);

            throw new Error( (error as any).message );
        }
    }

    private mediaPath() : string {

        let mediaPath = this.parent.ownerPath();

        if( mediaPath == null ) {
            throw new Error( "Parent document has not been created" );
        }

        mediaPath += "/" + this.key();

        const attachment = this.value();

        if( attachment?.data != null && (attachment.data as any).name != null ) {
            mediaPath += "/" + (attachment.data as any).name;
        }

        return mediaPath;
    }

    mediaType?: MediaType;

    private _pendingDeletePath? : string;
}