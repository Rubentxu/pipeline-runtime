package com.pipeline.runtime

import com.pipeline.runtime.traits.BaseSteps
import com.pipeline.runtime.traits.DynamicObject
import com.pipeline.runtime.traits.Shell
import com.pipeline.runtime.traits.Workspace
import groovy.transform.CompileStatic
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

import static groovy.lang.Closure.DELEGATE_FIRST
import static groovy.lang.Closure.DELEGATE_ONLY


@CompileStatic
class Dsl {

//    static void config(Map userConfig, Map environment) {
//        def pipelineResources = getContext().getPipelineResources()
//        pipelineResources.resolveConfigurationWithEnvironment(userConfig, environment)
//    }

    static void pipeline(@DelegatesTo(value = PipelineDsl, strategy = DELEGATE_ONLY) final Closure closure) {
        def stepsWithTraits = initialize() as StepsScript
        final PipelineDsl dsl = new PipelineDsl (stepsWithTraits)

        closure.delegate = dsl
        closure.resolveStrategy = DELEGATE_ONLY
        println "closure Dsl call $closure"
        closure.call()
    }

    public static def initialize() {
        StepsScript steps = new StepsScript()
        def stepsWithTraits = steps
        return stepsWithTraits
    }

//    public static class cdi {
//        static void initialize(_steps) {
//            println "Initialize CDI Module"
//            def stepsWithTraits = initialize()
//            registerDefaultContext(stepsWithTraits)
//            println "End Initialize CDI Module"
//        }
//
//        static void postInitialize() {
//            def gitlab_domain = GlobalState.getProperty('gitlab.domain') as String
//            getContext().postInitialize([gitlab_domain: gitlab_domain])
//        }
//    }
}


class PipelineDsl {
    final Placeholder any = Placeholder.ANY
    final StepsScript steps

    static final ConcurrentMap<String, String> env = new ConcurrentHashMap()

    PipelineDsl(StepsScript steps) {
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

    void environment(@DelegatesTo(value = Map, strategy = DELEGATE_FIRST) final Closure closure) {
        env.with(closure)
    }

    void stages(@DelegatesTo(value = StagesDsl, strategy = DELEGATE_ONLY) final Closure closure) {
        final StagesDsl dsl = new StagesDsl()

        closure.delegate = dsl
        closure.resolveStrategy = DELEGATE_ONLY
        println "closure PipelineDsl call $closure"
        closure.call()

        dsl.stages.each { stage ->
            println "iterate stage item $stage.name"
            stage(stage.name,stage.closure)
//            stage.run(steps)
        }
    }

    enum Placeholder {
        ANY
    }
}

class StagesDsl {
    protected final List<Stage> stages = []
    void stage(final String name, @DelegatesTo(value = StageDsl, strategy = DELEGATE_ONLY) final Closure closure) {
        println "Register stage with name $name"
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
}

class StageDsl {

    StageDsl(StepsScript steps) {
        super(steps)
    }

    void steps(
        @DelegatesTo(value = StepsScript, strategy = DELEGATE_ONLY)
        @ClosureParams(value = SimpleType, options = ["java.util.Map"]) final Closure closure) {

        closure.delegate = steps
        closure.resolveStrategy = DELEGATE_ONLY
        closure.call(PipelineDsl.env)
    }
}

class StepsScript implements Workspace, DynamicObject, BaseSteps, Shell {
    private dynamicProps = [env: [:], params: [:], logger: { println it }]



    void stage(final String name, @DelegatesTo(value = StageDsl, strategy = DELEGATE_ONLY) final Closure closure) {
        println "==> Running '${name}' stage..."

        final StageDsl dsl = new StageDsl(this)

        closure.delegate = dsl
        closure.resolveStrategy = DELEGATE_FIRST
        closure.call(PipelineDsl.env)
    }


}









