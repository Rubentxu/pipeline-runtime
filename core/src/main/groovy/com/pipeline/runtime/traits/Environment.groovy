package com.pipeline.runtime.traits

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

trait Environment {
    abstract ConcurrentMap<String, String> getEnv()
}