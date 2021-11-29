package com.pipeline.runtime

import com.pipeline.runtime.traits.BaseSteps
import com.pipeline.runtime.traits.DynamicObject
import com.pipeline.runtime.traits.GitSCMSteps
import com.pipeline.runtime.traits.Shell
import com.pipeline.runtime.traits.Workspace
import groovy.transform.NamedParam
import groovy.transform.NamedParams
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

import static groovy.lang.Closure.DELEGATE_FIRST
import static groovy.lang.Closure.DELEGATE_ONLY



class Dsl {

    static void pipeline(@DelegatesTo(value = PipelineDsl, strategy = DELEGATE_ONLY) final Closure closure) {
        final PipelineDsl dsl = new PipelineDsl()

        closure.delegate = dsl
        closure.resolveStrategy = DELEGATE_ONLY
        closure.call()
    }

}

class PipelineDsl {
    final Placeholder any = Placeholder.ANY

    static final ConcurrentMap<String, String> env = [:] as ConcurrentHashMap

    void agent(final Placeholder any) {
        println "Running pipeline using any available agent..."
    }

    void environment(@DelegatesTo(value = Map, strategy = DELEGATE_FIRST) final Closure closure) {
        env.with(closure)
    }

    void stages(@DelegatesTo(value = StagesDsl, strategy = DELEGATE_ONLY) final Closure closure) {
        final StagesDsl dsl = new StagesDsl()

        closure.delegate = dsl
        closure.resolveStrategy = DELEGATE_ONLY
        closure.call()

        dsl.stages.each { stage ->
            stage.run()
        }
    }

    enum Placeholder {
        ANY
    }
}

class StagesDsl {
    protected final List<Stage> stages = []

    void stage(final String name, @DelegatesTo(value = StageDsl, strategy = DELEGATE_ONLY) final Closure closure) {
        stages << new Stage(name, closure)
    }
}

class Stage {
    final String name
    final Closure closure

    Stage(String name, Closure closure) {
        this.name = name
        this.closure = closure
    }

    void run() {
        println "==> Running '${name}' stage..."

        final StageDsl dsl = new StageDsl()

        closure.delegate = dsl
        closure.resolveStrategy = DELEGATE_ONLY
        closure.call()
    }
}

class StageDsl {
    void steps(
            @DelegatesTo(value = Steps, strategy = DELEGATE_ONLY)
            @ClosureParams(value = SimpleType, options = ["java.util.Map"]) final Closure closure) {
        final Steps steps = new Steps()

        closure.delegate = steps
        closure.resolveStrategy = DELEGATE_ONLY
        closure.call(PipelineDsl.env)
    }
}

class Steps implements Workspace, DynamicObject, BaseSteps, Shell, GitSCMSteps {

}








