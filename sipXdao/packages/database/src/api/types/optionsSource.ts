
export interface OptionsSource  {

    options() : Promise<Map<string,any> | undefined> 
}
