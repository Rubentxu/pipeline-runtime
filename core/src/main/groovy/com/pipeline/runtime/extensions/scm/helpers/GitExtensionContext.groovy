package com.pipeline.runtime.extensions.scm.helpers

import com.pipeline.runtime.dsl.Context
import com.pipeline.runtime.dsl.DslContext
import groovy.transform.CompileStatic

import static com.pipeline.runtime.dsl.ContextHelper.executeInContext

//@CompileStatic
class GitExtensionContext implements Context {
    /**
     * Allows to perform a merge to a particular branch before building.
     * Can be called multiple times to merge more than one branch.
     */
    void mergeOptions(@DslContext(GitMergeOptionsContext) Closure gitMergeOptionsClosure) {
        GitMergeOptionsContext gitMergeOptionsContext = new GitMergeOptionsContext()
        executeInContext(gitMergeOptionsClosure, gitMergeOptionsContext)

    }

    /**
     * Cleans up the workspace after every checkout by deleting all untracked files and directories, including those
     * which are specified in {@code .gitignore}.
     */
    void cleanAfterCheckout() {

    }

    /**
     * Clean up the workspace before every checkout by deleting all untracked files and directories, including those
     * which are specified in {@code .gitignore}.
     */
    void cleanBeforeCheckout() {

    }

    /**
     * Specifies behaviors for cloning repositories.
     */
    void cloneOptions(@DslContext(GitCloneOptionsContext) Closure closure) {
        GitCloneOptionsContext context = new GitCloneOptionsContext()
        executeInContext(closure, context)


    }

    /**
     * Specifies behaviors for handling sub-modules.
     */
    void submoduleOptions(@DslContext(GitSubmoduleOptionsContext) Closure closure) {
        GitSubmoduleOptionsContext context = new GitSubmoduleOptionsContext()
        executeInContext(closure, context)


    }

    /**
     * Delete the contents of the workspace before building, ensuring a fully fresh workspace.
     */
    void wipeOutWorkspace() {

    }

    /**
     * Polls by using the workspace and disables the {@code git ls-remote} polling mechanism.
     */
    void disableRemotePoll() {

    }

    /**
     * Prunes obsolete local branches.
     */
    void pruneBranches() {

    }

    /**
     * If given, checkout the revision to build as HEAD on this branch.
     *
     * @since 1.53
     */
    void localBranch() {
        localBranch(null)
    }

    /**
     * If given, checkout the revision to build as HEAD on this branch.
     */
    void localBranch(String branch) {

    }

    /**
     * Specifies a local directory (relative to the workspace root) where the Git repository will be checked out.
     */
    void relativeTargetDirectory(String relativeTargetDirectory) {

    }

    /**
     * If set, the repository will be ignored when the notifyCommit-URL is accessed.
     */
    void ignoreNotifyCommit() {

    }

    /**
     * Creates a tag in the workspace for every build to unambiguously mark the commit that was built.
     */
    void perBuildTag() {

    }

    /**
     * Sets the strategy that Jenkins will use to choose what branches to build in what order.
     */
    void choosingStrategy(@DslContext(StrategyContext) Closure strategyClosure) {
        StrategyContext strategyContext = new StrategyContext()
        executeInContext(strategyClosure, strategyContext)

    }

//    @Override
    protected void addExtensionNode(Node node) {
        extensions << node
    }
}
