package com.pipeline.runtime

import groovy.transform.CompileStatic
import org.junit.Test
import picocli.CommandLine

@CompileStatic
class PipelineRuntimeSpec {


    @Test
    void testMyApp() throws Exception {
        def filePath = getClass().getClassLoader().getResource('pipelines/JenkinsfileTest.groovy').getPath()
        def configPath = getClass().getClassLoader().getResource('pipelines/config.yaml').getPath()

        new CommandLine(new PipelineCLI()).execute("-p=$filePath", "-c=$configPath");
        String expected = String.format("--jenkinsfile='value'%n" +
                "position[0]='arg0'%n" +
                "position[1]='arg1'%n");

//        println systemErrRule.getLog();
    }
}
