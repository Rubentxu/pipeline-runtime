package com.pipeline.runtime.extensions.scm.helpers

import com.pipeline.runtime.dsl.Context
import groovy.transform.CompileStatic

@CompileStatic
class GitCloneOptionsContext implements Context {
    boolean shallow
    boolean noTags
    String reference
    Integer timeout
    boolean honorRefspec
    int depth

    /**
     * Perform shallow clone, so that Git will not download history of the project. Defaults to {@code false}.
     */
    void shallow(boolean shallow = true) {
        this.shallow = shallow
    }

    /**
     * Do not check out tags. Defaults to {@code false}.
     *
     * @since 1.64
     */
    void noTags(boolean noTags = true) {
        this.noTags = noTags
    }

    /**
     * Specify a folder containing a repository that will be used by Git as a reference during clone operations.
     */
    void reference(String reference) {
        this.reference = reference
    }

    /**
     * Specify a timeout (in minutes) for clone and fetch operations.
     */
    void timeout(Integer timeout) {
        this.timeout = timeout
    }

    /**
     * Honor refspec on initial clone.
     *
     * @since 1.52
     */
    void honorRefspec(boolean honorRefspec = true) {
        this.honorRefspec = honorRefspec
    }

    /**
     * Set shallow clone depth, so that git will only download recent history of the project, saving time and
     * disk space when you just want to access the latest version of a repository.
     *
     * @since 1.68
     */
    void depth(int depth) {
        this.depth = depth
    }
}