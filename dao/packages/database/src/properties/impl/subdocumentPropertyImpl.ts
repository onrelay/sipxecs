import { AbstractDatabaseProperty } from "../../core/base/abstractDatabaseProperty";
import { DatabaseObject } from "../../core/spec/databaseObject";
import { DatabaseSubdocument } from "../../core/spec/databaseSubdocument";
import { SubdocumentProperty } from "../spec/subdocumentProperty";
import { PropertyTypes, PropertyType } from "../../core/defs/propertyType";

export class SubdocumentPropertyImpl<DerivedSubdocument extends DatabaseSubdocument> 
    extends AbstractDatabaseProperty<DerivedSubdocument> implements SubdocumentProperty<DerivedSubdocument> {

    constructor( parent : DatabaseObject, onNewSubdocument : () => DerivedSubdocument  ) { 
        super( parent, PropertyTypes.Subdocument as PropertyType ); 

        this._onNewSubdocument = onNewSubdocument;

        this._subdocument = onNewSubdocument();
    }

    value() {
        return this.subdocument();
    }

    setValue( subdocument : DerivedSubdocument | undefined ) : void {

        const oldSubdocument = this._subdocument;

        const newSubdocument = this._onNewSubdocument();

        if( subdocument != null ) { 

            this._subdocument = subdocument;
        }
        
        if( this.onChange( oldSubdocument, newSubdocument )) {

            this._subdocument = newSubdocument;

            this.onChanged( oldSubdocument, newSubdocument ) 
        }

    }

    subdocument() : DerivedSubdocument | undefined {
        return this._subdocument;
    }

    fromRecord( documentData: Record<string, any>): void {

        const data = documentData[this.key()];

        if( this._subdocument == null && data != null ) {

            this._subdocument = this._onNewSubdocument();

            this._subdocument.fromRecord( data );

        }
        else if( this._subdocument != null  ) {

            this._subdocument.fromRecord( data );
        }
    }

    async toRecord( documentData: Record<string, any>, force? : boolean ) : Promise<void> {

        documentData[this.key()] = await this._subdocument?.toRecord( force ); 
    }

    compareTo( other : SubdocumentProperty<DerivedSubdocument> ) : number {

        return this.compareValue( other.subdocument() );
    }

    compareValue( value : DerivedSubdocument | undefined ) : number {

        if( value == null && this._subdocument == null ) {
            return 0;
        }        
        if( value == null && this._subdocument != null ) {
            return 1;
        }
        if( value != null && this._subdocument == null ) {
            return 1;
        }

        return this._subdocument!.compareTo( value );
    }

    includes( other : SubdocumentProperty<DerivedSubdocument>  ) : boolean {
        return this.includesValue( other.value() );
    }

    includesValue( value : DerivedSubdocument | undefined ) : boolean {
        return this.compareValue( value ) === 0; 
    }

    protected _subdocument? : DerivedSubdocument;

    protected readonly _onNewSubdocument : () => DerivedSubdocument;

}