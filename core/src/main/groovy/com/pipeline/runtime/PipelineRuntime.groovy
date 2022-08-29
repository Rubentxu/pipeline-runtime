package com.pipeline.runtime


import com.pipeline.runtime.dsl.StepsExecutor
import com.pipeline.runtime.interfaces.ICasc
import com.pipeline.runtime.interfaces.Initializable
import com.pipeline.runtime.interfaces.IRunnerLogger
import com.pipeline.runtime.library.*
import com.pipeline.runtime.validations.ValidationCategory
import org.codehaus.groovy.runtime.InvokerHelper

import java.lang.reflect.Method

import static com.pipeline.runtime.library.GitSource.gitSource
import static com.pipeline.runtime.library.LocalLib.localLib
import static com.pipeline.runtime.library.LocalSource.localSource

//@CompileStatic
class PipelineRuntime implements Runnable, Initializable {

    private String jenkinsFile
    private Map<String, LibraryConfiguration> libraries = [:]
    private CustomGroovyScriptEngine scriptEngine
    private List<Map> configLibraries
    private ICasc casc
    public static IRunnerLogger logger
    private String workingDir
    List<String> scriptRoots

    PipelineRuntime(List<String> scriptRoots, IRunnerLogger logger) {
        this.scriptRoots = scriptRoots
        Objects.requireNonNull(this.scriptRoots, "Pipelinefile cannot be null.")
        this.jenkinsFile = toFullPath(scriptRoots.get(0))
        this.logger = logger
//        def globalExceptionHandler = new ExceptionHandler();
//        Thread.setDefaultUncaughtExceptionHandler(globalExceptionHandler);
    }


    private String resolveSourcePath(Map config) {
        String sourcePath = ''
        if (config.retriever?.local?.path) {
            logger.system("Load Local Source from ${config.retriever.local.path}")
            sourcePath = toFullPath(config.retriever.local.path)

        } else if (config.retriever?.scm?.git) {
            logger.system("Load Git Source from ${config.retriever?.scm?.git?.remote}")
            sourcePath = config.retriever?.scm?.git?.remote

        } else if (config.retriever?.local?.jar) {
            logger.system("Load Jar Source from ${config.retriever?.local?.jar}")
            sourcePath = toFullPath(config.retriever.local.jar)

        } else {
            throw new NullPointerException("Property 'source' (local or git) of the shared library must be defined $config")
        }
        return sourcePath
    }

    private SourceRetriever resolveSourceRetriever(Map config) {
        SourceRetriever retriever = null

        if (config.retriever?.local?.path) {
            retriever = localSource()

        } else if (config.retriever?.scm?.git) {
            assert config.retriever?.scm?.git?.remote.startsWith("https:"): "git source must point to a valid repository url"
            retriever = gitSource()

        } else if (config.retriever?.local?.jar) {
            retriever = localLib()
            logger.system("Load jar lib ${config.retriever.local.jar}")

        } else {
            throw new NullPointerException("Property 'source' (local or git) of the shared library must be defined $config")
        }
        retriever
    }


    String toFullPath(String filePath) {
        def file = new File(filePath)
        def path = file.toURI().toURL().getPath()
        assert file.exists(): "File ${path} not exist"
        return path
    }

    private LibraryConfiguration createLibraryConfig(Map config) {
        Objects.requireNonNull(config.name, "Property 'name' of the shared library must be defined")
        Objects.requireNonNull(config.retriever, "Property 'retriever' of the shared library must be defined")
        String name = config.name as String
        String version = config?.version ?: 'master'

        use(ValidationCategory) {
            String credentialsId = config.validateAndGet('retriever.scm.git.credentialsId').isString().defaultValueIfInvalid('')
            List<String> modulesPaths = config.validateAndGet('modulesPaths').is(List.class).defaultValueIfInvalid(['./'])

            LibraryConfiguration libraryConfig = new LibraryConfiguration.LibraryBuilder()
                    .name(name)
                    .defaultVersion(version)
                    .allowOverride(true)
                    .implicit(false)
                    .targetPath(this.workingDir)
                    .credentialsId(credentialsId)
                    .modulesPaths(modulesPaths)
                    .sourcePath(resolveSourcePath(config))
                    .build()

            libraryConfig.retriever = resolveSourceRetriever(config)
            libraryConfig.validate()
            return libraryConfig

        }
    }

    @Override
    def initialize(Map configuration) {
        this.casc = configuration
        logger.system "Into PipelineRuntime init()"

        use(ValidationCategory) {
            this.configLibraries = configuration.validateAndGet('pipeline.globalLibraries.libraries').is(List.class).defaultValueIfInvalid([]) as List<Map>
            this.workingDir = configuration.validateAndGet('pipeline.workingDir').isString().throwIfInvalid()
        }
        logger.prettyPrint('SYSTEM', configuration)

        for (Map library : (configLibraries as List<Map>)) {

            LibraryConfiguration libraryConfiguration = createLibraryConfig(library)
            this.libraries.put(libraryConfiguration.name, libraryConfiguration)
            logger.system("Library loaded ${library.name}")
        }

        Map<String, String> imports = ["NonCPS" : "com.cloudbees.groovy.cps.NonCPS",
                                       "Library": "com.pipeline.runtime.library.Library"
        ]
        Map<String, String> staticImport = [
                "pipeline"  : "com.pipeline.runtime.PipelineRuntime",
                "initialize": "com.pipeline.runtime.PipelineRuntime",
                "library"   : "com.pipeline.runtime.PipelineRuntime",
                "scm"       : "com.pipeline.runtime.extensions.SCMAdapter"
        ]
        ClassLoader baseClassloader = this.class.getClassLoader()
        this.scriptEngine = CustomGroovyScriptEngine.create(scriptRoots, imports, staticImport, logger, libraries, baseClassloader)
        return this
    }

    @Override
    void run() throws IllegalAccessException, InstantiationException, IOException {
        Objects.requireNonNull(scriptEngine, "GroovyScriptEngine is not initialized: Initialize the helper by calling init().")

        def binding = new Binding()
        StepsExecutor script = scriptEngine.loadScript(jenkinsFile, binding)
        logger.system("Run script $jenkinsFile")
        script.initialize(casc)

        // TODO Revisar la carga de la libreria para que sea opcional o implicita
        libraries.each { libraryName, libraryConfiguration ->
            if (libraryConfiguration.retriever instanceof GitSource) {
                (libraryConfiguration.retriever as GitSource).steps = script
            }
            scriptEngine.libraryLoader.loadLibrary(libraryName)
        }
        scriptEngine.setGlobalVars(binding)
        script.run()
        logger.system("Post Run script $jenkinsFile")
    }

}
