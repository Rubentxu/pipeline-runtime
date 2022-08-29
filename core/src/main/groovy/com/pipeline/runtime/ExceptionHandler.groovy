package com.pipeline.runtime



class ExceptionHandler implements Thread.UncaughtExceptionHandler {
    private RunnerLogger log = new RunnerLogger()

    public void uncaughtException(Thread t, Throwable ex) {
        def log = ServiceLocator.getService(ILogger.class)
        List lines = ["Error in ", ex.toString(), ex.getMessage()]
        lines.addAll(ex.getStackTrace().findAll { it.className.contains('com.pipeline.runtime') }.join("\n"))
        log.errorBanner(lines)
    }

}
