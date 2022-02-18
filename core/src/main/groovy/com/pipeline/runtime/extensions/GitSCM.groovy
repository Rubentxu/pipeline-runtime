package com.pipeline.runtime.extensions

import com.pipeline.runtime.ServiceLocator
import com.pipeline.runtime.dsl.StepsExecutor
import com.pipeline.runtime.interfaces.IConfiguration
import com.pipeline.runtime.interfaces.ILogger
import groovy.transform.ToString
import org.apache.commons.io.FileUtils
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class GitSCM {
    static Scm scm

    static def getScm(StepsExecutor self) {
        return scm
    }

    static def configureScm(StepsExecutor self) {
        scm = new Scm(self.configuration)
    }

    static def checkout(StepsExecutor self, final Scm scm) {
        self.log.info "+ checkout"
        File cloneDir = new File("${self.getWorkingDir()}/${scm.userRemoteConfigs[0].name}");
//        cloneDir.delete()
        cloneDir.mkdirs()

        def targetDirectory = scm.extensions.find { it.$class() == 'RelativeTargetDirectory' }?: ''
        Path targetPath = Paths.get("${cloneDir.absolutePath}/${targetDirectory?:''}")

        if(Files.exists(targetPath)) {
            FileUtils.cleanDirectory(targetPath.toFile())
        }
        self.log.info "+ checkout in Path ${targetPath.toString()}"
        def gitBuilder = Git.cloneRepository()
                .setURI(scm.userRemoteConfigs[0].url)
                .setDirectory(targetPath.toFile())
                .setBranchesToClone(scm.branches.collect {"refs/heads/${it.name}".toString() })
                .setBranch("refs/heads/${scm.branches[0].name}".toString())

        def credentialsId = scm.userRemoteConfigs[0]?.credentialsId
        if(credentialsId) {
            def typeCredential = self.getTypeCredentials(credentialsId)
            self.log.debug "CredentialId with name $credentialsId and type $typeCredential defined"
            if(typeCredential == 'username_password') {
                self.withCredentials([ self.usernamePassword(credentialsId: credentialsId,
                        usernameVariable: 'USER', passwordVariable: 'PASS')]) {
                    gitBuilder.setCredentialsProvider(new UsernamePasswordCredentialsProvider( self.env.USER, self.env.PASS ))
                }
            } else  if(typeCredential == 'secret_text') {
                self.withCredentials([ self.string(credentialsId: credentialsId,
                        variable: 'TOKEN')]) {
                    gitBuilder.setCredentialsProvider(new UsernamePasswordCredentialsProvider( self.env.TOKEN, '' ))
                }
            }
        }
        gitBuilder.call()
        self.log.debug "Call repository"

    }
}

@ToString
class Scm  {
    String $class = 'GitSCM'
    List<UserRemoteConfigs> userRemoteConfigs
    List<Extension> extensions
    List<Branch> branches

    Scm(IConfiguration config) {
        this.userRemoteConfigs = config.getValue('pipeline.scm.gitscm.userRemoteConfigs').collect{
            new UserRemoteConfigs(url: it.url,
                    name: it.name,
                    refspec: it.refspec,
                    credentialsId: it.credentialsId
            )
        }
        this.branches = config.getValue('pipeline.scm.gitscm.branches') as List<Branch>
        ServiceLocator.getService(ILogger).debug "SCM Create configuration ${this.toString()}"
    }

    Scm(userRemoteConfigs, branches) {
        this.userRemoteConfigs = userRemoteConfigs
        this.branches = branches
        ServiceLocator.getService(ILogger).debug "SCM Create configuration ${this.toString()}"
    }
}

@ToString
class Branch {
    String name
}

@ToString
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

@ToString
class SparseCheckoutPaths implements Extension {
    List<Path> sparseCheckoutPaths
    class Path {
        String path
    }
}




