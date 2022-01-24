package com.pipeline.runtime.library

import groovy.transform.CompileStatic
import groovy.transform.ToString

@ToString
@CompileStatic
class LibraryRecord {

    LibraryConfiguration configuration
    String version
    List<String> rootPaths

    Map<String, Object> definedGlobalVars

    LibraryRecord(LibraryConfiguration configuration, String version, List<String> rootPaths) {
        this.configuration = configuration
        this.version = version
        this.rootPaths = rootPaths
    }

    String getIdentifier() {
        return "$configuration.name@$version"
    }

}
