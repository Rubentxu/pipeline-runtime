package com.pipeline.runtime

import groovy.transform.CompileStatic
import org.yaml.snakeyaml.Yaml


@CompileStatic
class PipelineRuntime implements Runnable {
    private final GroovyClassLoader loader = new GroovyClassLoader(this.getClass().getClassLoader())
    private String jenkinsFile
    private String configFile
    private String library

    PipelineRuntime(String jenkinsFile, String configFile, String library) {
        this.jenkinsFile = jenkinsFile
        this.configFile = configFile
        this.library = library
    }


    void run() throws IllegalAccessException, InstantiationException, IOException {
        def binding = new Binding()
        GroovyScriptEngine engine
        if (library) {
            engine = new GroovyScriptEngine([toUrl(library), toUrl(jenkinsFile)] as URL[], loader)
        } else {
            println "Url jenkinsfile $jenkinsFile"
            engine = new GroovyScriptEngine([toUrl(jenkinsFile)] as URL[], loader)
            println "Url jenkinsfile $jenkinsFile"
        }

        if (configFile) {
            Yaml parser = new Yaml()
            Map example = parser.load((configFile as File).text)

            example.each { println it }
            binding = new Binding(example)
            println "Binding $example"

        }
//        def script = engine.groovyClassLoader.parseClass('greeter()').newInstance()
//        script.run()
        engine.run(toUrl(jenkinsFile).getPath(), binding)

    }

    URL toUrl(String filePath) {
        def url = new File(filePath).toURI().toURL()
        println "to Url $url"
        return url
    }

//    void loadRegisteredScriptExtensions(GroovyClassLoader loader, Path classpath) {
//        final String[] pe = classpath.list();
//        try {
//            for (String file : pe) {
//                loader.addClasspath(file);
//            }
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//
//    }


}
