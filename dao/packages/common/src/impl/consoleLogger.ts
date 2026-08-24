import { AbstractLogger } from "../base/abstractLogger";
import { LogLevels } from "../defs/logLevel";


export class ConsoleLogger extends AbstractLogger {

    error = this.errorEnabled() ?
        console.error.bind(console.error, "(" + LogLevels.Error + ")", "(" + this.name + ")") : AbstractLogger.ignore.bind(AbstractLogger.ignore,"");

    warn = this.warnEnabled() ?
        console.warn.bind(console.warn, "(" + LogLevels.Warn + ")", "(" + this.name + ")") : AbstractLogger.ignore.bind(AbstractLogger.ignore,"");

    info = this.infoEnabled() ?
        console.info.bind(console.info, "(" + LogLevels.Info + ")", "(" + this.name + ")") : AbstractLogger.ignore.bind(AbstractLogger.ignore,"");

    debug = this.debugEnabled() ? 
        console.debug.bind(console.debug, "(" + LogLevels.Debug + ")", "(" + this.name + ")") : AbstractLogger.ignore.bind(AbstractLogger.ignore,""); 
 
    traceIn = this.traceEnabled() ? 
        console.debug.bind(console.debug, "(" + LogLevels.Trace + ")", "(" + this.name + ")", "--> enter") : AbstractLogger.ignore.bind(AbstractLogger.ignore,"");
    
    traceOut = this.traceEnabled() ? 
        console.debug.bind(console.debug, "(" + LogLevels.Trace + ")", "(" + this.name + ")", "exit <--") : AbstractLogger.ignore.bind(AbstractLogger.ignore,"");

    traceInOut = this.traceEnabled() ? 
        console.debug.bind(console.debug, "(" + LogLevels.Trace + ")", "(" + this.name + ")", "enter <--> exit") : AbstractLogger.ignore.bind(AbstractLogger.ignore,"");
}