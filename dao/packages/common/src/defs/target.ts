export const Targets = {

    Emulator                : "emulator",

    Development             : "development",

    QA                      : "qa",

    Production              : "production"
} as const

export type Target = (typeof Targets)[keyof typeof Targets];

export const EmulatorTarget = Targets.Emulator;

export const DevelopmentTarget = Targets.Development;

export const QATarget = Targets.QA;

export const ProductionTarget = Targets.Production;


