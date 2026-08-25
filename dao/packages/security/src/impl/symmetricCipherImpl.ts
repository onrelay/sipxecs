import * as Crypto from "node:crypto";
import { configurationServiceFactory } from "@dao/configuration";
import { log } from "../base/abstractSecurityService";
import { SymmetricCipher } from "../spec/symmetricCipher";
import { SymmetricKey } from "../types/symmetricKey";
import { EncryptedRecord } from "../types/encryptedRecord";



export class SymmetricCipherImpl implements SymmetricCipher {

    constructor() {

        //log.traceInOut( "constructor()" );

        try {

            this.updateEncryptKey(); 

        } catch( error ) {

            log.warn( "constructor()", error );

            throw new Error( (error as any).message ); 
        }
    }

    setDefaultKey( defaultKey : SymmetricKey | null | undefined ) : void {

        this._defaultKey = defaultKey;

        this.updateEncryptKey();
    }

    defaultKey() : SymmetricKey | null | undefined {

        return this._defaultKey; 
    }

    setKey( key : SymmetricKey | null | undefined ) : void {

        this._key = key;

        this.updateEncryptKey();
    }

    key() : SymmetricKey | null | undefined {

        return this._key;
    }

    newInitializationVector() : string {

        //log.traceIn( "newInitializationVector()")
        try {
            const initializationVectorLength = configurationServiceFactory!.get().config(
                "security", "initializationVectorLength")!;

            let initializationVector = "";

            while(initializationVector.length < initializationVectorLength) {

                initializationVector += Math.random().toString(36).substring(2);
            } 
            initializationVector = initializationVector.substring(0, initializationVectorLength);  

            //log.traceOut( "newInitializationVector()", {initializationVector});
            return initializationVector;
                
        } catch( error ) {

            log.warn( "newInitializationVector()", error );

            throw new Error( (error as any).message );
        }
    }

    encrypt( data? : any ) : string | undefined {

        try {
            if( data == null ) {
                return undefined;
            }

            const algorithm = configurationServiceFactory!.get().config(
                "security", "algorithm")!;

            const encoding = configurationServiceFactory!.get().config(
                "security", "encoding")!; 

            const iv = this.newInitializationVector();

            var cipher = Crypto.createCipheriv( algorithm, this._encryptKeyValue!, iv);

            var hash = cipher.update(JSON.stringify(data), "utf8", encoding); 

            const encrypted = hash + cipher.final(encoding);

            const encryptedData = {
                iv: iv,
                content: encrypted,
                id: this._encryptKeyId,
                version: this._encryptKeyVersion
            } as EncryptedRecord;
   
            const encryptedJson = JSON.stringify( encryptedData );

            return encryptedJson;

        } catch( error ) {

            log.warn( "encrypt()", "problem with key", "encryptKeyId", this._encryptKeyId, "version", this._encryptKeyVersion );

            throw new Error( (error as any).message );


        }
    }

    decrypt( encryptedJson? : string ) : any | undefined {

        try {
            if( encryptedJson == null ) {
                return undefined;
            }

            const algorithm = configurationServiceFactory!.get().config(
                "security", "algorithm")!;

            const encoding = configurationServiceFactory!.get().config(
                "security", "encoding")!;

            const encryptedData = JSON.parse( encryptedJson ) as EncryptedRecord;

            if( encryptedData.iv == null || encryptedData.content == null ) {
                log.warn( "Leaving unencrypted data alone", encryptedData )
                return encryptedData;
            }

            var decipher;
            var decrypted;

            try {

                const decryptKey = this.decryptKey( this._key!, encryptedData.id, encryptedData.version );

                decipher = Crypto.createDecipheriv( algorithm, decryptKey!, encryptedData.iv );

                decrypted = decipher.update(encryptedData.content, encoding, "utf8");

                decrypted += decipher.final("utf8");

                const jsonData = decrypted.toString();

                const data = JSON.parse( jsonData );

                return data;

            } catch ( error ) {

                const decryptKey = this.decryptKey( this._defaultKey!, encryptedData.id, encryptedData.version );

                decipher = Crypto.createDecipheriv( algorithm, decryptKey!, encryptedData.iv );

                decrypted = decipher.update(encryptedData.content, encoding, "utf8");

                decrypted += decipher.final("utf8");

                const jsonData = decrypted.toString();

                const data = JSON.parse( jsonData );

                return data;
            }

        } catch( error ) {
            //log.warn( "decrypt()", "Error decrypting data", error ); 
            return undefined;
        }
    }

    isEncrypted( data : any ) : boolean { 

        try {
            if( data == null ) {
                return false;
            }

            const encryptedData = JSON.parse( data ) as EncryptedRecord;

            if( encryptedData?.iv != null && encryptedData?.content != null ) {
                return true;
            }

            return false;

        } catch( error ) {
            //log.warn( "isEncrypted()", error ); 

            return false;
        }
    }

    private updateEncryptKey() {

        const key = this._key != null && Object.keys( this._key.versions ).length > 0 ? this._key : this._defaultKey;

        if( key?.versions != null && Object.keys( key.versions ).length > 0 ) {

            this._encryptKeyId = key.id;

            this._encryptKeyVersion = Object.keys( key.versions ).pop();

            this._encryptKeyValue = Object.values( key.versions ).pop() as string;
        }
    }

    private decryptKey( key : SymmetricKey, keyId? : string, keyVersion? : string ) : string | undefined {

        if( key.versions == null || Object.keys( key.versions ).length === 0 ) {
            return undefined;
        }

        if( keyId != null && key.id !== keyId ) {
            
            return undefined;
        }

        if( keyVersion != null ) {
            return key.versions[keyVersion];
        }
        else {
            return Object.values( key.versions ).pop() as string;
        }
    }

    private _key : SymmetricKey | null | undefined;   

    private  _defaultKey : SymmetricKey | null | undefined;  
    
    private _encryptKeyId? : string;

    private _encryptKeyVersion? : string;

    private _encryptKeyValue? : string;
}


