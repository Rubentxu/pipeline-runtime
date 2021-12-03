package com.pipeline.runtime.dsl.traits


import org.eclipse.jgit.api.Git
import java.nio.file.Files

trait GitSCMSteps {
    void node(final Closure closure) {
        node("default", closure)
    }

    def checkout(final Scm scm) {
        File localPath = File.createTempFile("build/workspace", "");

        Files.delete(localPath.toPath())

        def targetDirectory = scm.extensions.find { it.$class() == 'RelativeTargetDirectory' }?: ''
        targetDirectory = "build/workspace/${targetDirectory?:''}"
        Git.cloneRepository()
                .setURI(scm.userRemoteConfigs[0].url)
                .setDirectory(new File(targetDirectory))
                .setBranchesToClone(scm.branches.collect {it.name })
                .setBranch(scm.branches[0].name)
                .call()
    }
}


class Scm  {
    String $class = 'GitSCM'
    List<UserRemoteConfigs> userRemoteConfigs = []
    List<Extension> extensions = [:]
    List<Branch> branches = [[ name: 'master']]

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




