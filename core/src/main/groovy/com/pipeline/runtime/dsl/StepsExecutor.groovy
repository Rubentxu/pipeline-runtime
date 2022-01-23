package com.pipeline.runtime.dsl

import com.pipeline.runtime.interfaces.IConfiguration

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

    StepsExecutor(IConfiguration configuration) {
        this.configuration = configuration
    }

    IConfiguration getConfiguration() {
        return this.configuration
    }

    def defaultMethodClosure(_, closure) {
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


}