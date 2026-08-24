import { Authorization, AuthorizationType, GenericAuthorizationManager, NamespaceSeparator } from "@dao/authorization";

export class DatabaseAuthorizationManager extends GenericAuthorizationManager {

    constructor( databaseUri : string, authorizationType : AuthorizationType ) {

        super( {
                namespace: DatabaseAuthorizationManager.namespaceFromUri( databaseUri ),
                key: DatabaseAuthorizationManager.keyFromUri( databaseUri ),
                authorizationType: authorizationType
        }  as Authorization );
    }

    authorizes( authorizationType : AuthorizationType, databaseUri : string ) : boolean {

        return super.authorizes(
            authorizationType,
            DatabaseAuthorizationManager.namespaceFromUri( databaseUri ),
            DatabaseAuthorizationManager.keyFromUri( databaseUri ) );
    }

    static namespaceFromUri( databaseUri : string ) : string {

        const path = databaseUri.split( "?" )[0];

        const pathElements = path.split( "/" );

        let namespace = "";

        const lastCollectionElement = pathElements.length % 2 === 0 ?
            pathElements.length - 2 : pathElements.length - 1;

        for( let index = 0; index <= lastCollectionElement; index++ ) {

            if( pathElements[index].length === 0 ) {
                continue;
            }

            namespace += namespace.length > 0 ? NamespaceSeparator : "";
            namespace += pathElements[index];
        }

        return namespace;
    }

    static keyFromUri( databaseUri : string ) : string | undefined {

        const path = databaseUri.split( "?" )[0];

        const pathElements = path.split( "/" );

        let pathElementCount = 0;

        for( const pathElement of pathElements ) {

            if( pathElement.length > 0 ) {
                pathElementCount++;
            }
        }

        if( pathElementCount % 2 !== 0 ) {
            return undefined;
        }

        for( let index = pathElements.length - 1; index >= 0; index-- ) {

            if( pathElements[index].length > 0 ) {
                return pathElements[index];
            }
        }

        return undefined;
    }
}
