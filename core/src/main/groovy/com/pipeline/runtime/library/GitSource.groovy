package com.pipeline.runtime.library

import com.pipeline.runtime.dsl.Steps
import com.pipeline.runtime.extensions.domain.scm.GitSCM
import groovy.transform.ToString

import java.nio.file.Path

//@CompileStatic
@ToString
class GitSource implements SourceRetriever {

    Steps steps

    @Override
    List<URL> retrieve(LibraryConfiguration configuration) throws IllegalStateException {

        String relativePath = "../libs/${configuration.name}"
        def scm = new GitSCM()
                .branch(configuration.defaultVersion)
                .credentialsId(configuration.credentialsId)
                .relativeTargetDirectory(relativePath)
                .url(configuration.sourcePath)

        steps.log.system "CredentialsId with $configuration.credentialsId"
        steps.checkout scm.toMap()
        File sourceDir = Path.of(steps.getWorkingDir(),relativePath).normalize().toFile()

        if (sourceDir.exists()) {
            if(configuration.modulesPaths) {
                List<URL> sources = configuration.modulesPaths.collect {
                    sourceDir.toPath().normalize().resolve(it).normalize().toUri().toURL()
                }
                return sources
            }
            return [sourceDir.toURI().toURL()]
        }
        throw new IllegalStateException("Directory $sourceDir.path does not exists")
    }

    static GitSource gitSource() {
        new GitSource()
    }

}
