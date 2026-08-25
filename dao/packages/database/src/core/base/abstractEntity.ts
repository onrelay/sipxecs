import { Language, LanguageName, Languages } from "@dao/common";
import { CountryProperty } from "../../properties/spec/countryProperty";
import { DefinitionProperty } from "../../properties/spec/definitionProperty";
import { GenericDatabaseDocument } from "../impl/genericDatabaseDocument";
import { CollectionDatabase } from "../spec/collectionDatabase";
import { User } from "../../documents/spec/user";
import { CountryPropertyImpl } from "../../properties/impl/countryPropertyImpl";
import { DefinitionPropertyImpl } from "../../properties/impl/definitionPropertyImpl";
import { log } from "./abstractDatabaseService";
import { CollectionProperty } from "../../properties/spec/collectionProperty";
import { Key, KeysCollectionName } from "../../documents/spec/key";
import { CollectionPropertyImpl } from "../../properties/impl/collectionPropertyImpl";
import { KeyFormats, KeyStatus, KeyStatuses, KeyTypes } from "@dao/security";
import { securityServiceFactory } from "@dao/security/src/api/securityServiceFactory";
import { TextProperty } from "../../properties/spec/textProperty";
import { TextPropertyImpl } from "../../properties/impl/textPropertyImpl";

export abstract class AbstractEntity extends GenericDatabaseDocument implements User {

    constructor( entityDocumentName : string, entityCollection : CollectionDatabase<User>, documentPath? : string ) {

        super( entityDocumentName, entityCollection, documentPath ); 

        try {
            this.username = new TextPropertyImpl( this );

            this.country = new CountryPropertyImpl( this ); 

            this.language = new DefinitionPropertyImpl<Language>( this, LanguageName, Languages );

            this.keys = new CollectionPropertyImpl<Key>( this, KeysCollectionName );

            //log.traceInOut( "constructor()", OrganizationsCollection );

        } catch( error ) {

            log.warn( "constructor()", "Error initializing entity", error );
            
            throw new Error( (error as any).message );
        }
    }

    async updateKeys() : Promise<boolean> {

        try {
            log.traceIn("updateKeys()", this.title.value() );

            let changed = false;

            const keys = await this.keys.collection().documents();

            let entitySecret : Key | undefined;

            for( const key of keys.values() ) {

                if( key.keyType.value() === KeyTypes.Symmetric ) {
                    entitySecret = key;
                    break;
                }
            }

            if( entitySecret == null ) {

                entitySecret = this.keys.collection().newDocument();

                entitySecret.keyType.setValue( KeyTypes.Symmetric as KeyType );

                entitySecret.keyFormat.setValue( KeyFormats.AES256 as KeyFormat );

                entitySecret.keyStatus.setValue( KeyStatuses.Requested as KeyStatus );

                entitySecret.keyVault.setValue( securityServiceFactory!.get().keyManager?.keyVault );
            }

            switch( entitySecret.keyStatus.value() )
            {
                case KeyStatuses.Requested:
                {
                    const symmetricKey = await securityServiceFactory!.get().keyManager?.createSymmetricKey( 
                        this.username.value()!, 
                        entitySecret.secretKey.value(),
                        this.title.value() == null ? undefined : 
                        {
                            "entity": this.title.value()!.replace(" ", "-").toLowerCase() 
                        } );
    
                    if( symmetricKey != null ) {
    
                        entitySecret.keyStatus.setValue( KeyStatuses.Enabled as KeyStatus );
    
                        const versionNumber = Object.keys( symmetricKey.versions ).pop()!;
    
                        entitySecret.version.setValue( +versionNumber );
    
                        if( entitySecret.id.value() == null ) {
                            await entitySecret.create();
                        }
                        else {
                            await entitySecret.update();
                        }

                        changed = true;
                    }
                    break;
                }

                case KeyStatuses.Enabled:
                {
                    const symmetricKey = await securityServiceFactory!.get().keyManager?.symmetricKey( this.username.value()! );
    
                    if( symmetricKey != null && symmetricKey.status !== KeyStatuses.Enabled ) {
    
                        securityServiceFactory!.get().keyManager!.enableSymmetricKey( this.username.value()! );

                        changed = true;
                    }
                    break;
                }

                case KeyStatuses.Disabled:
                {
                    const symmetricKey = await securityServiceFactory!.get().keyManager?.symmetricKey( this.username.value()! );
    
                    if( symmetricKey != null && symmetricKey.status !== KeyStatuses.Disabled ) {
    
                        securityServiceFactory!.get().keyManager!.disableSymmetricKey( this.username.value()! );

                        changed = true;
                    }
                    break;
                }
            }

            log.traceOut("updateOrganizationKeys()", {changed} );
            return changed;

        } catch (error) {
            log.warn("Error updating organization keys", error );

            return false;
        }
    }

    async disableKeys() : Promise<boolean> {

        try {
            log.traceIn("disableKeys()", this.title.value() );

            let changed = false;

            const keys = await this.keys.collection().documents();

            let entitySecret : Key | undefined;

            for( const key of keys.values() ) {

                if( key.keyType.value() === KeyTypes.Symmetric ) {
                    entitySecret = key;
                    break;
                }
            }

            if( entitySecret == null ) {

                log.traceOut("disableOrganizationKeys()", "not found" );
                return false;
            }

            switch( entitySecret.keyStatus.value() )
            {
                case KeyStatuses.Enabled:
                {
                    const symmetricKey = await securityServiceFactory!.get().keyManager?.symmetricKey( this.id.value()! );
        
                    if( symmetricKey != null && symmetricKey.status !== KeyStatuses.Disabled  ) {
    
                        securityServiceFactory!.get().keyManager!.disableSymmetricKey( this.id.value()! );
                        changed = true;
                    }
                    break;
                }
            }

            log.traceOut("disableOrganizationKeys()" );
            return changed;

        } catch (error) {
            log.warn("Error disabling organization keys", error );

            return false;
        }
    }

    readonly username : TextProperty;
 
    readonly country : CountryProperty;

    readonly language : DefinitionProperty<Language>; 
    
    readonly keys : CollectionProperty<Key>;
    
}
