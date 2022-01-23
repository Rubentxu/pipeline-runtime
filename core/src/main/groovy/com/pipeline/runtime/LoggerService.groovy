package com.pipeline.runtime

import com.pipeline.runtime.interfaces.IConfiguration
import com.pipeline.runtime.interfaces.ILoggerService

class LoggerService implements ILoggerService {

    static final String NORMAL = "\u001B[0m"
    static final String BOLD = "\u001B[1m"
    static final String ITALIC = "\u001B[3m"
    static final String UNDERLINE = "\u001B[4m"
    static final String BLINK = "\u001B[5m"
    static final String RAPID_BLINK = "\u001B[6m"
    static final String REVERSE_VIDEO = "\u001B[7m"
    static final String INVISIBLE_TEXT = "\u001B[8m"
    static final String BLACK = "\u001B[30m"
    static final String RED = "\u001B[31m"
    static final String GREEN = "\u001B[32m"
    static final String YELLOW = "\u001B[33m"
    static final String BLUE = "\u001B[34m"
    static final String MAGENTA = "\u001B[35m"
    static final String CYAN = "\u001B[36m"
    static final String WHITE = "\u001B[37m"
    static final String DARK_GRAY = "\u001B[1;30m"
    static final String LIGHT_RED = "\u001B[1;31m"
    static final String LIGHT_GREEN = "\u001B[1;32m"
    static final String LIGHT_YELLOW = "\u001B[1;33m"
    static final String LIGHT_BLUE = "\u001B[1;34m"
    static final String LIGHT_PURPLE = "\u001B[1;35m"
    static final String LIGHT_CYAN = "\u001B[1;36m"

    def LOG_PATTERN = '[%s] %s'

    def LEVEL_NUMBERS = [
        'FATAL': 100,
        'ERROR': 200,
        'WARN' : 300,
        'INFO' : 400,
        'DEBUG': 500
    ]

    private IConfiguration configuration

    public LoggerService(IConfiguration configuration) {
        this.configuration = configuration
    }

    @Override
    void log(String level, String message) {
        String configLevel = configuration.getValueOrDefault('global.logLevel','INFO')

        if (LEVEL_NUMBERS[level] <= LEVEL_NUMBERS[configLevel]) {
            def logPattern = LOG_PATTERN
            switch (level) {
                case ["FATAL", "ERROR"]:
                    logPattern = "$RED [%s] %s $BOLD"
                    break
                case "WARN":
                    logPattern = "$YELLOW [%s] %s $BOLD"
                    break
                case "INFO":
                    logPattern = "$GREEN [%s] %s $NORMAL"
                    break
                case "DEBUG":
                    logPattern = "$MAGENTA [%s] %s $ITALIC"
                    break
                case"METRIC":
                    logPattern = "$LIGHT_BLUE [%s] %s $ITALIC"
                    break
            }
            def msg = String.format(logPattern as String, level, message)
            println msg
        }
    }

    @Override
    void info(String message) {
        log('INFO', message)
    }

    @Override
    void warn(String message) {
        log('WARN', message)
    }

    @Override
    void debug(String message) {
        log('DEBUG', message)
    }

    @Override
    void error(String message) {
        log('ERROR', message)
    }

    @Override
    void fatal(String message) {
        log('FATAL', message)
    }

    @Override
    void metric(String message) {
        log('METRIC', message)
    }

    @Override
    def prettyPrint(levelLog, obj) {
        log(levelLog,prettyPrintExtend(obj, 0, new StringBuilder()).toString())
    }

    def prettyPrintExtend(obj, level = 0, sb = new StringBuilder()) {

        def indent = { lev -> sb.append("  " * lev) }
        if(obj instanceof Map){
            sb.append("{\n")
            obj.each{ name, value ->
                if(name.contains('.')) return // skip keys like "a.b.c", which are redundant
                indent(level+1).append(name)
                (value instanceof Map) ? sb.append(" ") : sb.append(" = ")
                prettyPrintExtend(value, level+1, sb)
                sb.append("\n")
            }
            indent(level).append("}")
        }
        else if(obj instanceof List){
            sb.append("[\n")
            obj.each{ value ->
                indent(level+1)
                prettyPrintExtend(value, level+1, sb).append(",")
                sb.append("\n")
            }
            indent(level).append("]")
        }
        else if(obj instanceof String){
            sb.append('"').append(obj).append('"')
        }
        else {
            sb.append(obj)
        }
    }

    def echoBanner(logLevel, def ... msgs) {
        log(logLevel, createBanner(msgs))
    }

    def errorBanner(def ... msgs) {
        log('ERROR',createBanner(msgs))
    }

    def createBanner(def ... msgs) {
        return """
       ===========================================

       ${msgFlatten(null, msgs).join("\n        ")}

       ===========================================
   """
    }

// flatten function hack included in case Jenkins security
// is set to preclude calling Groovy flatten() static method
// NOTE: works well on all nested collections except a Map
    def msgFlatten(def list, def msgs) {
        list = list ?: []
        if (!(msgs instanceof String) && !(msgs instanceof GString)) {
            msgs.each { msg ->
                list = msgFlatten(list, msg)
            }
        }
        else {
            list += msgs
        }

        return  list
    }
}
