package com.pipeline.runtime

import com.pipeline.runtime.interfaces.IConfiguration
import com.pipeline.runtime.interfaces.ILogger

class ExceptionHandler implements Thread.UncaughtExceptionHandler {
    private Logger log = new Logger()

    public void uncaughtException(Thread t, Throwable ex) {
        def log = ServiceLocator.getService(ILogger.class)
        List lines = ["Error in ", ex.toString(), ex.getMessage()]
        lines.addAll(ex.getStackTrace().findAll { it.className.contains('com.pipeline.runtime') }.join("\n"))
        log.errorBanner(lines)
    }

}
