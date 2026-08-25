export class UniqueId {

    static get() : string {

        return crypto.randomUUID(); 
    }
}