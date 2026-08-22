import { ServiceFactory } from "@sipxdao/common"
import { ConfigurationService } from "./configurationService";
import { ConfigurationManager } from "./configurationManager";
import { ConfigurationServiceImpl } from "../internal/configurationServiceImpl";

export let configurationServiceFactory : ConfigurationServiceFactory | undefined;

export class ConfigurationServiceFactory extends ServiceFactory<ConfigurationService> {

    static create( configurationManager : ConfigurationManager ) : void {

        const configurationService = new ConfigurationServiceImpl( configurationManager );

        configurationServiceFactory = new ConfigurationServiceFactory();

        configurationServiceFactory.init( configurationService );
    }
}
