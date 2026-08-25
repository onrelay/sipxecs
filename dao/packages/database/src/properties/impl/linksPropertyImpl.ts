import { DatabaseObject } from "../../core/spec/databaseObject";
import { LinksProperty } from "../spec/linksProperty";
import { PropertyTypes, PropertyType } from "../../core/defs/propertyType";
import { MapPropertyImpl } from "./mapPropertyImpl";

export class LinksPropertyImpl extends MapPropertyImpl<string> implements LinksProperty{

    constructor( parent : DatabaseObject ) {

        super( parent, PropertyTypes.Links as PropertyType ); 
    }

    setLink( title : string, url : string ): void { 
        super.setEntry( title, url );
    }

    removeLink( title : string ): boolean { 
        return super.removeEntry( title ); 
    }

}