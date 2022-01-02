package com.pipeline.runtime.extensions

import com.pipeline.runtime.dsl.StepsExecutor
import groovy.transform.CompileStatic
import org.eclipse.jgit.errors.UnsupportedCredentialItem
import org.eclipse.jgit.transport.CredentialItem
import org.eclipse.jgit.transport.CredentialsProvider
import org.eclipse.jgit.transport.URIish
import spock.lang.Specification

//@CompileStatic
class GitSCMSpec extends Specification {
    private static final String REMOTE_URL = "ssh://<user>:<pwd>@<host>:22/<path-to-remote-repo>/";
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
        // this is necessary when the remote host does not have a valid certificate, ideally we would install the certificate in the JVM
        // instead of this unsecure workaround!
//        CredentialsProvider allowHosts = new CredentialsProvider() {
//
//            @Override
//            public boolean supports(CredentialItem... items) {
//                for(CredentialItem item : items) {
//                    if((item instanceof CredentialItem.YesNoType)) {
//                        return true;
//                    }
//                }
//                return false;
//            }
//
//            @Override
//            public boolean get(URIish uri, CredentialItem... items) throws UnsupportedCredentialItem {
//                for(CredentialItem item : items) {
//                    if(item instanceof CredentialItem.YesNoType) {
//                        ((CredentialItem.YesNoType)item).setValue(true);
//                        return true;
//                    }
//                }
//                return false;
//            }
//
//            @Override
//            public boolean isInteractive() {
//                return false;
//            }
//        }
        File localPath = File.createTempFile("TestGitRepository", "");
        if(!localPath.delete()) {
            throw new IOException("Could not delete temporary file " + localPath);
        }

        println("Cloning from " + REMOTE_URL + " to " + localPath);
        def remoteConfig = new UserRemoteConfigs(url: REMOTE_URL, name: 'test', credentialsId: 'gitlab' )

        def scm = new Scm( new ArrayList<UserRemoteConfigs>(){{ add(remoteConfig)}},
                new ArrayList<Branch>(){{ add(new Branch(name:'main'))}})

        when:
        GitSCM.checkout(steps, scm)

        then:
        true


    }
}