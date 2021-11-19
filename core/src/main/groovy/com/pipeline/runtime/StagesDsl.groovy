package com.pipeline.runtime

class StagesDsl {
    protected final List<Stage> stages = []
    void stage(final String name, @DelegatesTo(value = StageDsl, strategy = Closure.DELEGATE_ONLY) final Closure closure) {
        println "Register stage with name $name"
        stages << new Stage(name, closure)
    }
}