package com.pipeline.runtime.extensions

import com.pipeline.runtime.dsl.StepsExecutor
import org.apache.commons.io.FileUtils
import org.eclipse.jgit.api.Git
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class GitSCMSteps {
    static Scm scm

    static def getScm(StepsExecutor self) {
        return scm
    }

    static def configureScm(StepsExecutor self, Map map) {
        scm = new Scm(map)
    }

    static def checkout(StepsExecutor self, final Scm scm) {
        println "+ checkout"
        File localPath = File.createTempFile(self.getWorkingDir(), "");

        Files.delete(localPath.toPath())

        def targetDirectory = scm.extensions.find { it.$class() == 'RelativeTargetDirectory' }?: ''
        Path targetPath = Paths.get("${self.workingDir}/${targetDirectory?:''}")

        if(Files.exists(targetPath)) {
            FileUtils.cleanDirectory(targetPath.toFile())
        }

        if(scm.userRemoteConfigs[0].credentialsId) {
            self.withCredentials([ self.usernamePassword(credentialsId: 'gitlab', usernameVariable: 'USER', passwordVariable: 'PASS')]) {
                println "Cogemos el USER: <${self.env.USER}>"
                println "Cogemos el PASS: <${self.env.PASS}>"
            }
        }

        Git.cloneRepository()
                .setURI(scm.userRemoteConfigs[0].url)
                .setDirectory(targetPath.toFile())
                .setBranchesToClone(scm.branches.collect {"refs/heads/${it.name}".toString() })
                .setBranch("refs/heads/${scm.branches[0].name}".toString())
                .call()
    }
}


class Scm  {
    String $class = 'GitSCM'
    List<UserRemoteConfigs> userRemoteConfigs
    List<Extension> extensions
    List<Branch> branches

    Scm(config) {
        this.userRemoteConfigs = config.userRemoteConfigs.collect{
            new UserRemoteConfigs(url: it.url,
                    name: it.name,
                    refspec: it.refspec,
                    credentialsId: it.credentialsId
            )
        }
        this.branches = config.branches as List<Branch>
    }
}

class Branch {
    String name
}

class UserRemoteConfigs {
    String url
    String name
    String refspec
    String credentialsId
}

trait Extension {
    String $class() { this.class.name }
}

class CleanCheckout implements Extension {}

class RelativeTargetDirectory implements Extension {
    String relativeTargetDir
}

class LocalBranch implements Extension {
    String localBranch
}

class SparseCheckoutPaths implements Extension {
    List<Path> sparseCheckoutPaths
    class Path {
        String path
    }
}




