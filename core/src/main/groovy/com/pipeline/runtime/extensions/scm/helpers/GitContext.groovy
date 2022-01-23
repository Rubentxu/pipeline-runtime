package com.pipeline.runtime.extensions.scm.helpers

import com.pipeline.runtime.dsl.DslContext
import com.pipeline.runtime.extensions.scm.RemoteContext
import groovy.transform.CompileStatic

import static com.pipeline.runtime.dsl.ContextHelper.executeInContext

@CompileStatic
class GitContext {
    List<RemoteContext> remoteConfigs = []
    List<String> branches = []
    Closure configureBlock
    GitExtensionContext extensionContext = new GitExtensionContext()


    /**
     * Adds a remote. Can be repeated to add multiple remotes.
     */
    void remote(@DslContext(RemoteContext) Closure remoteClosure) {
        RemoteContext remoteContext = new RemoteContext()
        executeInContext(remoteClosure, remoteContext)
        remoteConfigs << remoteContext

    }

    /**
     * Specify the branches to examine for changes and to build.
     */
    void branch(String branch) {
        this.branches.add(branch)
    }

    /**
     * Specify the branches to examine for changes and to build.
     */
    void branches(String... branches) {
        this.branches.addAll(branches)
    }


    /**
     * Allows direct manipulation of the generated XML. The {@code scm} node is passed into the configure block.
     *
     * @see <a href="https://github.com/jenkinsci/job-dsl-plugin/wiki/The-Configure-Block">The Configure Block</a>
     */
    void configure(Closure configureBlock) {
        this.configureBlock = configureBlock
    }

    /**
     * Adds additional behaviors.
     *
     * @since 1.44
     */
    void extensions(@DslContext(GitExtensionContext) Closure closure) {
        executeInContext(closure, extensionContext)
    }
}
