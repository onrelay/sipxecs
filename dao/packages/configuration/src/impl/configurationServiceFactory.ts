import { ServiceFactory } from "@dao/common"
import { ConfigurationService } from "../spec/configurationService";
import { ConfigurationManager } from "../spec/configurationManager";
import { ConfigurationServiceImpl } from "./configurationServiceImpl";

export let configurationServiceFactory : ConfigurationServiceFactory | undefined;

export class ConfigurationServiceFactory extends ServiceFactory<ConfigurationService> {

    static create( configurationManager : ConfigurationManager ) : void {

        const configurationService = new ConfigurationServiceImpl( configurationManager );

        configurationServiceFactory = new ConfigurationServiceFactory();

        configurationServiceFactory.init( configurationService );
    }
}
