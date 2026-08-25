import { DatabaseDocument } from "../spec/databaseDocument";
import { CollectionDatabase } from "../spec/collectionDatabase";
import { TemplateProperty } from "../../properties/spec/templateProperty";
import { Template } from "../../documents/spec/template";
import { SubdocumentProperty } from "../../properties/spec/subdocumentProperty";
import { TemplatedProperties } from "../spec/templatedProperties";
import { TemplatePropertyImpl } from "../../properties/impl/templatePropertyImpl";
import { GenericDatabaseDocument } from "../impl/genericDatabaseDocument";
import { TemplatedDocument } from "../spec/templatedDocument";
import { log } from "./abstractDatabaseService";
import { ReferenceHandle } from "../impl/referenceHandle";


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
