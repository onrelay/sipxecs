import { DatabaseDocument } from "./databaseDocument";
import { CollectionDatabase } from "./collectionDatabase";
import { TemplateProperty } from "../properties/templateProperty";
import { Template } from "./template";
import { SubdocumentProperty } from "../properties/subdocumentProperty";
import { TemplatedProperties } from "./templatedProperties";
import { TemplatePropertyImpl } from "../../internal/properties/templatePropertyImpl";
import { GenericDatabaseDocument } from "./genericDatabaseDocument";
import { TemplatedDocument } from "./templatedDocument";
import { log } from "./abstractDatabaseService";
import { ReferenceHandle } from "./referenceHandle";


export abstract class AbstractTemplatedDocument extends GenericDatabaseDocument implements TemplatedDocument {

    constructor( documentName : string,
        collectionDatabase : CollectionDatabase<DatabaseDocument>,
        documentPath? : string
        ) {

        super( documentName, collectionDatabase, documentPath );

        try {
            //log.traceIn( "constructor()", collectionDatabase, documentPath );

            this.template = new TemplatePropertyImpl<Template<TemplatedDocument>>( this );
            this.template.trackChanges = false;

            //log.traceOut( "constructor()");

        } catch( error ) {
                
            log.warn(  "("+documentName+")", "constructor()", "Error creating database document", error );

            throw new Error( (error as any).message );
        }
    }

    templatePath() : string | undefined {
        return this.template?.value()?.path; 
    }

    templateUri() : string | undefined {
        return this.template?.value()?.uri; 
    }  


    async onCreated(): Promise<void> {

        try {
          //log.traceIn( "onCreated()" );

          await super.onCreated();

          if( this.template.value() != null ) {

            const template = await this.template.document();

            if( template == null ) {
                log.warn("Document is not templated");
                return;
            }

            template.instances.setDocument( this.referenceHandle() as ReferenceHandle<TemplatedDocument>);

            await template.update();
          }

          //log.traceOut( "onCreated()" );
    
        } catch (error) {
    
          log.warn("onCreated()", "Error handling created notification", error);
    
          throw new Error( (error as any).message );
        }
    }

    readonly template : TemplateProperty<Template<TemplatedDocument>>;

    templatedProperties? : SubdocumentProperty<TemplatedProperties>

  
}
