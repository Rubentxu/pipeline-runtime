package com.pipeline.cli

import org.yaml.snakeyaml.Yaml
import picocli.CommandLine
import picocli.CommandLine.Option;

class PipelineRuntime implements Runnable {
    private final GroovyClassLoader loader = new GroovyClassLoader(this.getClass().getClassLoader());

    @Option(names = ["-j", "--jenkinsfile"], description = "Jenkinsfile")
    String jenkinsFile

    @Option(names = ["-c", "--config"], description = "Jenkins config file")
    String configFile

    void run() throws IllegalAccessException, InstantiationException, IOException {
        def binding = new Binding()
        if(configFile) {
            Yaml parser = new Yaml()
            Map example = parser.load((configFile as File).text)

            example.each{println it}
            binding = new Binding(example)
            println "Binding $example"

        }


        Script script = loader.parseClass(new File(jenkinsFile)).newInstance()
        script.setBinding(binding)
        script.run()

    }

    static void main(String[] args) {
        System.exit(new CommandLine(new PipelineRuntime()).execute(args))
    }
}
