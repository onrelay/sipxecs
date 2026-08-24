export declare const LogLevels: {
    readonly Error: "error";
    readonly Warn: "warn";
    readonly Info: "info";
    readonly Debug: "debug";
    readonly Trace: "trace";
};
export type LogLevel = (typeof LogLevels)[keyof typeof LogLevels];
//# sourceMappingURL=logLevel.d.ts.map