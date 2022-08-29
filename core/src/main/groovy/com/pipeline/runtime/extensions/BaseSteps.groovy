package com.pipeline.runtime.extensions

import com.pipeline.runtime.PipelineRuntime
import com.pipeline.runtime.dsl.PipelineDsl
import com.pipeline.runtime.dsl.StageDsl
import com.pipeline.runtime.dsl.StepsExecutor
import com.pipeline.runtime.extensions.domain.JobBuild
import com.pipeline.runtime.interfaces.IRunnerLogger
import groovy.transform.NamedParam
import groovy.transform.NamedParams

import static groovy.lang.Closure.DELEGATE_FIRST
import static groovy.lang.Closure.DELEGATE_ONLY

class BaseSteps {

    static final jobBuild = new JobBuild(true)

    static IRunnerLogger getLog(StepsExecutor self) {
        return PipelineRuntime.logger
    }

    static void node(StepsExecutor self, final Closure closure) {
        node("default", closure)
    }

    static void pipeline(StepsExecutor self, @DelegatesTo(value = PipelineDsl, strategy = DELEGATE_ONLY) final Closure closure) {
        final PipelineDsl dsl = new PipelineDsl(self)

        closure.delegate = dsl
        closure.resolveStrategy = DELEGATE_ONLY
        closure.call()
    }

    static void node(StepsExecutor self,final String name, @DelegatesTo(value = StageDsl, strategy = DELEGATE_ONLY) final Closure closure) {
        println "==> Running '${name}' node..."

        closure.delegate = self
        closure.resolveStrategy = DELEGATE_FIRST
        closure.call(PipelineDsl.env)
    }

    static void ansiColor(StepsExecutor self, String name, closure) {
        closure.delegate = self
        closure.resolveStrategy = DELEGATE_FIRST
        closure.call()
    }

    static void script(StepsExecutor self, closure) {
        println "==> Running 'script closure..."
        closure.delegate = self
        closure.resolveStrategy = DELEGATE_FIRST
        closure.call()
    }

    static void echo(StepsExecutor self, message) {
        println message.toString()
    }

    static void error(StepsExecutor self, @NamedParam(value = "message", type = String, required = true) final Map param) {
        throw new Exception(param.message)
    }

    static void error(StepsExecutor self, final String message) {
        throw new Exception(message)
    }

    static def stage(StepsExecutor self, String name, Closure closure) {
        self.log.info "+stage $name"
        closure.delegate = self
        def result = closure()
        self.log.info "+End stage $name"
        return result
    }


    static def stage(StepsExecutor self, @NamedParams([
            @NamedParam(value = "name", type = String, required = true),
            @NamedParam(value = "body", type = Closure, required = true)
    ]) final Map param) {
        self.stage(param.name, param.body)
    }

    static Map gitChangelog(StepsExecutor self, final Map param) {
      [commits:[]]
    }

    static def publishHTML(StepsExecutor self, final Map param) {
        [commits:[]]
    }

    static JobBuild getCurrentBuild(StepsExecutor self) {
        return jobBuild
    }
}
