package com.pipeline.runtime.extensions

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

trait Environment {
    final ConcurrentMap<String, String> env = [:] as ConcurrentHashMap
    final ConcurrentMap<String, Object> params = [:] as ConcurrentHashMap

}