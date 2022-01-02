package mocks

import com.pipeline.runtime.dsl.StepsExecutor

import java.util.concurrent.ConcurrentHashMap

class StepsExecutorMock extends ConcurrentHashMap {
    static private dynamicProps = [
            env               : [WORKSPACE: "./"],
            params            : [:],
            out               : {},
            configFileProvider: this.&defaultMethodClosure,
            ansiColor         : this.&defaultMethodClosure,
            container         : this.&defaultMethodClosure,
            node              : this.&defaultMethodClosure,
            dir               : this.&defaultMethodClosure,
            withCredentials   : this.&defaultMethodClosure,

    ]
    static private Map callTraces = [:]

    static def defaultMethodClosure(_, closure) {
        closure.delegate = this
        return closure()
    }

    static void echo(StepsExecutor self, String message) {
        println message
    }

    static void error(StepsExecutor self, String message) {
        throw new Exception(message)
    }

    static void node(StepsExecutor self, Closure body) {
        body.delegate = this
        body()
    }

    static void setProperty(StepsExecutor self, String propName, val) {
        dynamicProps[propName] = val
    }

    static def propertyMissing(StepsExecutor self,String prop) {
        return dynamicProps[prop]
    }

    static def methodMissing(StepsExecutor self, String methodName, args) {
        if (!callTraces.containsKey(methodName)) {
            callTraces[methodName] = []
        }
        callTraces[methodName] << [args: args, number: callTraces[methodName].size() + 1 as Integer]

        def prop = dynamicProps[methodName]
        if (prop instanceof Closure) {
            return prop(*args)
        } else {
            throw new Exception("\u001B[1;31m************ Method Missing with name $methodName and args $args **************\u001B[0m")
        }
    }

    static def verifyMethod(StepsExecutor self, String methodName, int number) {
        def calls = callTraces[methodName]
        def args = calls.find { it.number == number }.args
        return args[0]
    }

    static def verifyMethod(StepsExecutor self, String methodName, int number, Closure body) {
        def calls = callTraces[methodName]
        def args = calls.find { it.number == number }.args
        return body(args[0])
    }


}
