package com.pipeline.runtime.extensions

import com.pipeline.runtime.dsl.StepsExecutor

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

class Environment {
    static final ConcurrentMap<String, String> env = [*: System.env,] as ConcurrentHashMap

    static ConcurrentMap<String, String> getEnv(StepsExecutor self) {
        return env
    }

    static def propertyMissing(StepsExecutor self,String prop) {
        env[prop]
    }
    static void setProperty(StepsExecutor self, String prop, Object value) {
        env[prop] = value
    }

    Map env(Map env) {
        this.env.putAll(env)
        return this.env()
    }
}
