package com.pipeline.runtime.extensions

import com.pipeline.runtime.dsl.StepsExecutor
import groovy.transform.CompileStatic
import org.apache.commons.io.FileUtils
import org.eclipse.jgit.api.CloneCommand
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.lib.RepositoryCache
import org.eclipse.jgit.transport.RefSpec
import org.eclipse.jgit.util.FS
import spock.lang.Shared
import spock.lang.Specification

import static org.mockito.Mockito.when
import static org.powermock.api.mockito.PowerMockito.mockStatic

//@CompileStatic
class GitSCMSpec extends Specification {
    @Shared
    Repository remoteRepo

    def setupSpec() {
        File remoteDir = File.createTempFile("remote", "")
        remoteDir.delete()
        remoteDir.mkdirs()

        // Create a bare repository
        RepositoryCache.FileKey fileKey = RepositoryCache.FileKey.exact(remoteDir, FS.DETECTED)
        remoteRepo = fileKey.open(false)
        remoteRepo.create(true)

        // Clone the bare repository
        File cloneDir = File.createTempFile("clone", "")
        cloneDir.delete()
        cloneDir.mkdirs()
        Git git = Git.cloneRepository()
                .setURI(remoteRepo.getDirectory().getAbsolutePath()).setDirectory(cloneDir)
                .call()

        // Let's to our first commit
        // Create a new file
        File newFile = new File(cloneDir, "myNewFile")
        newFile.createNewFile();
        FileUtils.writeStringToFile(newFile, "Test content file")
        // Commit the new file
        git.add()
                .addFilepattern(newFile.getName())
                .call()
        git.commit()
                .setMessage("First commit")
                .setAuthor("Rubentxu", "rubentxudev@gmail.com")
                .call()

        // Push the commit on the bare repository
        RefSpec refSpec = new RefSpec("master")
        git.push()
                .setRemote("origin")
                .setRefSpecs(refSpec)
                .call()
    }

    def "Debe clonar un repositorio"() {
        given:
        StepsExecutor steps =  new StepsExecutor()
        steps.getWorkingDir = {
            '/tmp'
        }
        steps.getTypeCredentials = { 'username_password' }
        steps.usernamePassword = { params->
            steps.env[params.usernameVariable] = 'userGradle'
            steps.env[params.passwordVariable] = 'passwordGradle'
        }

        def remoteConfig = new UserRemoteConfigs(url: remoteRepo.getDirectory().getAbsolutePath(), name: 'test', credentialsId: 'gitlab' )

        def scm = new Scm( new ArrayList<UserRemoteConfigs>(){{ add(remoteConfig)}},
                new ArrayList<Branch>(){{ add(new Branch(name:'master'))}})

        when:
        GitSCM.checkout(steps, scm)

        then:
        true


    }
}