package com.pipeline.runtime

import com.pipeline.runtime.library.LibraryAnnotationTransformer
import com.pipeline.runtime.library.LibraryConfiguration
import com.pipeline.runtime.library.LibraryLoader
import groovy.transform.CompileStatic
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ImportCustomizer
import org.yaml.snakeyaml.Yaml


@CompileStatic
class PipelineRuntime implements Runnable {
    private GroovyClassLoader loader
    private String jenkinsFile
    private String configFile

    Map<String, LibraryConfiguration> libraries = [:]
    protected GroovyScriptEngine gse
    LibraryLoader libLoader
    String[] scriptRoots = ["src/main/jenkins", "./."]
    String scriptExtension = "jenkins"
    Map<String, String> imports = ["NonCPS": "com.cloudbees.groovy.cps.NonCPS"]
    String baseScriptRoot = "."
    Binding binding = new Binding()
    ClassLoader baseClassloader = this.class.classLoader

    PipelineRuntime(String jenkinsFile, String configFile, String library='.') {
        this.jenkinsFile = jenkinsFile
        this.configFile = configFile
        scriptRoots.join(library)
//        scriptRoots.join(jenkinsFile)
    }


    void run() throws IllegalAccessException, InstantiationException, IOException {

        if (configFile) {
            Yaml parser = new Yaml()
            Map example = parser.load((configFile as File).text)

            example.each { println it }
            binding = new Binding(example)
            println "Binding $example"

        }
        gse.run(toFullPath(jenkinsFile), binding)

    }

    String toFullPath(String filePath) {
        def url = new File(filePath).toURI().toURL()
        println "to Url $url"
        return url.getPath()
    }

    def init() {
        CompilerConfiguration configuration = new CompilerConfiguration()
        loader = new GroovyClassLoader(baseClassloader, configuration)

        libLoader = new LibraryLoader(loader, libraries)
        LibraryAnnotationTransformer libraryTransformer = new LibraryAnnotationTransformer(libLoader)
        configuration.addCompilationCustomizers(libraryTransformer)

        ImportCustomizer importCustomizer = new ImportCustomizer()
        imports.each { k, v -> importCustomizer.addImport(k, v) }
        configuration.addCompilationCustomizers(importCustomizer)

        configuration.setDefaultScriptExtension(scriptExtension)
        //        configuration.setScriptBaseClass(scriptBaseClass.getName())
        gse = new GroovyScriptEngine(scriptRoots, loader)
        gse.setConfig(configuration)
        return this
    }


}
