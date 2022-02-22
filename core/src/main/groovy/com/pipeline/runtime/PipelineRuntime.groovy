package com.pipeline.runtime

import com.pipeline.runtime.dsl.PipelineDsl
import com.pipeline.runtime.dsl.Steps
import com.pipeline.runtime.dsl.StepsExecutor
import com.pipeline.runtime.interfaces.IConfiguration
import com.pipeline.runtime.interfaces.ILogger
import com.pipeline.runtime.library.*
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ImportCustomizer
import org.codehaus.groovy.control.messages.WarningMessage
import org.codehaus.groovy.runtime.InvokerHelper

import java.lang.reflect.Method

import static com.pipeline.runtime.library.LocalSource.localSource
import static com.pipeline.runtime.library.LocalLib.localLib
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
    private String scriptExtension = "groovy"
    private ClassLoader baseClassloader
    private IConfiguration configuration
    private ILogger logger
    private Steps steps
    Class scriptBaseClass = StepsExecutor.class
    private Map<String, String> imports = [
            "NonCPS": "com.cloudbees.groovy.cps.NonCPS",
            "Library": "com.pipeline.runtime.library.Library"

    ]
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
        ServiceLocator.initialize()
        baseClassloader = this.class.getClassLoader()// Thread.currentThread().getContextClassLoader()
        this.configuration = ServiceLocator.getService(IConfiguration.class)
        this.logger = ServiceLocator.getService(ILogger.class)
        scriptRoots.add(jenkinsFile)

        configuration.loadConfig(configFile as File)
        if (configuration.containsKey('pipeline.globalLibraries.libraries')) {
            for (def library : configuration.getValue('pipeline.globalLibraries.libraries') as List<Map>) {
                registerSharedLibrary(library)
            }
        }
        configuration.printConfiguration()

        def globalExceptionHandler = new ExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(globalExceptionHandler);
    }

    void registerSharedLibrary(Map library) {
        Objects.requireNonNull(library.name, "Property 'name' of the shared library must be defined")
        Objects.requireNonNull(library.retriever, "Property 'retriever' of the shared library must be defined")
        def name = library.name
        def version = library?.version ?: 'master'
        SourceRetriever retriever = null
        String credentialsId = ''
        if (library.retriever?.local?.path) {
            retriever = localSource(toFullPath(library.retriever.local.path))
        } else if (library.retriever?.scm?.git) {
            assert library.retriever?.scm?.git?.remote.startsWith("https:"): "git source must point to a valid repository url"
            retriever = gitSource(library.retriever?.scm?.git?.remote,steps)
            credentialsId = library.retriever?.scm?.git?.credentialsId?:''
            logger.debug("CredentialsId  $credentialsId ${credentialsId? 'defined': 'not defined'}")
        } else if (library.retriever?.local?.jar) {
            retriever = localLib(toFullPath(library.retriever.local.jar))
            logger.debug("Load jar lib ${library.retriever.local.jar}")
        } else {
            throw new NullPointerException("Property 'source' (local or git) of the shared library must be defined $library")
        }

        LibraryConfiguration libraryConfig = LibraryConfiguration.library(name)
                .defaultVersion(version)
                .allowOverride(true)
                .implicit(false)
                .targetPath(configuration.getValueOrDefault('pipeline.workingDir','build/workspace'))
                .retriever(retriever)
                .credentialsId(credentialsId)
                .build()
        this.libraries.put(libraryConfig.name, libraryConfig)

    }

    PipelineRuntime init() {
        logger.debug "Into PipelineRuntime init()"

        CompilerConfiguration compilerConfiguration = new CompilerConfiguration(CompilerConfiguration.DEFAULT)
        loader = new GroovyClassLoader(baseClassloader, compilerConfiguration)

        libLoader = new LibraryLoader(loader, libraries)
        LibraryAnnotationTransformer libraryTransformer = new LibraryAnnotationTransformer(libLoader)
        compilerConfiguration.addCompilationCustomizers(libraryTransformer)
        compilerConfiguration.setWarningLevel(WarningMessage.NONE)

        ImportCustomizer importCustomizer = new ImportCustomizer()
        imports.each { k, v -> importCustomizer.addImport(k, v) }
        staticImport.each { k, v -> importCustomizer.addStaticImport(v, k) }
        compilerConfiguration.addCompilationCustomizers(importCustomizer)

        compilerConfiguration.setDefaultScriptExtension(scriptExtension)
        compilerConfiguration.setScriptBaseClass(scriptBaseClass.getName())
//        compilerConfiguration.addCompilationCustomizers(new ASTTransformationCustomizer(ToString))
        compilerConfiguration.setTargetBytecode(CompilerConfiguration.JDK11)
        compilerConfiguration.setRecompileGroovySource(true)

        File dir = new File("build/target/test-generated-classes");
        dir.mkdirs();
        Map options = new HashMap()
        options.put("stubDir", dir)
        compilerConfiguration.setJointCompilationOptions(options)

        gse = new GroovyScriptEngine(scriptRoots.toArray() as String[], loader)
        gse.setConfig(compilerConfiguration)
        for (def library : configuration.getValueOrDefault('pipeline.globalLibraries.libraries', []) as List<Map>) {
            libLoader.loadLibrary("${library.name}")
        }

        logger.info "Shared libs loaders ${libLoader.libRecords}"
        return this
    }

    void run() throws IllegalAccessException, InstantiationException, IOException {
        def binding = new Binding()
        StepsExecutor script = loadScript(jenkinsFile, binding)
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
        def file = new File(filePath)
        def path = file.toURI().toURL().getPath()
        assert file.exists(): "File ${path} not exist"
        return path
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
