package com.pipeline.runtime.library

import groovy.io.FileType
import groovy.transform.CompileStatic
import groovy.transform.Immutable

@Immutable
@CompileStatic
class LocalLib implements SourceRetriever {

    String sourceURL

    @Override
    List<URL> retrieve(String repository, String branch, String targetPath, String credentialsId) {
        def sourceDir = new File(sourceURL).toPath().toFile()
        if (sourceDir.exists()) {
            if(sourceDir.isDirectory()) {
                def list = []
                sourceDir.eachFileRecurse (FileType.FILES) { file ->
                    list << file.toURI().toURL()
                }
                return list
            }
            return [sourceDir.toURI().toURL()]
        }
        throw new IllegalStateException("Directory $sourceDir.path does not exists")
    }

    static LocalLib localLib(String source) {
        new LocalLib(source)
    }

    @Override
    String toString() {
        return "LocalSource{" +
                "sourceURL='" + sourceURL + '\'' +
                '}'
    }
}