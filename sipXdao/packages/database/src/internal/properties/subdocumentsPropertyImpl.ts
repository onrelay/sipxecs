import { UniqueId } from "@sipxdao/common";
import { AbstractDatabaseProperty } from "../../api/dao/abstractDatabaseProperty";
import { DatabaseObject } from "../../api/dao/databaseObject";
import { DatabaseSubdocument } from "../../api/dao/databaseSubdocument";
import { SubdocumentsProperty } from "../../api/properties/subdocumentsProperty";
import { PropertyTypes, PropertyType } from "../../api/types/propertyType";

export class SubdocumentsPropertyImpl<DerivedSubdocument extends DatabaseSubdocument> 
    extends AbstractDatabaseProperty<Map<string,DerivedSubdocument>> implements SubdocumentsProperty<DerivedSubdocument> {

    constructor( parent : DatabaseObject, onNewSubdocument : () => DerivedSubdocument ) { 
        
        super( parent, PropertyTypes.Subdocuments as PropertyType ); 

        this._onNewSubdocument = onNewSubdocument; 
    } 

    newSubdocument() : DerivedSubdocument {
        
        return this._onNewSubdocument!();
    }

    setSubdocument( subdocument : DerivedSubdocument ): void {  

        const oldSubdocuments = this.subdocuments();

        const newSubdocuments = new Map<string,DerivedSubdocument>( oldSubdocuments );

        const subdocumentId = subdocument.id.value() == null ? UniqueId.get(): 
            subdocument.id.value()!;

        newSubdocuments.set( subdocumentId, subdocument );

        if( this.onChange( oldSubdocuments, newSubdocuments) ) {

            if( subdocument.id.value() == null ) {

                subdocument.id.setValue( subdocumentId );
            }

            this._map = newSubdocuments;

            this.onChanged( oldSubdocuments, newSubdocuments);
        }
    }

    removeSubdocument( subdocumentId : string): boolean {  

        const oldSubdocuments = this.subdocuments();

        let newSubdocuments : Map<string,DerivedSubdocument> | undefined = 
            new Map<string,DerivedSubdocument>( oldSubdocuments );

        if( newSubdocuments.delete( subdocumentId ) ) {

            if( newSubdocuments.size === 0 ) {
                newSubdocuments = undefined;
            }

            if( this.onChange( oldSubdocuments, newSubdocuments ) ) {

                this._map = newSubdocuments;

                this.onChanged( oldSubdocuments, newSubdocuments )

                return true;
            }
        }

        return false; 
    }
    
    subdocuments() : Map<string,DerivedSubdocument> | undefined {

        if( this._map == null || this._map.size === 0 ) {
            return undefined;
        }

        return this._map;
    }

    value() {
        return this.subdocuments();
    }

    setValue( value : Map<string,DerivedSubdocument> | undefined ) : void {

        const oldValue = new Map<string,DerivedSubdocument>( this._map ); 

        if( this.onChange( oldValue, value ) ) {

            this._map = value;

            this.onChanged( oldValue, value );
        }
    }

    fromRecord( documentData: Record<string, any>): void {

        if( this._map != null ) {
            delete this._map;
        }

        const data = documentData[this.key()];

        if( data == null ) {
            return;
        }

        for( const entry of Object.entries( data )  ) {

            const key = entry[0];

            const subdocumentData = entry[1] as Record<string, any>;

            const subdocument = this._onNewSubdocument();

            subdocument.fromRecord( subdocumentData );

            if( this._map == null ) {
                this._map = new Map<string,DerivedSubdocument>();
            }

            this._map.set( key, subdocument ); 
        }
    }

    async toRecord( documentData: Record<string, any>, force? : boolean ) : Promise<void> {

        if( this._map == null || this._map.size === 0 ) {
            return;
        }

        const data = {} as any;

        for( const subdocumentEntry of this._map ) {

            data[subdocumentEntry[0]] = await subdocumentEntry[1].toRecord( force );
        }

        documentData[this.key()] = data;
    }

    compareTo( other : SubdocumentsProperty<DerivedSubdocument> ) : number {

        return this.compareValue( other.value() );

    }

    compareValue( otherSubdocuments : Map<string,DerivedSubdocument> | undefined ) : number {

        const subdocuments = this.value();

        if( subdocuments == null && otherSubdocuments == null ) {
            return 0;
        }

        if( subdocuments != null && otherSubdocuments == null ) {
            return 1;
        }

        if( subdocuments == null && otherSubdocuments != null ) {
            return -1;
        }

        if( subdocuments!.size !== otherSubdocuments!.size ) {
            return subdocuments!.size - otherSubdocuments!.size ;
        }

        const subdocumentsArray = Array.from( subdocuments!.values() );
        const otherSubdocumentsArray = Array.from( otherSubdocuments!.values() );

        for( let i = 0; i < subdocumentsArray.length; i++ ) {

            const compare = subdocumentsArray[i].compareTo( otherSubdocumentsArray[i] );

            if( compare !== 0 ) {
                return compare;
            }

        }

        return 0;

    }

    includes( other : SubdocumentsProperty<DerivedSubdocument>  ) : boolean {
        return this.includesValue( other.value() );
    }

    includesValue( value : Map<string,DerivedSubdocument> | undefined ) : boolean {
        return this.compareValue( value ) === 0;
    }

    private readonly _onNewSubdocument : () => DerivedSubdocument;

    protected _map : Map<string,DerivedSubdocument> | undefined;


}