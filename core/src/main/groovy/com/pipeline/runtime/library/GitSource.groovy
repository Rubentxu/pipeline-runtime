package com.pipeline.runtime.library

import com.pipeline.runtime.dsl.Steps
import com.pipeline.runtime.extensions.Branch
import com.pipeline.runtime.extensions.Scm
import com.pipeline.runtime.extensions.UserRemoteConfigs

import java.util.concurrent.TimeUnit

import groovy.transform.CompileStatic


//@CompileStatic
class GitSource implements SourceRetriever {

    String sourceURL
    Steps steps

    GitSource(String sourceURL, Steps steps) {
        assert sourceURL!=null && steps!=null : "GitSource necesita que sus dependencias no sean nulas"
        this.sourceURL = sourceURL
        this.steps = steps
    }

//    @Override
//    List<URL> retrieve(String repository, String branch, String targetPath) throws IllegalStateException {
//        File target = new File(targetPath)
//        def fetch = target.toPath().resolve("$repository@$branch").toFile()
//        if (fetch.exists()) {
//            return [fetch.toURI().toURL()]
//        } else {
//            fetch.parentFile.mkdirs()
//        }
//        def command = "git clone -b $branch --single-branch $sourceURL $repository@$branch"
//        println command
//        def processBuilder = new ProcessBuilder(command.split(' '))
//                .inheritIO()
//                .directory(target)
//        def proc = processBuilder.start()
//        proc.waitFor(CLONE_TIMEOUT_MIN, TimeUnit.MINUTES)
//        proc.exitValue()
//        return [fetch.toURI().toURL()]
//    }

    @Override
    List<URL> retrieve(String repository, String branch, String targetPath, String credentialsId) throws IllegalStateException {
        def remoteConfigs = [new UserRemoteConfigs(url: sourceURL,
                name: "$repository@$branch",
                refspec: branch,
                credentialsId: credentialsId
        )]

        def branches = [new Branch(name: branch)]
        def scm = new Scm(remoteConfigs, branches)
        steps.log.debug "CredentialsId with $credentialsId"
        steps.checkout scm
        File sourceDir = new File("${steps.getWorkingDir()}/${scm.userRemoteConfigs[0].name}")
        if (sourceDir.exists()) {
            return [sourceDir.toURI().toURL()]
        }
        throw new IllegalStateException("Directory $sourceDir.path does not exists")
    }

    static GitSource gitSource(String source, Steps steps) {
        new GitSource(source, steps)
    }

    @Override
    String toString() {
        return "GitSource{" +
                "sourceURL='" + sourceURL + '\'' +
                '}'
    }
}