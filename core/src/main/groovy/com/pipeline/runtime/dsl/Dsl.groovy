package com.pipeline.runtime.dsl

import com.pipeline.runtime.Job
import groovy.transform.CompileStatic
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType

import static groovy.lang.Closure.DELEGATE_FIRST
import static groovy.lang.Closure.DELEGATE_ONLY


trait NodeDsl {
    final Placeholder any = Placeholder.ANY

    void node(@DelegatesTo(value = StagesDsl, strategy = DELEGATE_ONLY) final Closure closure) {
        this.env.putAll(Job.script.getBinding().getVariables()?.environment)
        this.credentials.addAll(Job.script.getBinding().getVariables()?.credentials)
        this.configureScm(Job.script.getBinding().getVariables()?.scmConfig)
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



//@CompileStatic
class PipelineDsl {
    final Placeholder any = Placeholder.ANY

    void agent(final Placeholder any) {
        println "Running pipeline using any available agent..."
    }

    void environment(@DelegatesTo(value = Map, strategy = DELEGATE_FIRST) final Closure closure) {
        Job.steps.env.with(closure)
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
    final List<Stage> stages = []

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
            @DelegatesTo(value = StepsExecutor, strategy = DELEGATE_ONLY)
            @ClosureParams(value = SimpleType, options = ["java.util.Map"]) final Closure closure) {

        closure.delegate = Job.steps
        closure.resolveStrategy = DELEGATE_ONLY
        closure.call()
    }
}







