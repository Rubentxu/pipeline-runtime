package com.pipeline.runtime.extensions

import com.pipeline.runtime.dsl.StepsExecutor
import spock.lang.Specification

//@CompileStatic
class GitSCMSpec extends Specification {
    def "Debe clonar un repositorio"() {
        given:
        def steps = [getWorkingDir: {'/tmp/'}] as StepsExecutor

        when:
        GitSCM.checkout(steps, null)

        then:
        true


    }
}