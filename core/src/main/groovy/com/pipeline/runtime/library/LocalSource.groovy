package com.pipeline.runtime.library

import groovy.transform.CompileStatic
import groovy.transform.ToString

@CompileStatic
@ToString
class LocalSource implements SourceRetriever {

    @Override
    List<URL> retrieve(LibraryConfiguration configuration) {
        def sourceDir = new File(configuration.sourcePath).toPath().toFile()
        if (sourceDir.exists()) {
            if(configuration.modulesPaths) {
                List<URL> sources = configuration.modulesPaths.collect {
                    sourceDir.toPath().resolve(it).toUri().toURL()
                }
                return sources
            }
            return [sourceDir.toURI().toURL()]
        }
        throw new IllegalStateException("Directory $sourceDir.path does not exists")
    }

    static LocalSource localSource() {
        new LocalSource()
    }

}
