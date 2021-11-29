package com.pipeline.cli

import groovy.transform.CompileStatic
import org.junit.Rule
import org.junit.Test
import org.junit.contrib.java.lang.system.SystemErrRule;
import org.junit.contrib.java.lang.system.SystemOutRule
import picocli.CommandLine

import static org.junit.Assert.assertEquals;

@CompileStatic
class PipelineRuntimeSpec {


    @Test
    void testMyApp() throws Exception {
        def filePath = getClass().getClassLoader().getResource('pipelines/JenkinsfileTest').getPath()
        new CommandLine(new PipelineRuntime()).execute("--jenkinsfile=$filePath", "-d=dotfile");
        String expected = String.format("--jenkinsfile='value'%n" +
                "position[0]='arg0'%n" +
                "position[1]='arg1'%n");
//        assertEquals(expected, systemOutRule.getLog());
//        assertEquals("", systemErrRule.getLog());
    }
}
