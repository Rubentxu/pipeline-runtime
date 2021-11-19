package com.pipeline.runtime


import groovy.transform.CompileStatic

import static groovy.lang.Closure.DELEGATE_ONLY


@CompileStatic
class Dsl {

    static void pipeline(@DelegatesTo(value = PipelineDsl, strategy = DELEGATE_ONLY) final Closure closure) {
        def stepsWithTraits = initialize() as Steps
        final PipelineDsl dsl = new PipelineDsl (stepsWithTraits)

        closure.delegate = dsl
        closure.resolveStrategy = DELEGATE_ONLY
        println "closure Dsl call $closure"
        closure.call()
    }

    public static def initialize() {
        Steps steps = new Steps()
        def stepsWithTraits = steps
        return stepsWithTraits
    }

}











