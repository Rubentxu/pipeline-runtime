package com.pipeline.runtime

import com.pipeline.runtime.interfaces.ICasc
import com.pipeline.runtime.interfaces.IRunnerLogger
import groovy.transform.CompileStatic
import picocli.CommandLine
import picocli.CommandLine.Option

import java.text.SimpleDateFormat

//@CompileStatic
class PipelineCLI implements Runnable {

    @Option(names = ["-p", "--pipelinefile"], description = "Pipeline file definition")
    String jenkinsFile

    @Option(names = ["-c", "--config"], description = "Pipeline config file")
    String configFile

    void run() throws IllegalAccessException, InstantiationException, IOException {
        long start = System.currentTimeMillis();
        ICasc configuration = new Casc(configFile)
        IRunnerLogger loggerService = new RunnerLogger(configuration?.pipeline?.logLevel as String ?:'INFO')
        def runtime =  new PipelineRuntime([jenkinsFile], loggerService)
        runtime.initialize(configuration)
                .run()
        long finish = System.currentTimeMillis();
        long timeElapsed = finish - start
        println("Total Time Elapsed ${new SimpleDateFormat("mm:ss:SSS").format(new Date(timeElapsed))}")
    }


    static void main(String[] args) {
        System.exit(new CommandLine(new PipelineCLI()).execute(args))
    }
}

