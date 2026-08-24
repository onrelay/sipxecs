export const BasicPropertyTypes = {
    Text:                   "text",
    LongText:               "longText",
    Number:                 "number",
    Boolean:                "boolean",
    Confirmation:           "confirmation",
    PhoneNumber:            "phoneNumber",
    Country:                "country",
} as const

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
} as const

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
} as const


export const PropertyTypes = {
    ...BasicPropertyTypes,
    ...StandardPropertyTypes,
    ...AdvancedPropertyTypes
 } as const

export type PropertyType = (typeof PropertyTypes)[keyof typeof PropertyTypes];  

export const PropertyTypeName = "propertyType";
