package com.pipeline.runtime.dsl

import com.pipeline.runtime.dsl.traits.CredentialsManagement
import com.pipeline.runtime.dsl.traits.Shell

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

class StepsExecutor implements Shell, CredentialsManagement {
    private ConcurrentMap<String, Object> dynamicProps
    final ConcurrentMap<String, String> env = [:] as ConcurrentHashMap

    StepsExecutor() {
        dynamicProps = [
                params: [:],
                configFileProvider: this.&defaultMethodClosure,
                ansiColor: this.&defaultMethodClosure,
                container: this.&defaultMethodClosure,
                node: this.&defaultMethodClosure,
                dir: this.&defaultMethodClosure,
                node: this.&defaultMethodClosure,
                withCredentials: this.&defaultMethodClosure,
        ] as ConcurrentHashMap

    }

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

    void setProperty(String propName, val) {
        dynamicProps[propName] = val
    }

    def getProperty(String propName) {
        switch (propName) {
            case "steps":
                return this
            case "env":
                return this.env
            case "credentials":
                return this.getCredentialsStore()
            default:
                return dynamicProps[propName]
        }

    }

    def methodMissing(String methodName, args) {
        def prop = dynamicProps[methodName]
        if (prop instanceof Closure) {
            return prop(*args)
        } else {
            throw new Exception("\u001B[1;31m************ Method Missing with name $methodName and args $args **************\u001B[0m")
        }
    }
}