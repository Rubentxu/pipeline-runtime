package com.pipeline.runtime.library

import groovy.transform.CompileStatic
import groovy.transform.ToString
import groovy.transform.builder.Builder
import groovy.transform.builder.ExternalStrategy

/**
 * Mock for org.jenkinsci.plugins.workflow.libs.LibraryConfiguration
 */
@CompileStatic
@ToString
class LibraryConfiguration {

    String name
    String defaultVersion = 'master'
    SourceRetriever retriever
    boolean implicit = false
    boolean allowOverride = true
    String targetPath
    String credentialsId

    LibraryConfiguration validate() {
        if (name && defaultVersion && retriever && targetPath)
            return this
        throw new IllegalStateException("LibraryConfiguration is not properly initialized ${this.toString()}")
    }

    static LibraryBuilder library(String libName = null) {
        return new LibraryBuilder() {
            LibraryConfiguration build() { return super.build().validate() }
        }.with { it.name(libName) }
    }

    @Builder(builderStrategy = ExternalStrategy, forClass = LibraryConfiguration)
    static class LibraryBuilder {

    }

}