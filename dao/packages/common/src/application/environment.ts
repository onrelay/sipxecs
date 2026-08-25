
export const Environments = {

    Client: "client",

    Server:  "server"

}

export type Environment = keyof (typeof Environments);

export const ClientEnvironment = Environments.Client as Environment;

export const ServerEnvironment = Environments.Server as Environment;


 

