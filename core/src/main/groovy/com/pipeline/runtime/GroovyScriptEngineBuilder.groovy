package com.pipeline.runtime

import com.pipeline.runtime.interfaces.IRunnerLogger
import com.pipeline.runtime.library.LibraryAnnotationTransformer
import com.pipeline.runtime.library.LibraryConfiguration
import com.pipeline.runtime.library.LibraryLoader
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ImportCustomizer


class GroovyScriptEngineBuilder {
    Map<String, LibraryConfiguration> libraries
    Map<String, String> imports
    Map<String, String> staticImport
    String scriptExtension
    Class scriptBaseClass
    List<String> scriptRoots
    ClassLoader baseClassloader
    IRunnerLogger logger

    GroovyScriptEngineBuilder withLogger(IRunnerLogger logger) {
        this.logger = logger
        return this
    }

    GroovyScriptEngineBuilder withBaseClassLoader(ClassLoader baseClassloader) {
        this.baseClassloader = baseClassloader
        return this
    }

    GroovyScriptEngineBuilder withLibraries(Map<String, LibraryConfiguration> libraries) {
        this.libraries = libraries
        return this
    }

    GroovyScriptEngineBuilder withImports(Map<String, String> imports) {
        this.imports = imports
        return this
    }

    GroovyScriptEngineBuilder withStaticImport(Map<String, String> staticImport) {
        this.staticImport = staticImport
        return this
    }

    GroovyScriptEngineBuilder withScriptExtension(String scriptExtension) {
        this.scriptExtension = scriptExtension
        return this
    }

    GroovyScriptEngineBuilder withScriptBaseClass(Class scriptBaseClass) {
        this.scriptBaseClass = scriptBaseClass
        return this
    }

    GroovyScriptEngineBuilder withScriptRoots(List<String> scriptRoots) {
        this.scriptRoots = scriptRoots
        return this
    }

    CustomGroovyScriptEngine build() {
            CompilerConfiguration compilerConfiguration = new CompilerConfiguration(CompilerConfiguration.DEFAULT)
            GroovyClassLoader loader = new GroovyClassLoader(this.baseClassloader, compilerConfiguration)

            LibraryLoader libraryLoader = new LibraryLoader(loader, libraries, logger)
            LibraryAnnotationTransformer libraryTransformer = new LibraryAnnotationTransformer(libraryLoader)
            compilerConfiguration.addCompilationCustomizers(libraryTransformer)
//        compilerConfiguration.setWarningLevel(WarningMessage.NONE)

            ImportCustomizer importCustomizer = new ImportCustomizer()
            imports.each { k, v -> importCustomizer.addImport(k, v) }
            staticImport.each { k, v -> importCustomizer.addStaticImport(v, k) }
            compilerConfiguration.addCompilationCustomizers(importCustomizer)

            compilerConfiguration.setDefaultScriptExtension(scriptExtension)
            compilerConfiguration.setScriptBaseClass(scriptBaseClass.getName())
            def scriptEngine = new CustomGroovyScriptEngine(scriptRoots.toArray() as String[], libraryLoader)
            scriptEngine.setConfig(compilerConfiguration)
            return scriptEngine

    }
}
