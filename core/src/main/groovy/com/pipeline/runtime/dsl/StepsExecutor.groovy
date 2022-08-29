package com.pipeline.runtime.dsl

import com.pipeline.runtime.extensions.domain.EnvVars
import com.pipeline.runtime.interfaces.Initializable
import com.pipeline.runtime.validations.ValidationCategory

import java.util.Map.Entry
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

import static groovy.lang.Closure.DELEGATE_FIRST

//@CompileStatic
class StepsExecutor extends Script implements Steps, Initializable {
    static final EnvVars env = [*: System.env,] as ConcurrentHashMap
    static final ConcurrentMap<String, Object> params = [:] as ConcurrentHashMap
    public static final Object lock = new Object()

    private ConcurrentMap<String, Object> dynamicProps = [
            configFileProvider: this.&defaultMethodClosure,
            ansiColor         : this.&defaultMethodClosure,
            container         : this.&defaultMethodClosure,
            node              : this.&defaultMethodClosure,
            withCredentials   : this.&defaultMethodClosure,
    ] as ConcurrentHashMap


    def defaultMethodClosure(_, closure) {
        closure.delegate = this
        return closure()
    }

    def getSteps() {
        return this
    }

    @Override
    Object run() {
        return super.run()
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

    @Override
    def initialize(Map configuration) {
        println "Run StepEXECUTOR"
        println(configuration)
        this.initializeWorkspace(configuration)
        use(ValidationCategory) {
            List expandCredentials = configuration.validateAndGet('credentials')
                    .is(List.class)
                    .defaultValueIfInvalid([])
                    .collect {
                        it.collectEntries { key, value ->
                            if (value.contains('${')) {
                                [key, env.expand(value)]
                            } else {
                                [key, value]
                            }
                        }
                    }

            List expandGlobalConfigFiles = configuration.validateAndGet('globalConfigFiles')
                    .is(List.class)
                    .defaultValueIfInvalid([])
                    .collect {
                        it.collectEntries { key, value ->
                            if (value.contains('${')) {
                                [key, env.expand(value)]
                            } else {
                                [key, value]
                            }
                        }
                    }

            storeCredentials(expandCredentials)
            storeGlobalConfigFiles(expandGlobalConfigFiles)
            this.env.putAll(configuration.validateAndGet('pipeline.environmentVars').isMap().defaultValueIfInvalid([:]))
        }
        this.configureScm(configuration)

    }

    ConcurrentMap<String, Object> getParams() {
        return params
    }

    EnvVars getEnv() {
        return env
    }

    def propertyMissing(String prop) {
        env[prop]
    }

    void setProperty(String prop, Object value) {
        env[prop] = value
    }

    Map env(Map env) {
        this.env.putAll(env)
        return this.env
    }

    void environment(@DelegatesTo(value = Map, strategy = DELEGATE_FIRST) final Closure closure) {
        env.with(closure)
    }
}
