package com.pipeline.runtime


import com.pipeline.runtime.library.LibraryAnnotationTransformer
import com.pipeline.runtime.library.LibraryConfiguration
import com.pipeline.runtime.library.LibraryLoader
import com.pipeline.runtime.library.MethodSignature
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ImportCustomizer
import org.codehaus.groovy.runtime.MetaClassHelper
import org.yaml.snakeyaml.Yaml

import java.lang.reflect.Method

import static com.pipeline.runtime.library.LibraryConfiguration.library
import static com.pipeline.runtime.library.LocalSource.localSource
import static com.pipeline.runtime.library.MethodSignature.method

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
    Map<String, String> imports = ["NonCPS": "com.cloudbees.groovy.cps.NonCPS"]
    String baseScriptRoot = "."
    Binding binding
    ClassLoader baseClassloader = this.class.classLoader

    PipelineRuntime(String jenkinsFile, String configFile, String libraryPath='.') {
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

//            return new LibClassLoader(this, null)
        })

        setGlobalVars(binding)
        def script = gse.createScript(toFullPath(jenkinsFile), binding)
        script.run()

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
                        script.metaClass.getMethods().findAll { it.name == 'call' }.forEach { m ->
                            this.registerAllowedMethod(MethodSignature.method(e.value.class.name, m.getNativeParameterTypes()).name,
                                    { args ->
                                        // When calling a one argument method with a null argument the
                                        // Groovy doMethodInvoke appears to incorrectly assume a zero
                                        // argument call signature for the method yielding an IllegalArgumentException
                                        if (args == null && m.getNativeParameterTypes().size() == 1) {
                                            m.doMethodInvoke(e.value, MetaClassHelper.ARRAY_WITH_NULL)
                                        } else {
                                            m.doMethodInvoke(e.value, args)
                                        }
                                    } )
                        }
                    }
                    binding.setVariable(e.key, e.value)
                }
    }

    /**
     * @param name method name
     * @param closure method implementation, can be null
     */
    void registerAllowedMethod(String name, Closure closure = null) {
        allowedMethodCallbacks.put(method(name), closure)
    }


    /**
     * List of allowed methods with default interceptors.
     * Complete this list in need with {@link #registerAllowedMethod}
     */
    protected Map<MethodSignature, Closure> allowedMethodCallbacks = [:]
//            (method("load", String.class))           : loadInterceptor,
//            (method("parallel", Map.class))          : parallelInterceptor,
//            (method("libraryResource", String.class)): libraryResourceInterceptor,
//    ]

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
        gse = new GroovyScriptEngine(scriptRoots.toArray() as String[], loader)
        gse.setConfig(configuration)
        getLibLoader().loadLibrary("commons")
        println "commons libs loaders ${getLibLoader().libRecords}"
        return this
    }


}
