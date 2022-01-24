package com.pipeline.runtime.library

import com.pipeline.runtime.ServiceLocator
import com.pipeline.runtime.interfaces.ILoggerService

import static groovy.io.FileType.FILES
import groovy.lang.GroovyCodeSource
import java.nio.file.Files
import java.util.function.Consumer
import java.util.function.Predicate

import org.apache.commons.io.FilenameUtils
import org.codehaus.groovy.runtime.DefaultGroovyMethods

import groovy.transform.CompileStatic

/**
 * Loads global shared libraries to groovy class loader
 */
@CompileStatic
class LibraryLoader {

    private final GroovyClassLoader groovyClassLoader

    private final Map<String, LibraryConfiguration> libraryDescriptions

    //Set to `false` *only* if testing libs that have vars steps that accept lib
    //class instances as arguments *when* the default `true` breaks test,
    //throwing MissingMethodExceptions like the following.
    //
    //  No signature of method: `
    //  JENKINSFILE.monster1() is applicable for argument types: `
    //  (org.test.Monster1) values: [org.test.Monster1@45f50182]
    //
    //Lib class preload in this case can lead to the class being loaded
    //too many times, confusing the method interceptor logic at runtime.
    //
    //Warning: Setting to false can break other tests, e.g. ones where lib
    //classes want to access the binding env.
    Boolean preloadLibraryClasses = true

    final Map<String, LibraryRecord> libRecords = new HashMap<>()

    LibraryLoader(GroovyClassLoader groovyClassLoader, Map<String, LibraryConfiguration> libraryDescriptions) {
        this.groovyClassLoader = groovyClassLoader
        this.libraryDescriptions = libraryDescriptions
    }

    static String getLibraryId(LibraryConfiguration lib, String version = null) {
        return "$lib.name@${version ?: lib.defaultVersion}"
    }

    /**
     * Return class loader for all libraries
     * @return return class loader
     */
    GroovyClassLoader getGroovyClassLoader() {
        return groovyClassLoader
    }

    /**
     * Loads all implicit library configurations
     */
    void loadImplicitLibraries() {
        libraryDescriptions.values().stream()
                .filter { it.implicit }
                .filter { !libRecords.containsKey(getLibraryId(it)) }
                .forEach {
                    doLoadLibrary(it)
                }
    }

    /**
     * Load library described by expression if it corresponds to a known library configuration
     * @param expression
     * @throws Exception
     */
    void loadLibrary(String expression) throws Exception {
        def lib = parse(expression)
        def libName = lib[0]
        def version = lib[1]
        def library = libraryDescriptions.get(libName)
        if (!library) {
            throw new Exception("Library description '$libName' not found")
        }
        if (!matches(libName, version, library)) {
            throw new Exception("Library '$expression' does not match description $library")
        }
        if (!libRecords.containsKey(getLibraryId(library, version))) {
            doLoadLibrary(library, version)
        }
    }

    /**
     * Loads library to groovy class loader.
     * @param library library configuration.
     * @param version version to load, if null loads the default version defined in configuration.
     * @throws Exception
     */
    private void doLoadLibrary(LibraryConfiguration library, String version = null) throws Exception {
        def logger = ServiceLocator.getService(ILoggerService.class)
        logger.info "Loading shared library ${library.name} with version ${version ?: library.defaultVersion}"
        try {
            def urls = library.retriever.retrieve(library.name, version ?: library.defaultVersion, library.targetPath, library.credentialsId)
            def record = new LibraryRecord(library, version ?: library.defaultVersion, urls.path)
            logger.info "Library Record ${record}"
            logger.info "Library URLS ${urls}"
            libRecords.put(record.getIdentifier(), record)
            def globalVars = [:]
            urls.forEach { URL url ->
                def file = new File(url.toURI())
                logger.info "Global library available in path ${file.toPath()}"
                def srcPath = file.toPath().resolve('src')
                def varsPath = file.toPath().resolve('vars')
                def resourcesPath = file.toPath().resolve('resources')
                groovyClassLoader.addURL(srcPath.toUri().toURL())
                groovyClassLoader.addURL(varsPath.toUri().toURL())
                groovyClassLoader.addURL(resourcesPath.toUri().toURL())

                if (varsPath.toFile().exists()) {
                    def ds = Files.list(varsPath)
                    ds.map { it.toFile() }
                            .filter ({File it -> it.name.endsWith('.groovy') } as Predicate<File>)
                            .map { FilenameUtils.getBaseName(it.name) }
                            .filter ({String it -> !globalVars.containsValue(it) } as Predicate<String>)
                            .forEach ({ String it ->
                                def clazz = groovyClassLoader.loadClass(it)
                                // instantiate by invokeConstructor to avoid interception
                                Object var = DefaultGroovyMethods.newInstance(clazz)
                                globalVars.put(it, var)
                            } as Consumer<String>)
                    // prevent fd leak on the DirectoryStream from Files.list()
                    ds.close()
                }

                // pre-load library classes using JPU groovy class loader
                if (preloadLibraryClasses && srcPath.toFile().exists()) {
                    srcPath.toFile().eachFileRecurse (FILES) { File srcFile ->
                        if (srcFile.name.endsWith(".groovy")) {
                            Class clazz = groovyClassLoader.parseClass(srcFile)
                            groovyClassLoader.loadClass(clazz.name)
                        }
                    }
                }
            }
            record.definedGlobalVars = globalVars as Map<String, Object>
        } catch (Exception e) {
            throw new LibraryLoadingException(e, library, version)
        }
    }

    private static String[] parse(String identifier) {
        identifier.split('@')
        int at = identifier.indexOf('@')
        if (at == -1) {
            return [identifier, null] as String[] // pick up defaultVersion
        } else {
            return [identifier.substring(0, at), identifier.substring(at + 1)] as String[]
        }
    }

    private static boolean matches(String libName, String version, LibraryConfiguration libraryDescription) {
        if (libraryDescription.name == libName) {
            if (version == null) {
                return true
            }
            if (libraryDescription.allowOverride || libraryDescription.defaultVersion == version) {
                return true
            }
        }
        return false
    }

    static class LibraryLoadingException extends Exception {

        LibraryLoadingException(Throwable cause, LibraryConfiguration configuration, String version) {
            super("Error on loading library ${LibraryLoader.getLibraryId(configuration, version)} : ${cause.message}", cause)
        }
    }

}
