package com.pipeline.runtime

import com.pipeline.runtime.dsl.PipelineDsl

import static groovy.lang.Closure.DELEGATE_ONLY
import static groovy.lang.Closure.DELEGATE_ONLY
import static groovy.lang.Closure.DELEGATE_ONLY
import static groovy.lang.Closure.DELEGATE_ONLY

class Job {
    public static Script script

    static void pipeline(@DelegatesTo(value = PipelineDsl, strategy = DELEGATE_ONLY) final Closure closure) {
        final PipelineDsl dsl = new PipelineDsl()

        closure.delegate = dsl
        closure.resolveStrategy = DELEGATE_ONLY
        closure.call()
    }

    static void node(@DelegatesTo(value = PipelineDsl, strategy = DELEGATE_ONLY) final Closure closure) {
        final PipelineDsl dsl = new PipelineDsl()

        closure.delegate = dsl
        closure.resolveStrategy = DELEGATE_ONLY
        closure.call()
    }

}
