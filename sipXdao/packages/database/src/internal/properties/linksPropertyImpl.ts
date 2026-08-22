import { DatabaseObject } from "../../api/dao/databaseObject";
import { LinksProperty } from "../../api/properties/linksProperty";
import { PropertyTypes, PropertyType } from "../../api/types/propertyType";
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