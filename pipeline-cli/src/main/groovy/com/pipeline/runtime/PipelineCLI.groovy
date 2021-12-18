package com.pipeline.cli

import com.pipeline.runtime.PipelineRuntime
import groovy.transform.CompileStatic
import picocli.CommandLine
import picocli.CommandLine.Option

@CompileStatic
class PipelineCLI implements Runnable {

    @Option(names = ["-p", "--pipelinefile"], description = "Pipeline file definition")
    String jenkinsFile

    @Option(names = ["-c", "--config"], description = "Pipeline config file")
    String configFile

    void run() throws IllegalAccessException, InstantiationException, IOException {
        PipelineRuntime runtime = new PipelineRuntime(jenkinsFile, configFile)
        runtime.init()
                .run()

    }


    static void main(String[] args) {
        System.exit(new CommandLine(new PipelineCLI()).execute(args))
    }
}
