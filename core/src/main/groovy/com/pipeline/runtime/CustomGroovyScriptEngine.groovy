package com.pipeline.runtime

import com.pipeline.runtime.dsl.StepsExecutor
import com.pipeline.runtime.interfaces.IRunnerLogger
import com.pipeline.runtime.library.LibClassLoader
import com.pipeline.runtime.library.LibraryConfiguration
import com.pipeline.runtime.library.LibraryLoader
import org.codehaus.groovy.runtime.InvokerHelper

import java.lang.reflect.Method

class CustomGroovyScriptEngine extends GroovyScriptEngine {
    private static Method SCRIPT_SET_BINDING = Script.getMethod('setBinding', Binding.class)
    LibraryLoader libraryLoader

    CustomGroovyScriptEngine(String[] urls, LibraryLoader libraryLoader) throws IOException {
        super(urls, libraryLoader.getGroovyClassLoader())
        this.libraryLoader = libraryLoader
    }

    StepsExecutor loadScript(String scriptName, Binding binding) {
        Objects.requireNonNull(binding, "Binding cannot be null.")
        Class scriptClass = this.loadScriptByName(scriptName)
        return  InvokerHelper.createScript(scriptClass, binding) as StepsExecutor
    }

    /**
     * Sets global variables defined in loaded libraries on the binding
     * @param binding
     */
    public void setGlobalVars(Binding binding) {
        this.libraryLoader.libRecords.values().stream()
                .flatMap { it.definedGlobalVars.entrySet().stream() }
                .forEach { e ->
                    if (e.value instanceof Script) {
                        Script script = Script.cast(e.value)
                        // invoke setBinding from method to avoid interception
//                        script.setDelegate(this.script)
                        SCRIPT_SET_BINDING.invoke(script, binding)
                    }
                    binding.setVariable(e.key, e.value)
                }
    }

    static GroovyScriptEngine create(
            List<String> scriptRoots,
            Map<String, String> imports,
            Map<String, String> staticImport,
            IRunnerLogger logger,
            Map<String, LibraryConfiguration> libraries,
            ClassLoader baseClassloader,
            Class scriptBaseClass = StepsExecutor.class,
            String scriptExtension = "groovy"

    ) {

        return new GroovyScriptEngineBuilder()
                .withBaseClassLoader(baseClassloader)
                .withLogger(logger)
                .withStaticImport(staticImport)
                .withImports(imports)
                .withScriptExtension(scriptExtension)
                .withScriptBaseClass(scriptBaseClass)
                .withScriptRoots(scriptRoots)
                .withLibraries(libraries)
                .build()
    }

    static LibClassLoader library(Map args) {
        assert args.identifier
//        libLoader.loadImplicitLibraries()
//        libLoader.loadLibrary(args.identifier)
//        setGlobalVars(script.getBinding())
        return new LibClassLoader(null)
    }

    static LibClassLoader library(String expression) {
//        libLoader.loadImplicitLibraries()
//        libLoader.loadLibrary(expression)
//        setGlobalVars(script.getBinding())
        return new LibClassLoader(null)
    }
}
