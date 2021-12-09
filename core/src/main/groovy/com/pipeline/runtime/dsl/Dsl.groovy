package com.pipeline.runtime.dsl


import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType

import static groovy.lang.Closure.DELEGATE_FIRST
import static groovy.lang.Closure.DELEGATE_ONLY

class Dsl {
    static Script script

    static void initialize(script) {
        this.script = script
    }

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


trait NodeDsl {
    final Placeholder any = Placeholder.ANY

    void node(@DelegatesTo(value = StagesDsl, strategy = DELEGATE_ONLY) final Closure closure) {
        this.env.putAll(Dsl.script.getBinding().getVariables()?.environment)
        this.credentials.addAll(Dsl.script.getBinding().getVariables()?.credentials)
        this.configureScm(Dsl.script.getBinding().getVariables()?.scmConfig)
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




class PipelineDsl {
    final Placeholder any = Placeholder.ANY
    static StepsExecutor steps

    PipelineDsl() {
        steps = new StepsExecutor()
        steps.env.putAll(Dsl.script.getBinding().getVariables()?.environment)
        steps.credentials.addAll(Dsl.script.getBinding().getVariables()?.credentials)
        steps.configureScm(Dsl.script.getBinding().getVariables()?.scmConfig)
    }

    void agent(final Placeholder any) {
        println "Running pipeline using any available agent..."
    }

    void environment(@DelegatesTo(value = Map, strategy = DELEGATE_FIRST) final Closure closure) {
        PipelineDsl.steps.env.with(closure)
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

        closure.delegate = PipelineDsl.steps
        closure.resolveStrategy = DELEGATE_ONLY
        closure.call()
    }
}

//class StepsExecutor implements Workspace, DynamicObject, BaseSteps, Shell, GitSCMSteps {
//    static final ConcurrentMap<String, String> env = [:] as ConcurrentHashMap
//
//}








