package com.pipeline.runtime.library

import groovy.transform.CompileStatic

@CompileStatic
interface SourceRetriever {

    public static final int CLONE_TIMEOUT_MIN = 10

    List<URL> retrieve(String repository, String branch, String targetPath) throws IllegalStateException

}
