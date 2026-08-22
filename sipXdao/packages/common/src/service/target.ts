
export const Targets = {

    Emulator                : "emulator",

    Development             : "development",

    QA                      : "qa",

    Production              : "production"
}

export type Target = keyof (typeof Targets);

export const EmulatorTarget = Targets.Emulator as Target;

export const DevelopmentTarget = Targets.Development as Target;

export const QATarget = Targets.QA as Target;

export const ProductionTarget = Targets.Production as Target;



