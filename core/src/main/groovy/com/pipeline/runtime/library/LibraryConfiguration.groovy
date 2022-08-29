package com.pipeline.runtime.library

import groovy.transform.CompileStatic
import groovy.transform.ToString
import groovy.transform.builder.Builder
import groovy.transform.builder.ExternalStrategy

@CompileStatic
@ToString
class LibraryConfiguration {

    String name
    String sourcePath
    String defaultVersion = 'master'
    SourceRetriever retriever
    boolean implicit = false
    boolean allowOverride = true
    String targetPath
    String credentialsId
    List<String> modulesPaths

    LibraryConfiguration validate() {
        if (name && defaultVersion && retriever && targetPath)
            return this
        throw new IllegalStateException("LibraryConfiguration is not properly initialized ${this.toString()}")
    }


    @Builder(builderStrategy = ExternalStrategy, forClass = LibraryConfiguration)
    static class LibraryBuilder {

    }

}
