export const BasicPropertyTypes = {
    Text:                   "text",
    LongText:               "longText",
    Number:                 "number",
    Boolean:                "boolean",
    Confirmation:           "confirmation",
    PhoneNumber:            "phoneNumber",
    Country:                "country",
}

export const StandardPropertyTypes = { 
    Texts:                  "texts",
    Definition:             "definition",
    Definitions:            "definitions",
    Date:                   "date",
    Image:                  "image",
    Geolocation:            "geolocation",
    Attachments:            "attachments", 
    Links:                  "links",
    Reference:              "reference",
    References:             "references" 
}

export const AdvancedPropertyTypes = {
    Organization:           "organization",
    Owner:                  "owner",
    SymbolicOwners:         "symbolicOwners",
    Collection:             "collection",
    SymbolicCollection:     "symbolicCollection",
    Subdocument:            "subdocument",
    Subdocuments:           "subdocuments",  
    Template:               "template",
    Data:                   "data",
    Map:                    "map",
    Empty:                  "empty"
}


export const PropertyTypes = {
    ...BasicPropertyTypes,
    ...StandardPropertyTypes,
    ...AdvancedPropertyTypes
 }

export type PropertyType = keyof (typeof PropertyTypes);  

export const PropertyTypeName = "propertyType";
