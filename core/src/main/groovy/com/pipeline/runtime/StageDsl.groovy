package com.pipeline.runtime

import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType

class StageDsl {

    void steps(
        @DelegatesTo(value = Steps, strategy = Closure.DELEGATE_ONLY)
        @ClosureParams(value = SimpleType, options = ["java.util.Map"]) final Closure closure) {
        final Steps steps = new Steps()
        closure.delegate = steps
        closure.resolveStrategy = Closure.DELEGATE_ONLY
        closure.call(PipelineDsl.env)
    }
}