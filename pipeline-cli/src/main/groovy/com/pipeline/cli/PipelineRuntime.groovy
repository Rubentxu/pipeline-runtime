package com.pipeline.cli
import picocli.CommandLine
import picocli.CommandLine.Option;

class PipelineRuntime implements Runnable {

    @Option(names = ["-c", "--count"], description = "number of repetitions")
    int count = 1

    void run() {
        count.times {
            println("hello world $it...")
        }
    }
    static void main(String[] args) {
        System.exit(new CommandLine(new PipelineRuntime()).execute(args))
    }
}
