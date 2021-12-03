package com.pipeline.cli

import com.pipeline.runtime.PipelineRuntime
import groovy.transform.CompileStatic
import picocli.CommandLine
import picocli.CommandLine.Option

@CompileStatic
class PipelineCLI implements Runnable {

    @Option(names = ["-j", "--jenkinsfile"], description = "Jenkinsfile")
    String jenkinsFile

    @Option(names = ["-c", "--config"], description = "Jenkins config file")
    String configFile

    @Option(names = ["-l", "--library"], description = "Jenkins library path")
    String library

    void run() throws IllegalAccessException, InstantiationException, IOException {
        PipelineRuntime runtime = new PipelineRuntime(jenkinsFile, configFile, library)
        runtime.run()

    }


    static void main(String[] args) {
        System.exit(new CommandLine(new PipelineCLI()).execute(args))
    }
}
