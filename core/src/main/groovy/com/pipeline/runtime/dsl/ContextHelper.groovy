package com.pipeline.runtime.dsl

class ContextHelper {
    private ContextHelper() {
    }

    static void executeInContext(Closure closure, Context freshContext) {
        if (closure) {
            closure.delegate = freshContext
            closure.resolveStrategy = Closure.DELEGATE_FIRST
            closure.call()
        }
    }
}
