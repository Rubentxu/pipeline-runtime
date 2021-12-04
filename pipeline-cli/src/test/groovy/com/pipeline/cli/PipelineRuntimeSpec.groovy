package com.pipeline.cli

import groovy.transform.CompileStatic
import org.junit.Rule
import org.junit.Test
import org.junit.contrib.java.lang.system.SystemErrRule;
import org.junit.contrib.java.lang.system.SystemOutRule
import picocli.CommandLine

@CompileStatic
class PipelineRuntimeSpec {
    @Rule
    public SystemErrRule systemErrRule = new SystemErrRule().enableLog();

    @Rule
    public SystemOutRule systemOutRule = new SystemOutRule().enableLog();

    @Test
    void testMyApp() throws Exception {
        def filePath = getClass().getClassLoader().getResource('pipelines/JenkinsfileTest.groovy').getPath()
        def configPath = getClass().getClassLoader().getResource('pipelines/config.yaml').getPath()
        def scriptsPath = getClass().getClassLoader().getResource('scripts/').getPath()
        new CommandLine(new PipelineCLI()).execute("--jenkinsfile=$filePath", "-c=$configPath", "-l=$scriptsPath");
        String expected = String.format("--jenkinsfile='value'%n" +
                "position[0]='arg0'%n" +
                "position[1]='arg1'%n");
        println systemOutRule.getLog();
//        println systemErrRule.getLog();
    }
}
