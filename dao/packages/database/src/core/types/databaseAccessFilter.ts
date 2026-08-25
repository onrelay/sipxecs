import { Database } from "../spec/database";
import { DatabaseDocument } from "../spec/databaseDocument";


export type DatabaseAccessFilter = {

    canCreate?: Database<DatabaseDocument>[];

    canRead?: Database<DatabaseDocument>[]; 

    canUpdate?: Database<DatabaseDocument>[];

    canDelete?: Database<DatabaseDocument>[];
};
