import { GenericDatabaseDocument } from "../../api/dao/genericDatabaseDocument";
import { log } from "../../api/dao/abstractDatabaseService";
import { CollectionDatabase } from "../../api/dao/collectionDatabase";
import { DatabaseProperty } from "../../api/dao/databaseProperty";
import { databaseServiceFactory } from "../../api/dao/databaseServiceFactory";
import { Entity } from "../../api/dao/entity";
import { PropertyDescriptorsName, PropertyDescriptor } from "../../api/dao/propertyDescriptor";
import { Template } from "../../api/dao/template";
import { TemplatedDocument, TemplatedDocumentName } from "../../api/dao/templatedDocument";
import { NumberProperty } from "../../api/properties/numberProperty";
import { ReferenceProperty } from "../../api/properties/referenceProperty";
import { SubdocumentsProperty } from "../../api/properties/subdocumentsProperty";
import { SymbolicCollectionProperty } from "../../api/properties/symbolicCollectionProperty";
import { TextProperty } from "../../api/properties/textProperty";
import { NumberPropertyImpl } from "../properties/numberPropertyImpl";
import { ReferencePropertyImpl } from "../properties/referencePropertyImpl";
import { SubdocumentsPropertyImpl } from "../properties/subdocumentsPropertyImpl";
import { SymbolicCollectionPropertyImpl } from "../properties/symbolicCollectionPropertyImpl";
import { TextPropertyImpl } from "../properties/textPropertyImpl";
import { PropertyDescriptorImpl } from "./propertyDescriptorImpl";


export class TemplateImpl<Document extends TemplatedDocument> extends GenericDatabaseDocument implements Template<TemplatedDocument> {

    constructor(templateDocumentName : string, templatesCollection: CollectionDatabase<Template<TemplatedDocument>>, documentPath?: string) {

        super(templateDocumentName, templatesCollection, documentPath);  

        try { 

            this.instanceOwner = new ReferencePropertyImpl<Entity>( this );

            this.instanceCollectionName = new TextPropertyImpl( this ); 

            this.instanceCollectionName.required = true;

            this.instanceDocumentName = new TextPropertyImpl( this );   

            this.instanceDocumentName.required = true;

            const templateDatabase = () : CollectionDatabase<TemplatedDocument> | undefined  => {

                if( this.instanceCollectionName.value() == null ) {
                    return undefined;
                }
                return databaseServiceFactory!.get().databaseFactory.collectionDatabaseFromCollectionName( 
                    this.instanceCollectionName.value()! ) as CollectionDatabase<TemplatedDocument>;

            }

            (this.instanceDocumentName as TextPropertyImpl).setOnOptions( async () => { 

                if( this.instanceCollectionName.value() == null ) { 
                    return undefined;
                }

                return templateDatabase() == null ? undefined :
                    templateDatabase()!.documentNames() as string[]; 
            });


            this.propertyDescriptors = new SubdocumentsPropertyImpl<PropertyDescriptor<DatabaseProperty<any>>>( this,
                () => new PropertyDescriptorImpl<DatabaseProperty<any>>( this, PropertyDescriptorsName ) 
            );

            this.version = new NumberPropertyImpl( this, 1, 1 );
            this.version.required = true;

            this.instances = new SymbolicCollectionPropertyImpl<TemplatedDocument>( this, 
                    () => [templateDatabase()],
                    "template" ); 


        } catch (error) { 

            log.warn("constructor()", "Error initializing Template", error);

            throw new Error( (error as any).message );
        }
    }

    referenceDateProperty()  {
        return undefined;
    }

    async onCreated() : Promise<void> {
        try {
            //log.traceIn( "("+this.collectionDatabase.collectionName()+")", "onCreated()" );

            await super.onCreated();
    
            //log.traceOut( "("+this.collectionDatabase.collectionName()+")", "onCreated()" );
  
        } catch( error ) {
            
            log.warn( "("+this.collectionDatabase.collectionName()+")", "onCreated()", "Error handling created notification", error );
  
            throw new Error( (error as any).message );
        }
    }

    async onUpdated() : Promise<void> {
        try {
            //log.traceIn( "onUpdated()" );

            await super.onUpdated();
    
            //log.traceOut( "onUpdated()" );
  
        } catch( error ) {
            
            log.warn( "onUpdated()", "Error handling updated notification", error );
  
            throw new Error( (error as any).message );
        }    
    }

    async onDeleted() : Promise<void> {

        try {
            //log.traceIn( "onDeleted()" );

            await super.onDeleted();
    
            //log.traceOut( "onDeleted()" );
  
        } catch( error ) {
            
            log.warn( "onDeleted()", "Error handling updated notification", error );
  
            throw new Error( (error as any).message );
        }  
    }


    readonly instanceOwner : ReferenceProperty<Entity>;

    readonly instanceCollectionName : TextProperty;

    readonly instanceDocumentName : TextProperty;

    readonly propertyDescriptors : SubdocumentsProperty<PropertyDescriptor<DatabaseProperty<any>>>;

    readonly version : NumberProperty;

    readonly instances : SymbolicCollectionProperty<TemplatedDocument>;

}
