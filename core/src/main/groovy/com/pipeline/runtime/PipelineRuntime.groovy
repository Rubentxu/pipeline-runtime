package com.pipeline.runtime

import com.pipeline.runtime.library.LibClassLoader
import com.pipeline.runtime.library.LibraryAnnotationTransformer
import com.pipeline.runtime.library.LibraryConfiguration
import com.pipeline.runtime.library.LibraryLoader
import groovy.transform.CompileStatic
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ImportCustomizer
import org.yaml.snakeyaml.Yaml

import static com.pipeline.runtime.library.LibraryConfiguration.library
import static com.pipeline.runtime.library.LocalSource.localSource


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

    PipelineRuntime(String jenkinsFile, String configFile, String libraryPath='.') {
        this.jenkinsFile = jenkinsFile
        this.configFile = configFile
        scriptRoots.join(libraryPath)
//        scriptRoots.join(jenkinsFile)
        def library = library().name('commons')
                .defaultVersion("master")
                .allowOverride(true)
                .implicit(false)
                .targetPath('<notNeeded>')
                .retriever(localSource(libraryPath))
                .build()
        registerSharedLibrary(library)

    }

    void registerSharedLibrary(LibraryConfiguration libraryDescription) {
        Objects.requireNonNull(libraryDescription)
        Objects.requireNonNull(libraryDescription.name)
        this.libraries.put(libraryDescription.name, libraryDescription)
    }

    void run() throws IllegalAccessException, InstantiationException, IOException {

        if (configFile) {
            Yaml parser = new Yaml()
            Map example = parser.load((configFile as File).text)

            example.each { println it }
            binding = new Binding(example)
            println "Binding $example"

        }
        binding.getVariables().put('library', { String expression ->
            getLibLoader().loadLibrary(expression)
            println "$expression libs loaders ${getLibLoader().libRecords}"
            return new LibClassLoader(this, null)
        })

        gse.run(toFullPath(jenkinsFile), binding)

    }

    String toFullPath(String filePath) {
        def url = new File(filePath).toURI().toURL()
        println "to Url $url"
        return url.getPath()
    }

    PipelineRuntime init() {
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
