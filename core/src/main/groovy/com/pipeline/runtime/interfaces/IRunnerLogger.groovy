package com.pipeline.runtime.interfaces

interface IRunnerLogger {

    void setLogLevel(String level)

    void log(String level, String message)

    void info(String message)

    void warn(String message)

    void debug(String message)

    void error(String message)

    void fatal(String message)

    void system(String message)

    void whenDebug(Closure body)

    void echoBanner(String level, List<String> messages)

    void errorBanner(List<String> msgs)

    def <T> void prettyPrint(String level, T obj)
}
