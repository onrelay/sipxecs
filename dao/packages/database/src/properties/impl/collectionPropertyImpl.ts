import { Monitor } from "@dao/common";
import { AbstractDatabaseProperty } from "../../core/base/abstractDatabaseProperty";
import { DatabaseDocument } from "../../core/spec/databaseDocument";
import { CollectionProperty } from "../spec/collectionProperty";
import { PropertyType, PropertyTypes } from "../../core/defs/propertyType";
import { Database } from "../../core/spec/database";
import { CollectionDatabase } from "../../core/spec/collectionDatabase";
import { CollectionGroupDatabase } from "../../core/spec/collectionGroupDatabase";
import { DatabaseAccess } from "../../core/impl/databaseAccess";
import { log } from "../../core/base/abstractDatabaseService";
import { databaseServiceFactory } from "../../core/impl/databaseServiceFactory";

export class CollectionPropertyImpl<DerivedDocument extends DatabaseDocument> 
    extends AbstractDatabaseProperty<DerivedDocument> implements CollectionProperty<DerivedDocument> {

    constructor( parent : DatabaseDocument, collectionName : string, allowCollectionGroup? : boolean ) {

        super( parent, PropertyTypes.Collection as PropertyType ); 

        //log.traceIn( "constructor()", parent.title, collectionName );

        try {
            this._collectionName = collectionName;

            this._allowCollectionGroup = allowCollectionGroup;

            //log.traceOut( "constructor()" ); 

        } catch( error ) {
            
            log.warn( "constructor()", "Error initializing document reference", error );

            throw new Error( (error as any).message );
        }
    }

    collectionName() : string {
        return this._collectionName
    }

    allowCollectionGroup() : boolean | undefined {
        return this._allowCollectionGroup;
    }

    value() {
        return undefined;
    }

    setValue( value : any | undefined ) : void {
        log.warn( "setValue()", "not supported for collection property" );
    }

    database() : Database<DerivedDocument> {

        if( !!this._allowCollectionGroup ) {
            return this.collectionGroup()!;
        }
        else {
            return this.collection(); 
        }
    }

    collection() : CollectionDatabase<DerivedDocument> {
        return databaseServiceFactory!.get().databaseFactory.collectionDatabaseFromCollectionName( 
            this.collectionName(), this.parent as DatabaseDocument ) as CollectionDatabase<DerivedDocument>;
    }

    collectionGroup() : CollectionGroupDatabase<DerivedDocument> | undefined {

        if( !this._allowCollectionGroup ) {
            return undefined;
        }

        return databaseServiceFactory!.get().databaseFactory.collectionGroupDatabaseFromCollectionName( 
            this.collectionName(), this.parent as DatabaseDocument ) as CollectionGroupDatabase<DerivedDocument>;
    }

    newDocument(): DerivedDocument | undefined {

        try {

            return this.collection().newDocument();

        } catch( error ) {

            log.warn("path()", "Error creating new document on collection ", error );

            throw new Error( (error as any).message );
        }  
    }

    protected async monitor( newMonitor : Monitor): Promise<void> { 

        throw new Error( "Unsupported" );
    }

    protected async release(): Promise<void> {

        throw new Error( "Unsupported" );
    }


    fromRecord( documentData: Record<string, any>): void {}

    async toRecord( documentData: Record<string, any> ) : Promise<void> {}

    compareTo( other : CollectionProperty<DerivedDocument> ) : number {
        return this._collectionName.localeCompare( (other as CollectionPropertyImpl<DerivedDocument>)._collectionName );
    }

    compareValue( derivedDocument : DerivedDocument | undefined ) : number {

        throw new Error("Unsupported")
    }

    includes( other : CollectionProperty<DerivedDocument>  ) : boolean {
        throw new Error("Unsupported")
    }

    includesValue( derivedDocument : DerivedDocument | undefined ) : boolean {
        throw new Error("Unsupported")
    }

    databaseAccess() : DatabaseAccess {

        return this.database().databaseAccess();
    }


    private readonly _collectionName : string;

    private readonly _allowCollectionGroup? : boolean; 

}
