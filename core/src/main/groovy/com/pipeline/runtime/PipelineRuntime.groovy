package com.pipeline.runtime


import com.pipeline.runtime.library.LibClassLoader
import com.pipeline.runtime.library.LibraryAnnotationTransformer
import com.pipeline.runtime.library.LibraryConfiguration
import com.pipeline.runtime.library.LibraryLoader
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ImportCustomizer
import org.codehaus.groovy.runtime.InvokerHelper
import org.yaml.snakeyaml.Yaml

import java.lang.reflect.Method

import static com.pipeline.runtime.library.LibraryConfiguration.library
import static com.pipeline.runtime.library.LocalSource.localSource

//@CompileStatic
class PipelineRuntime implements Runnable {
    protected static Method SCRIPT_SET_BINDING = Script.getMethod('setBinding', Binding.class)
    private GroovyClassLoader loader
    private String jenkinsFile
    private String configFile

    Map<String, LibraryConfiguration> libraries = [:]
    protected GroovyScriptEngine gse
    LibraryLoader libLoader
    List<String> scriptRoots = []
    String scriptExtension = "jenkins"
    Map<String, String> imports = ["NonCPS": "com.cloudbees.groovy.cps.NonCPS", "Library": "com.pipeline.runtime.library.Library"]
    Map<String, String> staticImport = [
            "pipeline"  : "com.pipeline.runtime.Job",
            "initialize": "com.pipeline.runtime.Job",
            "scm": "com.pipeline.runtime.extensions.GitSCMSteps"
    ]

    String baseScriptRoot = "."
    Binding binding
    ClassLoader baseClassloader = this.class.classLoader
//    Class scriptBaseClass = StepsExecutor.class

    PipelineRuntime(String jenkinsFile, String configFile, String libraryPath = '.') {
        this.jenkinsFile = jenkinsFile
        this.configFile = configFile
        scriptRoots.add(libraryPath)

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
            return new LibClassLoader(this, null)
        })

        def script = loadScript(toFullPath(jenkinsFile), binding)
        Job.script = script
        script.run()


    }


    Script loadScript(String scriptName, Binding binding) {
        Objects.requireNonNull(binding, "Binding cannot be null.")
        Objects.requireNonNull(gse, "GroovyScriptEngine is not initialized: Initialize the helper by calling init().")
        Class scriptClass = gse.loadScriptByName(scriptName)
        setGlobalVars(binding)
        Script script = InvokerHelper.createScript(scriptClass, binding)
        return script
    }


    /**
     * Sets global variables defined in loaded libraries on the binding
     * @param binding
     */
    public void setGlobalVars(Binding binding) {
        libLoader.libRecords.values().stream()
                .flatMap { it.definedGlobalVars.entrySet().stream() }
                .forEach { e ->
                    if (e.value instanceof Script) {
                        Script script = Script.cast(e.value)
                        // invoke setBinding from method to avoid interception
                        SCRIPT_SET_BINDING.invoke(script, binding)
                    }
                    binding.setVariable(e.key, e.value)
                }
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
        staticImport.each { k, v -> importCustomizer.addStaticImport(v, k) }
        configuration.addCompilationCustomizers(importCustomizer)

        configuration.setDefaultScriptExtension(scriptExtension)
//        configuration.setScriptBaseClass(scriptBaseClass.getName())
        gse = new GroovyScriptEngine(scriptRoots.toArray() as String[], loader)
        gse.setConfig(configuration)
        getLibLoader().loadLibrary("commons")
        println "commons libs loaders ${getLibLoader().libRecords}"
        return this
    }



}
