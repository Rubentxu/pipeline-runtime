package com.pipeline.runtime

import com.cloudbees.groovy.cps.NonCPS
import com.pipeline.runtime.interfaces.IRunnerLogger


class RunnerLogger implements IRunnerLogger  {

    static final String RESET = "\u001B[0m"
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

    def LEVEL_NUMBERS = [
            'FATAL' : 100,
            'ERROR' : 200,
            'WARN'  : 300,
            'INFO'  : 400,
            'DEBUG' : 500,
            'SYSTEM' : 600,
    ]
    protected Script steps
    String logLevel = 'INFO'

    RunnerLogger(String level) {
        setLogLevel(level)
    }

    @Override
    void log(String level, String message) {

        if (!LEVEL_NUMBERS[level]) return
        if (LEVEL_NUMBERS[level] <= LEVEL_NUMBERS[logLevel]) {
            def formatOpts = [
                    color: '',
                    level: level,
                    text : message,
                    style: '',
                    reset: RESET
            ]
            switch (level) {
                case ['FATAL', 'ERROR']:
                    formatOpts.color = RED
                    formatOpts.style = BOLD
                    break
                case 'WARN':
                    formatOpts.color = YELLOW
                    formatOpts.style = BOLD
                    break
                case 'INFO':
                    formatOpts.color = GREEN
                    break
                case 'DEBUG':
                    formatOpts.color = MAGENTA
                    formatOpts.style = ITALIC
                    break
                case 'SYSTEM':
                    formatOpts.color = CYAN
                    formatOpts.style = ITALIC
                    break
            }
            write(formatOpts)
        }
    }

    protected static String formatMessage(Map options) {
        return "${options.color}${options.style}[${options.level}] ${options.text}${options.reset}"
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
    void system(String message) {
        log('SYSTEM', message)
    }

    @Override
    void whenDebug(Closure body) {
        if (logLevel == 'DEBUG') {
            body()
        }
    }

    @Override
    <T> void prettyPrint(String levelLog, T obj) {
        log(levelLog, prettyPrintExtend(obj, 0, new StringBuilder()).toString())
    }

    protected <T> StringBuilder prettyPrintExtend(T obj, level = 0, StringBuilder sb = new StringBuilder()) {

        def indent = { lev -> sb.append("  " * lev) }
        if (obj instanceof Map) {
            sb.append("{\n")
            obj.each { name, value ->
                // if(name.contains('.'))  return // skip keys like "a.b.c", which are redundant
                indent(level + 1).append(name)
                (value instanceof Map) ? sb.append(" ") : sb.append(" = ")
                prettyPrintExtend(value, level + 1, sb)
                sb.append("\n")
            }
            indent(level).append("}")
        } else if (obj instanceof List) {
            sb.append("[\n")
            obj.eachWithIndex { value, index ->
                indent(level + 1)
                def isLatestElement = (index == obj.size() - 1)
                isLatestElement ? prettyPrintExtend(value, level + 1, sb) : prettyPrintExtend(value, level + 1, sb).append(",")
                sb.append("\n")
            }
            indent(level).append("]")
        } else if (obj instanceof String) {
            sb.append('"').append(obj).append('"')
        } else {
            sb.append(obj)
        }
        return sb
    }

    @Override
    void echoBanner(String level, List<String> messages) {
        log(level, createBanner(messages))
    }

    @Override
    void errorBanner(List<String> msgs) {
        error(createBanner(msgs))
    }

    @NonCPS
    static String createBanner(List<String> msgs) {
        return """
        |===========================================
        |${msgFlatten(null, msgs.findAll { !it?.isEmpty() }).join("\n")}
        |===========================================
        """.stripMargin().trim()
    }

    // flatten function hack included in case Jenkins security
    // is set to preclude calling Groovy flatten() static method
    // NOTE: works well on all nested collections except a Map
    @NonCPS
    protected static msgFlatten(def list, def msgs) {
        list = list ?: []
        if (!(msgs instanceof String) && !(msgs instanceof GString)) {
            msgs.each { msg ->
                list = msgFlatten(list, msg)
            }
        } else {
            list += msgs
        }

        return list
    }


    void write(Map formatOpts) {
        def msg = formatMessage(formatOpts)
        println msg
    }
}
