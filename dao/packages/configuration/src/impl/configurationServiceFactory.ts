import { Context, ServiceFactory } from "@dao/common"
import { ConfigurationService } from "../spec/configurationService";
import { ConfigurationServiceImpl } from "./configurationServiceImpl";

export let configurationServiceFactory : ConfigurationServiceFactory | undefined;

export class ConfigurationServiceFactory extends ServiceFactory<ConfigurationService> {

    static create( context : Context ) : void {

        const configurationService = new ConfigurationServiceImpl( context );

        configurationServiceFactory = new ConfigurationServiceFactory();

        configurationServiceFactory.init( configurationService );
    }
}
