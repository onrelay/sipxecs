export const LogLevels = {

    Error : "error",

    Warn : "warn",

    Info : "info",

    Debug : "debug",

    Trace: "trace"

} as const;

export type LogLevel = (typeof LogLevels)[keyof typeof LogLevels];    
