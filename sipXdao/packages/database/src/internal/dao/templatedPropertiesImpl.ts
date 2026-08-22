import { log } from "../../api/dao/abstractDatabaseService";
import { GenericDatabaseSubdocument } from "../../api/dao/genericDatabaseSubdocument";
import { DatabaseObject } from "../../api/dao/databaseObject";
import { TemplatedProperties, TemplatedPropertiesName } from "../../api/dao/templatedProperties";

export class TemplatedPropertiesImpl extends GenericDatabaseSubdocument implements TemplatedProperties {

    constructor( parent : DatabaseObject, key : string ) {

        super( TemplatedPropertiesName, parent, key );

        try {

            //log.traceInOut( "constructor()" );

        } catch( error ) {

            log.warn( "constructor()", "Error initializing address", error );
            
            throw new Error( (error as any).message );
        }
    }

 }
