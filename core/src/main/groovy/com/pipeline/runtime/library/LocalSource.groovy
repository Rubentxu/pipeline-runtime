package com.pipeline.runtime.library

import groovy.transform.CompileStatic
import groovy.transform.Immutable

@Immutable
@CompileStatic
class LocalSource implements SourceRetriever {

    String sourceURL

    @Override
    List<URL> retrieve(String repository, String branch, String targetPath, String credentialsId) {
        def sourceDir = new File(sourceURL).toPath().toFile()
        if (sourceDir.exists()) {
            return [sourceDir.toURI().toURL()]
        }
        throw new IllegalStateException("Directory $sourceDir.path does not exists")
    }

    static LocalSource localSource(String source) {
        new LocalSource(source)
    }

    @Override
    String toString() {
        return "LocalSource{" +
                "sourceURL='" + sourceURL + '\'' +
                '}'
    }
}