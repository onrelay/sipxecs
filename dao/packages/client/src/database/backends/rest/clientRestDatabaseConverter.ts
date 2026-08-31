    export class ClientRestDatabaseConverter {
        toDate(value?: unknown): Date | undefined {
            return value instanceof Date ? value : undefined;
        }

        fromDate(value?: Date): unknown {
            return value;
        }

        toGeolocation(value?: unknown): unknown {
            return value;
        }

        fromGeolocation(value?: unknown): unknown {
            return value;
        }
    }