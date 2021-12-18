package com.pipeline.runtime

import com.pipeline.runtime.dsl.PipelineDsl
import com.pipeline.runtime.dsl.Steps
import com.pipeline.runtime.dsl.StepsExecutor
import com.pipeline.runtime.library.*
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ImportCustomizer
import org.codehaus.groovy.runtime.InvokerHelper
import org.yaml.snakeyaml.Yaml

import java.lang.reflect.Method

import static com.pipeline.runtime.library.LocalSource.localSource
import static com.pipeline.runtime.library.GitSource.gitSource
import static groovy.lang.Closure.DELEGATE_ONLY

//@CompileStatic
class PipelineRuntime implements Runnable {
    private static Method SCRIPT_SET_BINDING = Script.getMethod('setBinding', Binding.class)
    private GroovyClassLoader loader
    private String jenkinsFile
    private Map<String, LibraryConfiguration> libraries = [:]
    private GroovyScriptEngine gse
    private LibraryLoader libLoader
    private List<String> scriptRoots = []
    private String scriptExtension = "jenkins"
    private ClassLoader baseClassloader = this.class.classLoader
    private Map configuration
//    Class scriptBaseClass = StepsExecutor.class
    private Map<String, String> imports = ["NonCPS": "com.cloudbees.groovy.cps.NonCPS", "Library": "com.pipeline.runtime.library.Library"]
    private Map<String, String> staticImport = [
            "pipeline"  : "com.pipeline.runtime.PipelineRuntime",
            "initialize": "com.pipeline.runtime.PipelineRuntime",
            "library"   : "com.pipeline.runtime.PipelineRuntime",
            "scm"       : "com.pipeline.runtime.extensions.GitSCM"
    ]


    PipelineRuntime(String jenkinsFile, String configFile) {
        Objects.requireNonNull(jenkinsFile, "Pipelinefile cannot be null.")
        Objects.requireNonNull(configFile, "Configuration file cannot be null.")
        this.jenkinsFile = toFullPath(jenkinsFile)

        scriptRoots.add(jenkinsFile)

        Yaml parser = new Yaml()
        configuration = parser.load((configFile as File).text)
        registerSharedLibrary(configuration)

        configuration.each { println it }
        println "Configuration $configuration"

        ServiceLocator.instance.loadService(Steps.class, new StepsExecutor())


    }

    void registerSharedLibrary(Map configuration) {
        if (configuration.sharedLibrary) {
            Objects.requireNonNull(configuration.sharedLibrary.name, "Property 'name' of the shared library must be defined")
            Objects.requireNonNull(configuration.sharedLibrary.source, "Property 'source' of the shared library must be defined")
            def name = configuration.sharedLibrary.name
            def version = configuration.sharedLibrary?.version ?: 'master'
            SourceRetriever retriever = null
            if (configuration.sharedLibrary.source.local) {
                retriever = localSource(toFullPath(configuration.sharedLibrary.source.local))
            } else if (configuration.sharedLibrary.source.git) {
                assert configuration.sharedLibrary.source.git.startsWith("https:"): "git source must point to a valid repository url"
                retriever = gitSource(configuration.sharedLibrary.source.git)
            } else {
                throw new NullPointerException("Property 'source' (local or git) of the shared library must be defined")
            }

            LibraryConfiguration library = LibraryConfiguration.library(name)
                    .defaultVersion(version)
                    .allowOverride(true)
                    .implicit(false)
                    .targetPath('<notNeeded>')
                    .retriever(retriever)
                    .build()
            this.libraries.put(library.name, library)
        }

    }

    void run() throws IllegalAccessException, InstantiationException, IOException {
        def binding = new Binding()

        def script = loadScript(jenkinsFile, binding)
        initializePipeline(binding)
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

    private void initializePipeline(Binding binding) {
        def steps = ServiceLocator.instance.getService(Steps.class)
        steps.env.putAll(configuration.environment)
        steps.credentials.addAll(configuration.credentials)
        steps.configureScm(configuration.scmConfig)
        steps.initializeWorkspace()
        steps.setBinding(binding)
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
        def file = new File(filePath)
        def path = file.toURI().toURL().getPath()
        assert file.exists() : "File ${path} not exist"
        return path
    }

    PipelineRuntime init() {
        CompilerConfiguration compilerConfiguration = new CompilerConfiguration()
        loader = new GroovyClassLoader(baseClassloader, compilerConfiguration)

        libLoader = new LibraryLoader(loader, libraries)
        LibraryAnnotationTransformer libraryTransformer = new LibraryAnnotationTransformer(libLoader)
        compilerConfiguration.addCompilationCustomizers(libraryTransformer)

        ImportCustomizer importCustomizer = new ImportCustomizer()
        imports.each { k, v -> importCustomizer.addImport(k, v) }
        staticImport.each { k, v -> importCustomizer.addStaticImport(v, k) }
        compilerConfiguration.addCompilationCustomizers(importCustomizer)

        compilerConfiguration.setDefaultScriptExtension(scriptExtension)
//        configuration.setScriptBaseClass(scriptBaseClass.getName())
        gse = new GroovyScriptEngine(scriptRoots.toArray() as String[], loader)
        gse.setConfig(compilerConfiguration)
        libLoader.loadLibrary(configuration.sharedLibrary.name)
        println "shared libs loaders ${libLoader.libRecords}"
        return this
    }

    static LibClassLoader library(Map args) {
        assert args.identifier
//        libLoader.loadImplicitLibraries()
//        libLoader.loadLibrary(args.identifier)
//        setGlobalVars(script.getBinding())
        return new LibClassLoader(ServiceLocator.instance.getService(Steps.class), null)
    }

    static LibClassLoader library(String expression) {
//        libLoader.loadImplicitLibraries()
//        libLoader.loadLibrary(expression)
//        setGlobalVars(script.getBinding())
        return new LibClassLoader(ServiceLocator.instance.getService(Steps.class), null)
    }

    static void pipeline(@DelegatesTo(value = PipelineDsl, strategy = DELEGATE_ONLY) final Closure closure) {
        final PipelineDsl dsl = new PipelineDsl()

        closure.delegate = dsl
        closure.resolveStrategy = DELEGATE_ONLY
        closure.call()
    }


    static void node(@DelegatesTo(value = PipelineDsl, strategy = DELEGATE_ONLY) final Closure closure) {
        final PipelineDsl dsl = new PipelineDsl()

        closure.delegate = dsl
        closure.resolveStrategy = DELEGATE_ONLY
        closure.call()
    }


}
