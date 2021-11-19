package com.pipeline.runtime.traits

import com.pipeline.runtime.PipelineDsl
import com.pipeline.runtime.StageDsl

import static groovy.lang.Closure.DELEGATE_FIRST
import static groovy.lang.Closure.DELEGATE_ONLY

trait GitSCMSteps {
    void node(final Closure closure) {
        node("default", closure)
    }

    void node(final String name, @DelegatesTo(value = StageDsl, strategy = DELEGATE_ONLY) final Closure closure) {
        println "==> Running '${name}' node..."

        final StageDsl dsl = new StageDsl(this)

        closure.delegate = dsl
        closure.resolveStrategy = DELEGATE_FIRST
        closure.call(PipelineDsl.env)
    }

    void ansiColor(String name, closure) {
//        closure.delegate = this
//        closure.resolveStrategy = DELEGATE_FIRST
        closure.call(PipelineDsl.env)
    }

    void script(closure) {
        println "==> Running 'script closure..."
        closure.delegate = this
        closure.resolveStrategy = DELEGATE_FIRST
        closure.call(PipelineDsl.env)
    }

    void echo(final String message) {
        println message
    }
}
