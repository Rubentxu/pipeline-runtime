package com.pipeline.runtime.interfaces

interface ILogger {
    void log(String level, String message)
    void info(String message)
    void warn(String message)
    void debug(String message)
    void error(String message)
    void fatal(String message)
    void metric(String message)
    def echoBanner(logLevel, def ... msgs)
    def errorBanner(def ... msgs)
    def prettyPrint(levelLog, obj)
}
