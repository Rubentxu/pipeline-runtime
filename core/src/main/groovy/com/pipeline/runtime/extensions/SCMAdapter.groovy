package com.pipeline.runtime.extensions


import com.pipeline.runtime.dsl.StepsExecutor
import com.pipeline.runtime.extensions.domain.scm.GitSCM
import com.pipeline.runtime.extensions.domain.scm.RelativeTargetDirectory
import com.pipeline.runtime.validations.ValidationCategory
import org.apache.commons.io.FileUtils
import org.eclipse.jgit.api.CloneCommand
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class SCMAdapter {
    static GitSCM scm

    static def getScm(StepsExecutor self) {
        return scm
    }

    static def configureScm(StepsExecutor self, Map config) {
        use(ValidationCategory) {
            scm = new GitSCM()
                    .credentialsId(config.validateAndGet('pipeline.scm.gitscm.userRemoteConfigs').is(List.class).defaultValueIfInvalid([])?.get(0).credentialsId)
                    .url(config.validateAndGet('pipeline.scm.gitscm.userRemoteConfigs').is(List.class).defaultValueIfInvalid([]).get(0).url)
                    .branch(config.validateAndGet('pipeline.scm.gitscm.branches').is(List.class).defaultValueIfInvalid([]).get(0).name)
            self.env.BRANCH_NAME = scm.branch
        }
    }

    static def checkout(StepsExecutor self, Map configMap) {
        def gitSCM = new GitSCM(configMap)
        checkout(self, gitSCM)
    }

    static def checkout(StepsExecutor self, final GitSCM scm) {
        self.log.info "+ checkout"
        File cloneDir = new File("${self.getWorkingDir()}")
        cloneDir.delete()
        cloneDir.mkdirs()

        String targetDirectory = scm.extensions.get(RelativeTargetDirectory.class)?.relativeTargetDirectory ?: ''
        Path targetPath = Paths.get("${cloneDir.absolutePath}/${targetDirectory ?: ''}").normalize()

        if (Files.exists(targetPath)) {
            FileUtils.cleanDirectory(targetPath.toFile())
        }
        self.log.info "+ checkout in Path ${targetPath.toString()}"
        CloneCommand gitBuilder = Git.cloneRepository()
                .setURI(scm.url)
                .setDirectory(targetPath.toFile())
                .setBranchesToClone(["refs/heads/${scm.branch}".toString()])
                .setBranch("refs/heads/${scm.branch}".toString())

        String credentialsId = scm.credentialsId
        if (credentialsId) {
            def typeCredential = self.getTypeCredentials(credentialsId)
            self.log.system "CredentialId with name $credentialsId and type $typeCredential defined".toString()
            if (typeCredential == 'username_password') {
                self.withCredentials([self.usernamePassword(credentialsId: credentialsId,
                        usernameVariable: 'USER', passwordVariable: 'PASS')]) {
                    gitBuilder.setCredentialsProvider(new UsernamePasswordCredentialsProvider(self.env.USER, self.env.PASS))
                }
            } else if (typeCredential == 'secret_text' || typeCredential == 'string') {
                self.withCredentials([self.string(credentialsId: credentialsId,
                        variable: 'TOKEN')]) {
                    gitBuilder.setCredentialsProvider(new UsernamePasswordCredentialsProvider('oauth2', self.env.TOKEN))
                }
            } else if (typeCredential == null) {
                throw new Exception("Checkout Error -- CredentialsId '$credentialsId' Not Found")
            }

        }

        gitBuilder.call()
        self.log.system "Call repository ${gitBuilder.directory.name}"

    }
}




