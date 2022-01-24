package com.pipeline.runtime.library

import groovy.transform.CompileStatic

@CompileStatic
interface SourceRetriever {
    List<URL> retrieve(String repository, String branch, String targetPath, String credentialsId)
}
