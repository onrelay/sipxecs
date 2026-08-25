import { MediaType, StorageMedia, storageServiceFactory } from "@dao/storage";
import { MapPropertyImpl } from "./mapPropertyImpl";
import { AttachmentsProperty } from "../spec/attachmentsProperty";
import { DatabaseObject } from "../../core/spec/databaseObject";
import { PropertyType, PropertyTypes } from "../../core/defs/propertyType";
import { Monitor } from "@dao/common";
import { log } from "../../core/base/abstractDatabaseService";

const Factory = { get: () => ({ storageService: storageServiceFactory?.get() }) };

export class AttachmentsPropertyImpl extends MapPropertyImpl<StorageMedia> implements AttachmentsProperty{

    constructor( parent : DatabaseObject, mediaType : MediaType ) {

        super( parent, PropertyTypes.Attachments as PropertyType ); 

        this.mediaType = mediaType; 
    }

    setValue( value : Map<string,StorageMedia> | undefined ): void {

        //log.traceIn( "setValue()", {value} );

        const previousAttachments = this.value();

        if( previousAttachments != null ) {

            for( const previousPath of previousAttachments.keys() ) {

                if( value == null || !value.has( previousPath )) {

                    if( this._pendingDeletePaths == null ) {
                        this._pendingDeletePaths = [];
                    }

                    this._pendingDeletePaths.push( previousPath );
                }
            }
            //log.debug( "setValue()", "pending deletion",  this._pendingDeletePaths );
        }
        else if ( value != null && this._pendingDeletePaths != null ) {

            log.debug( "setValue()", "remove pending deletion" );

            delete this._pendingDeletePaths;
        }

        super.setValue( value );

        //log.traceOut( "setValue()" );

    }

    addAttachment( attachment : StorageMedia ): void {

        //log.traceIn( "addAttachment()", {attachment} );

        try {

            let oldAttachments = this.value();

            let newAttachments = new Map<string,StorageMedia>( oldAttachments );

            let mediaPath = this.mediaPathPrefix();

            if( attachment?.data != null && (attachment.data as any).name != null ) {
                mediaPath += "/" + (attachment.data as any).name;
            }
            else {
                mediaPath += "/" + newAttachments.size;
            }

            attachment.path = mediaPath;

            newAttachments.set( mediaPath, attachment );

            super.setValue( newAttachments );
           
            //log.traceOut( "addAttachment()" );

        } catch( error ) {

            log.warn( "download()", "Error downloading attachments", error );

            throw new Error( (error as any).message );
        }    
    }

    removeAttachment( attachment : StorageMedia ): boolean {

        //log.traceIn( "removeAttachment()", {attachment} );

        try {

            if( attachment.path == null ) {

                //log.traceOut( "removeAttachment()", "no attachment path" );
                return false;
            }

            const oldAttachments = this.value();

            if( oldAttachments == null ) {

                //log.traceOut( "removeAttachment()", "no attachments" );
                return false;
            }

            const newAttachments = new Map<string,StorageMedia>( oldAttachments );
            
            if( !newAttachments.delete( attachment.path ) ) {

                //log.traceOut( "removeAttachment()", "not found", {attachments} );
                return false;
            }

            super.setValue( newAttachments );

            if( !this.isChanged() ) {
                //log.traceOut( "removeAttachment()", "rejected", {attachments} );
                return false;
            }

            if( this._pendingDeletePaths == null ) {
                this._pendingDeletePaths = [];
            }

            this._pendingDeletePaths.push( attachment.path );

            //log.traceOut( "removeAttachment()", "removed" );
            return true;
        
        } catch( error ) {

            log.warn( "download()", "Error downloading attachments", error );

            throw new Error( (error as any).message );
        }    
    }

    async downloadUrls(): Promise<Map<string,string> | undefined> {
 
        //log.traceIn( "downloadUrls()" );

        let result = new Map<string,string>();
        try {

            if( Factory.get().storageService == null ) {
                throw new Error( "No storage service");
            }

            const attachments = super.value();

            if( attachments == null ) {
                //log.traceOut( "downloadUrls()", "no attachments" );
                return undefined;
            }

            await Promise.all( Array.from( attachments.values() ).map( async media => { 

                try {

                    const downloadUrl = await Factory.get().storageService!.mediaManager.downloadUrl( media );

                    if( downloadUrl != null ) {
                        result.set( media.path, downloadUrl );
                    }

                } catch (error) {
                    log.warn("downloadUrls()", "Error getting download URL: " + media.path, error);
                }
            }));
            
            //log.traceOut( "downloadUrls()", "OK" );
            return result;  

        } catch( error ) {

            log.warn( "downloadUrls()", "Error getting download URLs", error );

            throw new Error( (error as any).message );
        }    
    }

    async download( monitor? : Monitor ): Promise<Map<string,StorageMedia> | undefined> {
 
        //log.traceIn( "download()" );

        let result = new Map<string,StorageMedia>();
        try {

            if( Factory.get().storageService == null ) {
                throw new Error( "No storage service");
            }

            const attachments = super.value();

            if( attachments == null ) {
                //log.traceOut( "download()", "no attachments" );
                return undefined;
            }

            await Promise.all( Array.from( attachments.values() ).map( async media => {

                try {

                    await Factory.get().storageService!.mediaManager.download( media, monitor );

                    result.set( media.path, media );

                } catch (error) {
                    log.warn("download()", "Error downloading media: " + media.path, error);
                }
            }));
            
           // log.traceOut( "download()", "OK" );
            return result;  

        } catch( error ) {

            log.warn( "download()", "Error downloading attachments", error );

            throw new Error( (error as any).message );
        }    
    }

    async upload( monitor? : Monitor ): Promise<void> {
 
        //log.traceIn( "upload()",  );

        try {

            if( Factory.get().storageService == null ) {
                throw new Error( "No storage service");
            }

            const attachments = super.value();

            if( attachments == null || attachments.size === 0 ) {
                //log.traceOut( "upload()", "no attachments" );
                return; 
            }
            
            const previousValues = this.previousValues();

            const attachmentList = Array.from( attachments.values() );

            await Promise.all( attachmentList.map( async attachment => {

                try {

                    const index = attachmentList.indexOf( attachment );

                    attachment.path = this.mediaPath( attachment, index );

                    attachment.mediaType = this.mediaType;
        
                    await Factory.get().storageService!.mediaManager.upload( attachment, monitor );
                    
                    delete attachment.data;       
                            
                } catch (error) {
                    log.warn("upload()", "Error uploading media", error);
                }
            }));


            this.setValue( attachments );

            this.setPreviousValues( previousValues );

            //log.traceOut( "upload()", result );

        } catch( error ) {

            log.warn( "setAttachments()", "Error uploading attachments", error );

            throw new Error( (error as any).message );
        }    
    }

    async delete( monitor? : Monitor ): Promise<void> {
 
        //log.traceIn( "delete()",  );

        try {

            if( Factory.get().storageService == null ) {
                throw new Error( "No storage service");
            }

            const attachments = super.value();

            if( attachments == null || attachments.size === 0 ) {
                //log.traceOut( "delete()", "no attachments" );
                return; 
            }
            
            const attachmentList = Array.from( attachments.values() );

            await Promise.all( attachmentList.map( async attachment => {

                try {
        
                    if( attachment.path != null ) {

                        await Factory.get().storageService!.mediaManager.delete( attachment.path );
                    }                                                
                } catch (error) {
                    log.warn("upload()", "Error uploading media", error);
                }
            }));


            this.setValue( undefined );

            //log.traceOut( "upload()", result );

        } catch( error ) {

            log.warn( "setAttachments()", "Error uploading attachments", error );

            throw new Error( (error as any).message );
        }    
    }

    async onCreate(): Promise<void> {

        try {
            //log.traceIn( "onCreate()" );

            await super.onCreate();

            await this.upload();

            //log.traceOut( "onCreate()" );

        } catch (error) {

            log.warn("onCreated()", "Error handling created notification", error);

            throw new Error( (error as any).message );
        }
    }

    async onUpdate(): Promise<void> {
        try {
            //log.traceIn( "onUpdate()" );

            await super.onUpdate();

            if( this._pendingDeletePaths != null ) {

                await Promise.all( this._pendingDeletePaths.map( async pendingDeletePath => {

                    try {
                        await Factory.get().storageService!.mediaManager.delete( pendingDeletePath );     
                                
                    } catch (error) {
                        log.warn("onUpdate()", "Error deleting media", error);
                    }
                }));

                delete this._pendingDeletePaths;

                //log.traceOut( "onUpdate()", "pending delete" );
                return;
            }

            await this.upload();

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

    private mediaPath( attachment : StorageMedia, index? : number ) : string {

        let mediaPath = this.mediaPathPrefix();

        if( mediaPath == null ) {
            throw new Error( "Parent document has not been created" );
        }

        if( attachment?.data != null && (attachment.data as any).name != null ) {
            mediaPath += "/" + (attachment.data as any).name;
        }
        else if( index != null ) {
            mediaPath += "/" + index;
        }

        return mediaPath;
    }

    private mediaPathPrefix() : string {

        let mediaPathPrefix = this.parent.ownerPath();

        if( mediaPathPrefix == null ) {
            throw new Error( "Parent document has not been created" );
        }

        mediaPathPrefix += "/" + this.key();

        return mediaPathPrefix;
    }

    mediaType : MediaType;

    private _pendingDeletePaths? : string[];

}