import { GenericDatabaseDocument } from "../../core/impl/genericDatabaseDocument";
import { log } from "../../core/base/abstractDatabaseService";
import { CollectionDatabase } from "../../core/spec/collectionDatabase";
import { DatabaseProperty } from "../../core/spec/databaseProperty";
import { databaseServiceFactory } from "../../core/impl/databaseServiceFactory";
import { User } from "../spec/user";
import { PropertyDescriptorsName, PropertyDescriptor } from "../../core/spec/propertyDescriptor";
import { Template } from "../spec/template";
import { TemplatedDocument, TemplatedDocumentName } from "../../core/spec/templatedDocument";
import { NumberProperty } from "../../properties/spec/numberProperty";
import { ReferenceProperty } from "../../properties/spec/referenceProperty";
import { SubdocumentsProperty } from "../../properties/spec/subdocumentsProperty";
import { SymbolicCollectionProperty } from "../../properties/spec/symbolicCollectionProperty";
import { TextProperty } from "../../properties/spec/textProperty";
import { NumberPropertyImpl } from "../../properties/impl/numberPropertyImpl";
import { ReferencePropertyImpl } from "../../properties/impl/referencePropertyImpl";
import { SubdocumentsPropertyImpl } from "../../properties/impl/subdocumentsPropertyImpl";
import { SymbolicCollectionPropertyImpl } from "../../properties/impl/symbolicCollectionPropertyImpl";
import { TextPropertyImpl } from "../../properties/impl/textPropertyImpl";
import { PropertyDescriptorImpl } from "../../core/impl/propertyDescriptorImpl";


export class TemplateImpl<Document extends TemplatedDocument> extends GenericDatabaseDocument implements Template<TemplatedDocument> {

    constructor(templateDocumentName : string, templatesCollection: CollectionDatabase<Template<TemplatedDocument>>, documentPath?: string) {

        super(templateDocumentName, templatesCollection, documentPath);  

        try { 

            this.instanceOwner = new ReferencePropertyImpl<User>( this );

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


    readonly instanceOwner : ReferenceProperty<User>;

    readonly instanceCollectionName : TextProperty;

    readonly instanceDocumentName : TextProperty;

    readonly propertyDescriptors : SubdocumentsProperty<PropertyDescriptor<DatabaseProperty<any>>>;

    readonly version : NumberProperty;

    readonly instances : SymbolicCollectionProperty<TemplatedDocument>;

}
