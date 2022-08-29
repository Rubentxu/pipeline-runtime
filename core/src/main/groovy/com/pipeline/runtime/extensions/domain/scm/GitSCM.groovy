package com.pipeline.runtime.extensions.domain.scm

import java.util.concurrent.ConcurrentHashMap

class GitSCM {
    String url
    String branch = 'master'
    String credentialsId
    Boolean changelog = null
    Boolean poll = null
    Boolean doGenerateSubmoduleConfigurations = false
    Map<Class<? extends ScmExtensions>, ScmExtensions> extensions = new ConcurrentHashMap<>()

    GitSCM() {
    }


    GitSCM(Map configMap) {
        assert configMap.scm : "GitSCM configuration not found"

        def scm = configMap.scm
        assert scm.branches?.get(0).name : "GitSCM branches not found"
        assert scm.userRemoteConfigs?.get(0).url : "GitSCM userRemoteConfigs url not found"
        assert scm.userRemoteConfigs?.get(0).credentialsId : "GitSCM credentialsId url not found"

        branch(scm.branches?.get(0).name)
        this.doGenerateSubmoduleConfigurations = scm.doGenerateSubmoduleConfigurations?: false
        url(scm.userRemoteConfigs?.get(0).url)
        credentialsId(scm.userRemoteConfigs?.get(0).credentialsId)
        resolveExtensions(scm.extensions)
    }

    private resolveExtensions(List<Map> extensions) {
        extensions.each {
            switch (it.$class) {
                case 'CloneOption':
                    this.cloneOption(it.depth, it.timeout, it.noTags, it.shallow)
                    break
                case 'RelativeTargetDirectory':
                    this.relativeTargetDirectory(it.relativeTargetDir)
                    break
                case 'SparseCheckoutPaths':
                    this.sparseCheckoutPath(it.sparseCheckoutPaths.get(0).path.sparseCheckoutPath)
                    break

            }

        }
    }

    GitSCM url(String url) {
        this.url = url
        return this
    }

    GitSCM branch(String branch) {
        this.branch = branch
        return this
    }

    GitSCM credentialsId(String credentialsId) {
        this.credentialsId = credentialsId
        return this
    }

    GitSCM changelog(Boolean changelog) {
        this.changelog = changelog
        return this
    }

    GitSCM poll(Boolean poll) {
        this.poll = poll
        return this
    }

    GitSCM sparseCheckoutPath(String sparseCheckoutPath) {
        this.extensions.put(SparseCheckoutPath.class, new SparseCheckoutPath(sparseCheckoutPath))
        return this
    }

    GitSCM relativeTargetDirectory(String relativeTargetDirectory) {
        this.extensions.put(RelativeTargetDirectory.class, new RelativeTargetDirectory(relativeTargetDirectory))
        return this
    }

    String relativeTargetDirectory() {
        return extensions.get(RelativeTargetDirectory.class)?.relativeTargetDirectory?:''
    }

    GitSCM cloneOption( Integer depth, Integer timeout, Boolean noTags, Boolean shallow) {
        this.extensions.put(CloneOption.class, new CloneOption(depth: depth, timeout: timeout, noTags: noTags, shallow: shallow))
        return this
    }

    Map toMap() {
        List<Map> resolveExtensions = extensions.collect {it.value.toMap()}

        def scmMap = [scm: [$class                           : 'GitSCM',
                            branches                         : [[name: branch]],
                            doGenerateSubmoduleConfigurations: doGenerateSubmoduleConfigurations,
                            extensions                       : resolveExtensions,
                            submoduleCfg                     : [],
                            userRemoteConfigs                : [[credentialsId: credentialsId, url: url]]
        ]]

        if (changelog) scmMap.changelog = this.changelog
        if (poll) scmMap.poll = this.changelog
        return scmMap
    }

}



