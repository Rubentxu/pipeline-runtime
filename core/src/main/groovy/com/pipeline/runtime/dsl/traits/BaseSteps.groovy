package com.pipeline.runtime.dsl.traits

import com.pipeline.runtime.dsl.PipelineDsl
import com.pipeline.runtime.dsl.StageDsl
import groovy.transform.NamedParam
import groovy.transform.NamedParams

import static groovy.lang.Closure.DELEGATE_ONLY
import static groovy.lang.Closure.DELEGATE_FIRST

trait BaseSteps {

    def baseDir = './'

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


    def findFiles(@NamedParams([
            @NamedParam(value = "glob", type = String, required = true),
            @NamedParam(value = "excludes", type = String)
    ]) final Map param) {
        if (param.excludes) {
            return new FileNameFinder().getFileNames(baseDir, param.glob, param.excludes)
        } else {
            return new FileNameFinder().getFileNames(baseDir, param.glob)
        }
    }
}
