export const Environments = {

    Client: "client",

    Server:  "server"

} as const

export type Environment = (typeof Environments)[keyof typeof Environments];

export const ClientEnvironment = Environments.Client;

export const ServerEnvironment = Environments.Server;


 