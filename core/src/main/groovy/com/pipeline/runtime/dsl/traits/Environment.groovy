package com.pipeline.runtime.dsl.traits


import java.util.concurrent.ConcurrentMap

trait Environment {
    abstract ConcurrentMap<String, String> getEnv()
}