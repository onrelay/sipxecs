export const LogLevel = {

    Error : "error",

    Warn : "warn",

    Info : "info",

    Debug : "debug",

    Trace: "trace"

};

export type LogLevel = keyof (typeof LogLevel);   