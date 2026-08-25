import { log } from "../base/abstractDatabaseService";
import { GenericDatabaseSubdocument } from "./genericDatabaseSubdocument";
import { DatabaseObject } from "../spec/databaseObject";
import { TemplatedProperties, TemplatedPropertiesName } from "../spec/templatedProperties";

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
