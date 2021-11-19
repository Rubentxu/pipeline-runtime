package com.pipeline.runtime

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

class PipelineDsl {
    final Placeholder any = Placeholder.ANY
    final Steps steps

    static final ConcurrentMap<String, String> env = new ConcurrentHashMap()

    PipelineDsl(Steps steps) {
        this.steps = steps
        env.putAll([WORKSPACE:'build/workspace',WORKSPACE_TMP:'build/workspace@tmp'])
    }

    void agent(final Placeholder any) {
        println "Running pipeline using any available agent..."
    }

    void agent(final Closure closure) {
        println "Running pipeline using any available agent..."
    }

    void options(final Closure closure) {
        println "Running pipeline using any available options..."
    }

    void environment(@DelegatesTo(value = Map, strategy = Closure.DELEGATE_FIRST) final Closure closure) {
        env.with(closure)
    }

    void stages(@DelegatesTo(value = StagesDsl, strategy = Closure.DELEGATE_ONLY) final Closure closure) {
        final StagesDsl dsl = new StagesDsl()

        closure.delegate = dsl
        closure.resolveStrategy = Closure.DELEGATE_ONLY
        println "closure PipelineDsl call $closure"
        closure.call()

        dsl.stages.each { stage ->
            println "iterate stage item $stage.name"
            stage.run()
        }
    }

    enum Placeholder {
        ANY
    }
}