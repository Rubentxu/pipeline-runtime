package com.pipeline.runtime.library

import groovy.io.FileType
import groovy.transform.CompileStatic
import groovy.transform.ToString

@ToString
@CompileStatic
class LocalLib implements SourceRetriever {

    @Override
    List<URL> retrieve(LibraryConfiguration configuration) {
        def sourceDir = new File(configuration.sourcePath).toPath().toFile()
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

    static LocalLib localLib() {
        new LocalLib()
    }

}
