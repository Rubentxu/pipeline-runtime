package com.pipeline.runtime

import com.pipeline.runtime.dsl.PipelineDsl
import com.pipeline.runtime.dsl.StepsExecutor

import static groovy.lang.Closure.DELEGATE_ONLY

class Job {
    public static Script script
    public static StepsExecutor steps

    static void pipeline(@DelegatesTo(value = PipelineDsl, strategy = DELEGATE_ONLY) final Closure closure) {
        final PipelineDsl dsl = new PipelineDsl()

        closure.delegate = dsl
        closure.resolveStrategy = DELEGATE_ONLY
        initializeJob()
        closure.call()
    }

    static private void initializeJob() {
        steps = StepsExecutor.getInstance()
        steps.env.putAll(Job.script.getBinding().getVariables()?.environment)
        steps.credentials.addAll(Job.script.getBinding().getVariables()?.credentials)
        steps.configureScm(Job.script.getBinding().getVariables()?.scmConfig)
        steps.initializeWorkspace()
        steps.setBinding(script.getBinding())
    }

    static void node(@DelegatesTo(value = PipelineDsl, strategy = DELEGATE_ONLY) final Closure closure) {
        final PipelineDsl dsl = new PipelineDsl()

        closure.delegate = dsl
        closure.resolveStrategy = DELEGATE_ONLY
        closure.call()
    }

}
