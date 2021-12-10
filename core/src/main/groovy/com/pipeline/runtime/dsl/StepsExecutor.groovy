package com.pipeline.runtime.dsl


import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

@Singleton
class StepsExecutor extends Script {
    private ConcurrentMap<String, Object> dynamicProps = [
            configFileProvider: this.&defaultMethodClosure,
            ansiColor         : this.&defaultMethodClosure,
            container         : this.&defaultMethodClosure,
            node              : this.&defaultMethodClosure,
            dir               : this.&defaultMethodClosure,
            withCredentials   : this.&defaultMethodClosure,
    ] as ConcurrentHashMap


    def defaultMethodClosure(_, closure) {
        closure.delegate = this
        return closure()
    }

    def getSteps() {
        println "getSteps"
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