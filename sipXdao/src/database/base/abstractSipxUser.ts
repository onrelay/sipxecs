import { AbstractUser, CollectionDatabase, log, TextProperty, TextPropertyImpl, TextsProperty, TextsPropertyImpl, User } from "@dao/database";
import { SipxUser } from "../spec/sipxUser";

export  class AbstractSipxUser extends AbstractUser implements SipxUser {  

    constructor( 
        userDocumentName : string,
        userCollection : CollectionDatabase<User>, 
        documentPath? : string  ) {   

        super( userDocumentName, userCollection, documentPath );

        try {

            this.userName = new TextPropertyImpl( this );

            this.pin = new TextPropertyImpl( this );

            this.sipPassword = new TextPropertyImpl( this );

            this.aliases = new TextsPropertyImpl( this );

            this.groups = new TextsPropertyImpl( this );


            //log.traceInOut( "constructor()", KeysCollection ); 

        } catch( error ) {

            log.warn( "constructor()", "Error initializing key", error );
            
            throw new Error( (error as any).message );
        }
    }

    async onCreate() : Promise<void> {

        //log.traceIn( "onCreate()" );

        try {

            await super.onCreate();
    
            //log.traceOut( "onCreate()" );
  
        } catch( error ) {
            
            log.warn( "onCreate()", "Error handling created notification", error );
  
            throw new Error( (error as any).message );
        }
    }

    async onUpdate() : Promise<void> {

        //log.traceIn( "onUpdate()" );

        try {

            await super.onUpdate();
    
            //log.traceOut( "onUpdate()" );
  
        } catch( error ) {
            
            log.warn( "onUpdated()", "Error handling updated notification", error );
  
            throw new Error( (error as any).message );
        }
    }

    async onDelete() : Promise<void> {

        //log.traceIn( "onDelete()" );

        try {

            await super.onDelete();
    
            //log.traceOut( "onDelete()" );
  
        } catch( error ) {
            
            log.warn( "onDelete()", "Error handling deleted notification", error );
  
            throw new Error( (error as any).message );
        }
    }

    fromXml( xml: string ): void {
        const userName = this.extractXmlTag( xml, "userName" ) ?? this.extractXmlTag( xml, "id" ) ?? "";
        const firstName = this.extractXmlTag( xml, "firstName" ) ?? "";
        const lastName = this.extractXmlTag( xml, "lastName" ) ?? "";
        const emailAddress = this.extractXmlTag( xml, "emailAddress" ) ?? "";
        const pin = this.extractXmlTag( xml, "pin" ) ?? "";
        const sipPassword = this.extractXmlTag( xml, "sipPassword" ) ?? "";
        const groups = this.extractXmlTags( xml, "group", "name" );
        const aliases = this.extractXmlTags( xml, "alias", "alias" );

        if( userName.length > 0 ) {
            this.id.setValue( userName );
        }

        this.authId.setValue( userName );
        this.userName.setValue( userName );
        this.firstName.setValue( firstName );
        this.lastName.setValue( lastName );
        this.email.setValue( emailAddress );
        this.pin.setValue( pin );
        this.sipPassword.setValue( sipPassword );
        this.groups.setValue( groups );
        this.aliases.setValue( aliases );
    }

    toXml(): string {
        const userName = this.userName.value() ?? this.authId.value() ?? "";
        const firstName = this.firstName.value() ?? "";
        const lastName = this.lastName.value() ?? "";
        const emailAddress = this.email.value() ?? "";
        const pin = this.pin.value() ?? "";
        const sipPassword = this.sipPassword.value() ?? "";

        const groupsArray = this.groups.values() ?? [];
        const groupsXml = groupsArray.length > 0
            ? `<groups>${groupsArray.map( g => `<group><name>${g}</name></group>` ).join( "" )}</groups>`
            : "";

        const aliasesArray = this.aliases.values() ?? [];
        const aliasesXml = aliasesArray.length > 0
            ? `<aliases>${aliasesArray.map( a => `<alias><alias>${a}</alias></alias>` ).join( "" )}</aliases>`
            : "";

        return `<?xml version="1.0" encoding="UTF-8"?>
<user>
  <userName>${userName}</userName>
  <firstName>${firstName}</firstName>
  <lastName>${lastName}</lastName>
  <emailAddress>${emailAddress}</emailAddress>
  <pin>${pin}</pin>
  <sipPassword>${sipPassword}</sipPassword>
  ${groupsXml}
  ${aliasesXml}
</user>`;
    }

    private extractXmlTag( xml: string, tag: string ): string | undefined {
        const match = xml.match( new RegExp( `<${tag}>(.*?)</${tag}>`, "s" ) );
        return match ? match[1].trim() : undefined;
    }

    private extractXmlTags( xml: string, containerTag: string, valueTag: string ): string[] {
        const regex = new RegExp( `<${containerTag}>[\\s\\S]*?</${containerTag}>`, "g" );
        const matches = xml.match( regex ) ?? [];
        const results: string[] = [];

        for( const block of matches ) {
            const val = this.extractXmlTag( block, valueTag );
            if( val != null ) {
                results.push( val );
            }
        }

        return results;
    }

    readonly userName : TextProperty;

    readonly pin : TextProperty;

    readonly sipPassword : TextProperty;

    readonly groups : TextsProperty;

    readonly aliases : TextsProperty;
     
}
