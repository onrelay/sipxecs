export interface PersistentState {

    property(key: string): any | undefined;

    setProperty(key: string, value: any | undefined): void;

    clearProperty(key: string): void;
}