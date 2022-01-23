package com.pipeline.runtime.extensions

import com.pipeline.runtime.PipelineRuntime
import com.pipeline.runtime.dsl.StepsExecutor
import spock.lang.Specification

class PipelineRuntimeSpec extends Specification {

    def "Debe clonar un repositorio"() {
        given:
        def filePath = getClass().getClassLoader().getResource('pipelines/JenkinsfileTest.groovy').getPath()
        def configPath = getClass().getClassLoader().getResource('pipelines/casc.yaml').getPath()
        def runtime =  new PipelineRuntime(filePath, configPath)


        when:
        runtime.init()
                .run()


        then:
        true


    }
}
