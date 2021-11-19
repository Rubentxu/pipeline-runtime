package com.pipeline.cli
import picocli.CommandLine
import picocli.CommandLine.Option;

class PipelineRuntime implements Runnable {
    private final GroovyClassLoader loader = new GroovyClassLoader(this.getClass().getClassLoader());

    @Option(names = ["-j", "--jenkinsfile"], description = "Jenkinsfile")
    String jenkinsFile

    void run() throws IllegalAccessException, InstantiationException, IOException {
            Class pipelineClass = loader.parseClass(
                    new File(jenkinsFile));
            GroovyObject calc = (GroovyObject) pipelineClass.newInstance()
            calc.invokeMethod("call", null)

    }
    static void main(String[] args) {
        System.exit(new CommandLine(new PipelineRuntime()).execute(args))
    }
}
