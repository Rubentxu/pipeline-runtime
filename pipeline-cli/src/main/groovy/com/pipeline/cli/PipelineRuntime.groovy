package com.pipeline.cli

import io.github.cdimascio.dotenv.Dotenv
import picocli.CommandLine
import picocli.CommandLine.Option;

class PipelineRuntime implements Runnable {
    private final GroovyClassLoader loader = new GroovyClassLoader(this.getClass().getClassLoader());

    @Option(names = ["-j", "--jenkinsfile"], description = "Jenkinsfile")
    String jenkinsFile

    @Option(names = ["-d", "--dotfile"], description = "Environment dot file")
    String dotFile

    void run() throws IllegalAccessException, InstantiationException, IOException {
//        Dotenv dotenv = Dotenv.configure()
//                .directory(dotFile)
//                .ignoreIfMalformed()
//                .ignoreIfMissing()
//                .systemProperties()
//                .load();
//            new GroovyShell().run(new File(jenkinsFile));
        Script script = loader.parseClass(new File(jenkinsFile)).newInstance()
        script.run()
//            GroovyObject calc = (GroovyObject) pipelineClass.newInstance()
//            calc.invokeMethod("main", null)

    }
    static void main(String[] args) {
        System.exit(new CommandLine(new PipelineRuntime()).execute(args))
    }
}
