package com.pipeline.runtime.library

import groovy.transform.CompileStatic

import java.nio.file.Path

//@CompileStatic
interface SourceRetriever {
    List<URL> retrieve(LibraryConfiguration configuration)
}
