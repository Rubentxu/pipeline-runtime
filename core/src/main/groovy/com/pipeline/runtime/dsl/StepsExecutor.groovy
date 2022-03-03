package com.pipeline.runtime.dsl

import com.pipeline.runtime.ServiceLocator
import com.pipeline.runtime.interfaces.IConfiguration
import com.pipeline.runtime.interfaces.ILogger
import groovy.transform.CompileStatic

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

//@CompileStatic
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
    ILogger log

    StepsExecutor() {
        this.configuration = ServiceLocator.getService(IConfiguration.class)
        this.log = ServiceLocator.getService(ILogger.class)
        ServiceLocator.loadService(Steps.class, this)
    }

    IConfiguration getConfiguration() {
        return this.configuration
    }

    ILogger getLog() {
        return this.log
    }

    def defaultMethodClosure(_, closure) {
        log.debug 'Get default method Closure'
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
        return super.run()
    }


    def initialize() {
        println "Run StepEXECUTOR"

        this.initializeWorkspace(configuration)
        this.credentials.addAll(configuration.getValueOrDefault('credentials',[:]))
        this.env.putAll(configuration.getValueOrDefault('pipeline.environmentVars',[:]))
        log.debug("Credentials ... ${this.credentials}")
        this.configureScm()
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