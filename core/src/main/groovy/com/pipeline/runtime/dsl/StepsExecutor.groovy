package com.pipeline.runtime.dsl

import com.pipeline.runtime.interfaces.IConfiguration
import com.pipeline.runtime.interfaces.ILoggerService

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap


class StepsExecutor extends Script implements Steps {
    private ConcurrentMap<String, Object> dynamicProps = [
            configFileProvider: this.&defaultMethodClosure,
            ansiColor         : this.&defaultMethodClosure,
            container         : this.&defaultMethodClosure,
            node              : this.&defaultMethodClosure,
            dir               : this.&defaultMethodClosure,
            withCredentials   : this.&defaultMethodClosure,
    ] as ConcurrentHashMap
    IConfiguration configuration
    ILoggerService logger

    StepsExecutor(IConfiguration configuration, ILoggerService loggerService) {
        this.configuration = configuration
        this.logger = loggerService
    }

    IConfiguration getConfiguration() {
        return this.configuration
    }

    ILoggerService getLogger() {
        return this.logger
    }

    def defaultMethodClosure(_, closure) {
        logger.debug 'Get default method Closure'
        closure.delegate = this
        return closure()
    }

    def getSteps() {
        return this
    }

    void echo(String message) {
        println message
    }

    void error(String message) {
        throw new Exception(message)
    }

    @Override
    Object run() {
        println "Run StepEXECUTOR"
        return null
    }

    def methodMissing(String methodName, args) {
        def prop = dynamicProps[methodName]

        if (prop instanceof Closure) {
            return callClosure(prop, args)
        } else {
            throw new Exception("\u001B[1;31m************ Method Missing with name $methodName and args $args **************\u001B[0m")
        }
    }

    /**
     * Call closure by handling spreading of parameter default values
     *
     * @param closure to call
     * @param args array of arguments passed to this closure call. Is null by default.
     * @return result of the closure call
     */
    Object callClosure(Closure closure, Object[] args = null) {
        if (!args) {
            return closure.call()
        } else if (args.size() > closure.maximumNumberOfParameters) {
            return closure.call(args)
        } else {
            return closure.call(*args)
        }
    }


}