import { DatabaseAccessType, DatabaseAccessTypes } from "../defs/databaseAccessType";

export class DatabaseAccess {

    constructor( allowList : boolean, allowCreate : boolean, allowRead : boolean, allowUpdate : boolean, allowDelete : boolean ) {

        this.allowList = allowList;

        this.allowCreate = allowCreate;

        this.allowRead = allowRead;

        this.allowUpdate = allowUpdate;

        this.allowDelete = allowDelete;
    }

    databaseAccessTypes() : DatabaseAccessType[] { 

        const databaseAccessTypes : DatabaseAccessType[] = [];

        if( this.allowList ) {
            databaseAccessTypes.push( DatabaseAccessTypes.List );
        }

        if( this.allowCreate ) {
            databaseAccessTypes.push( DatabaseAccessTypes.Create );
        }

        if( this.allowRead ) {
            databaseAccessTypes.push( DatabaseAccessTypes.Read ); 
        }

        if( this.allowUpdate ) {
            databaseAccessTypes.push( DatabaseAccessTypes.Update );
        }

        if( this.allowDelete ) {
            databaseAccessTypes.push( DatabaseAccessTypes.Delete );
        }
        
        return databaseAccessTypes;
    }


    all() : boolean {
        return this.allowList && this.allowCreate && this.allowRead && this.allowUpdate && this.allowDelete;
    }

    none() : boolean {
        return !this.allowList && !this.allowCreate && !this.allowRead && !this.allowUpdate && !this.allowDelete;
    }

    read() : boolean {
        return this.allowRead;
    }

    write() : boolean {
        return this.allowCreate && this.allowUpdate && this.allowDelete;
    }

    readOnly() : boolean {
        return !this.allowCreate && this.allowRead && !this.allowUpdate && !this.allowDelete;
    }

    writeOnly() : boolean {
        return this.allowCreate && !this.allowRead && this.allowUpdate && this.allowDelete;
    }

    static allowAll() : DatabaseAccess {
        return new DatabaseAccess( true, true, true, true, true );
    }

    static allowNone() : DatabaseAccess {
        return new DatabaseAccess( false, false, false, false, false );
    }

    static allowReadOnly() : DatabaseAccess {
        return new DatabaseAccess( true, false, true, false, false );
    }

    static allowWriteOnly() : DatabaseAccess {
        return new DatabaseAccess( true, true, false, true, true );
    }

    static allowUpdate() : DatabaseAccess {
        return new DatabaseAccess( true, false, true, true, false );
    }

    static fromDatabaseAccessTypes( databaseAccessTypes? : DatabaseAccessType[] ) : DatabaseAccess {
        return databaseAccessTypes == null ? DatabaseAccess.allowNone() :
         new DatabaseAccess( 
            databaseAccessTypes.includes( DatabaseAccessTypes.List ), 
            databaseAccessTypes.includes( DatabaseAccessTypes.Create ), 
            databaseAccessTypes.includes( DatabaseAccessTypes.Read ), 
            databaseAccessTypes.includes( DatabaseAccessTypes.Update ), 
            databaseAccessTypes.includes( DatabaseAccessTypes.Delete )            
        );
    }

    static merge( databaseAccess1 : DatabaseAccess, databaseAccess2 : DatabaseAccess ) : DatabaseAccess {

        return new DatabaseAccess(
            databaseAccess1.allowList && databaseAccess2.allowList,
            databaseAccess1.allowCreate && databaseAccess2.allowCreate,
            databaseAccess1.allowRead && databaseAccess2.allowRead,
            databaseAccess1.allowUpdate && databaseAccess2.allowUpdate,
            databaseAccess1.allowDelete && databaseAccess2.allowDelete
        );
    }

    readonly allowList : boolean;

    readonly allowCreate : boolean;

    readonly allowRead : boolean;

    readonly allowUpdate : boolean;

    readonly allowDelete : boolean;

}