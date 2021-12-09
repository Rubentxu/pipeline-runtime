package com.pipeline.runtime.extensions

import com.pipeline.runtime.dsl.StepsExecutor

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

class Workspace {
    static String workingDir = "build/workspace"

    static final ConcurrentMap<String, Object> params = [:] as ConcurrentHashMap

    static void initializeEnvironment(extended) {

    }

    static String getWorkingDir(StepsExecutor self) {
        return workingDir
    }

    static ConcurrentMap<String, Object> getParams(StepsExecutor self) {
        return params
    }
}
