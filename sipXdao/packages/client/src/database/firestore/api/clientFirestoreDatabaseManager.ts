import { Observation, Observations } from "@sipxdao/common";
import { AbstractFirestoreDatabaseManager, CollectionDatabase, CollectionGroupDatabase, Database, DatabaseDocument, databaseServiceFactory, DatabaseTypes, log } from "@sipxdao/database";


export class ClientFirestoreDatabaseManager extends AbstractFirestoreDatabaseManager {

    async monitorDatabase( database : Database<DatabaseDocument> ) : Promise<void> {
        log.traceIn( "("+database.collectionName()+")", database.databaseType, "monitorDatabase()");

        try {
            if( !database.databaseAccess().allowRead ) {
                throw new Error( "No read access to collection " + database.collectionName() );
            }

            let databaseMonitors;

            if( database.databaseType == DatabaseTypes.Collection ) {

                databaseMonitors = this._collectionMonitors;
            }
            else if( database.databaseType == DatabaseTypes.CollectionGroup ) {

                databaseMonitors = this._collectionGroupMonitors;
            }
            else {
                throw new Error( "Database type cannot be monitored " + database.databaseType );
            }

            let databaseMonitor = databaseMonitors.get( database.uri() );

            if( databaseMonitor !== undefined ) {
                log.traceOut( "("+database.collectionName()+")", "monitorDatabase()", "Already monitoring database" );
                return;
            }

            databaseMonitors.set( database.uri(), null ); // placeholder

            databaseMonitor = await this.databaseRecordQuery(database).onSnapshot( 
                async ( snapshot: { docChanges: () => any[]; } ) => {

                    //log.traceIn("(" + collectionDatabase.collectionName()+ ")", "monitorDatabase()", "onShapshot" );

                    const result = new Map<string, DatabaseDocument>();

                    const databaseObserver = this.observer( database );

                    let hasNotified = false;

                    const changes = snapshot.docChanges();

                    for( const change of changes ) {

                        try {
                            const documentPath = database.path() + "/" + change.doc.id;

                            const documentData = change.doc.data();

                            const databaseDocument =
                                await databaseServiceFactory!.get().databaseFactory.documentFromRecord(
                                    documentPath, documentData ) as DatabaseDocument;

                            if (databaseDocument == null) {

                                log.warn("(" + database.collectionName()+ ")", "monitorDatabase()", "could not read document", { documentPath });
                            }
                            else {
                                let observation: Observation;

                                if (change.type === 'added') {

                                    observation = Observations.Create as Observation;
                                }
                                else if (change.type === 'modified') {

                                    observation = Observations.Update as Observation;
                                }
                                else if (change.type === 'removed') {

                                    observation = Observations.Delete as Observation;
                                }
                                else {
                                    throw new Error("Unrecognized change type");
                                }

                                //log.debug("(" + collectionDatabase.collectionName()+ ")", "monitorDatabase()", "onShapshot", "DB change notification");

                                result.set(databaseDocument.uri(), databaseDocument);

                                if (changes.length === 1 || observation !== Observations.Create as Observation) {

                                    if( databaseObserver?.onNotify != null ) {

                                        databaseObserver.onNotify( this, 
                                            observation!,
                                            databaseDocument.uri(),
                                            databaseDocument );

                                            hasNotified = true; 
                                    }
                                }
                            }

                        } catch (error) {
                            log.warn("(" + database.collectionName()+ ")", "monitorDatabase()", "onShapshot", "Error reading snapshot", error);
                        }
                    }    

                    if (!hasNotified) {

                        if( databaseObserver?.onNotify != null ) {
                                            
                            databaseObserver.onNotify( this, 
                                Observations.Create as Observation,
                                database.uri(),
                                result );
                        }
                    }

                //log.traceOut("(" + collectionDatabase.collectionName()+ ")", "monitorDatabase()", "onShapshot", changes.length );
            },
            async ( error : any ) => {
                log.warn( "("+database.collectionName()+")", "monitorDatabase()", "Error monitoring collection", error );
            });
            
            if( databaseMonitors.get(database.uri()) === undefined ) {
                // We have been released while waiting
                databaseMonitor();
            }
            else {
                databaseMonitors.set( database.uri(), databaseMonitor! );
            }
            log.traceOut( "("+database.collectionName()+")", "monitorDatabase()");

        } catch( error ) {
            log.warn( "("+database.collectionName()+")", "monitorDatabase()", "Error monitoring collection", error );
            
            throw new Error( (error as any).message );
        }
    }

    async releaseDatabase( database : Database<DatabaseDocument> ) : Promise<void> {
        
        //log.traceIn( "("+collectionDatabase.collectionName()+")", "releaseDatabase()");

        try {

            let databaseMonitors;

            if( database.databaseType == DatabaseTypes.Collection ) {

                databaseMonitors = this._collectionMonitors;
            }
            else if( database.databaseType == DatabaseTypes.CollectionGroup ) {

                databaseMonitors = this._collectionGroupMonitors;
            }
            else {
                throw new Error( "Database type cannot be monitored " + database.databaseType );
            }

            let databaseMonitor = databaseMonitors.get( database.uri()  );

            if( databaseMonitor === undefined ) {
                //log.traceOut( "("+collectionDatabase.collectionName()+")", "releaseDatabase()", "Not monitoring database" );
                return;
            }

            if( databaseMonitor != null ) {

                databaseMonitor(); // unsubscribes 
            }

            databaseMonitors.delete( database.uri() );

            //log.traceOut( "("+collectionDatabase.collectionName()+")", "releaseDatabase()" );

        } catch( error ) {

            log.warn( "Error stopping database monitoring from firestore for", database.collectionName(), error );
            
            log.traceOut( "("+database.collectionName()+")", "releaseDatabase()", error );
        }
    }

    async monitorDocuments( collectionDatabase : CollectionDatabase<DatabaseDocument>, documentPaths : string[] ) : Promise<void> {

        log.traceIn( "("+collectionDatabase.collectionName()+")", "monitorDocuments()", documentPaths );

        try {
            if( documentPaths != null ) {

                for( const documentPath of documentPaths ) {

                    await this.monitorDocument( collectionDatabase, documentPath );
                }
            }

            log.traceOut( "("+collectionDatabase.collectionName()+")", "monitorDocuments()" );
            
        } catch( error ) {

            log.warn( "Error starting document monitoring for", collectionDatabase.collectionName(), error );
            
            throw new Error( (error as any).message );
        }
    }

    async monitorDocument( collectionDatabase : CollectionDatabase<DatabaseDocument>, documentPath : string ) : Promise<void> {

        log.traceIn( "("+collectionDatabase.collectionName()+")", "monitorDocument()", documentPath );

        try {
            const databaseDocument = collectionDatabase.newDocument( documentPath );

            const documentId = databaseDocument.id.value()!;

            if( documentId == null ) {
                throw new Error( "No valid document ID from path: " + documentPath );
            }

            if( !databaseDocument.databaseAccess().allowRead ) {
                throw new Error( "No read access to document");
            }

            let collectionDocumentMonitors = this._documentMonitors.get( collectionDatabase.uri() );

            if( collectionDocumentMonitors == null ) {

                collectionDocumentMonitors = new Map<string,() => void>();

                this._documentMonitors.set( collectionDatabase.uri(), collectionDocumentMonitors );
            }

            if( collectionDocumentMonitors.has( documentPath ) ) {

                await databaseDocument.read();

                await collectionDatabase.notify( 
                    Observations.Create as Observation, 
                    documentPath, 
                    databaseDocument );

                log.traceOut( "("+collectionDatabase.collectionName()+") Already monitoring document: " + documentPath );
                return;
            }

            let hasInitialResult = false;

            collectionDocumentMonitors.set( collectionDatabase.uri(), null ); // placeholder

            const collectionDocumentMonitor = await this.collectionReference( collectionDatabase ).doc( documentId ).onSnapshot( 
                async (documentData: { exists: null; data: () => any; id: string; }) => {

                //log.debug( "("+collectionDatabase.collectionName()+")", "monitorDocument()", "snapshot", documentData );

                try {
                    if( documentData.exists != null && !documentData.exists ) {
                        throw new Error("Document not found: " + documentPath );
                    }

                    const databaseDocument = 
                        await databaseServiceFactory!.get().databaseFactory.documentFromRecord( 
                            documentPath, documentData.data() ) as DatabaseDocument;

                    if( databaseDocument == null ) {
                        throw new Error( "Could not read document: " + documentPath );
                    }

                    if( !hasInitialResult ) {

                        await collectionDatabase.notify( 
                            Observations.Create as Observation, 
                            documentPath, 
                            databaseDocument );

                        hasInitialResult = true;
                    }
                    else {
                        await collectionDatabase.notify( 
                            Observations.Update as Observation, 
                            documentPath, 
                            databaseDocument );
                    }
                    
                } catch( error ) {
                    log.warn( "("+collectionDatabase.collectionName()+")", "monitorDocument()", "snapshot", error );

                    await collectionDatabase.notify( 
                        Observations.Delete as Observation, 
                        documentPath, 
                        undefined );
                } 
            },
            async ( error : any ) => {
                log.warn( "("+collectionDatabase.collectionName()+")", "monitorDocument()", "Error monitoring document", error );
            });     
            
            collectionDocumentMonitors.set( collectionDatabase.uri(), collectionDocumentMonitor ); 

        } catch( error ) {

            log.warn( "Error stopping documents monitoring from firestore for", collectionDatabase.collectionName(), error );
            
            log.traceOut( "("+collectionDatabase.collectionName()+")", "monitorDocument()", undefined );
            return undefined;
        }
    }
    
    async releaseDocuments( collectionDatabase : CollectionDatabase<DatabaseDocument>, documentPaths : string[] ) : Promise<void> {
       
        //log.traceIn( "("+collectionDatabase.collectionName()+")", "releaseDocuments()", documentPaths );

        try {

            const collectionDocumentMonitors = this._documentMonitors.get( collectionDatabase.uri() );

            if( collectionDocumentMonitors != null ) {
                documentPaths.forEach( documentPath => {

                    if( collectionDocumentMonitors.has( documentPath )) {

                        collectionDocumentMonitors.get( documentPath )!(); // unsubscribes
            
                        collectionDocumentMonitors.delete( documentPath );
                    }    
                } );
            }

            //log.traceOut( "("+collectionDatabase.collectionName()+")", "releaseDocuments()" );

        } catch( error ) {

            log.warn( "Error stopping documents monitoring from firestore for", collectionDatabase.collectionName(), error );
            
            log.traceOut( "("+collectionDatabase.collectionName()+")", "releaseDocuments()", error );
        }

    }

    async releaseAllDocuments( collectionDatabase : CollectionDatabase<DatabaseDocument> ) : Promise<void> {
 
        //log.traceIn( "("+collectionDatabase.collectionName()+")", "releaseAllDocuments()");

        try {

            let collectionDocumentMonitors = this._documentMonitors.get( collectionDatabase.uri() );

            if( collectionDocumentMonitors == null ) {
                //log.traceOut( "("+collectionDatabase.collectionName()+")", "releaseAllDocuments()", "No monitors" );
                return;
            }
            collectionDocumentMonitors.forEach( documentMonitor => {

                documentMonitor();

            } );

            this._documentMonitors.delete( collectionDatabase.uri() );

            //log.traceOut( "("+collectionDatabase.collectionName()+")", "releaseAllDocuments()" );

        } catch( error ) {

            log.warn( "Error stopping all documents monitoring from firestore for", collectionDatabase.collectionName(), error );
            
            log.traceOut( "("+collectionDatabase.collectionName()+")", "releaseAllDocuments()", error );
        }
    }
 }