import { Database } from "../dao/database";
import { DatabaseDocument } from "../dao/databaseDocument";


export type DatabaseAccessFilter = {

    canCreate?: Database<DatabaseDocument>[];

    canRead?: Database<DatabaseDocument>[]; 

    canUpdate?: Database<DatabaseDocument>[];

    canDelete?: Database<DatabaseDocument>[];
};
