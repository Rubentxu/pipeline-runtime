package com.pipeline.runtime.extensions.scm.helpers

import com.pipeline.runtime.dsl.Context
import groovy.transform.CompileStatic

@CompileStatic
class GitSubmoduleOptionsContext implements Context {
    boolean disable
    boolean recursive
    boolean tracking
    String reference
    Integer timeout
    boolean parentCredentials

    /**
     * Disables submodules processing. Defaults to {@code false}.
     */
    void disable(boolean disable = true) {
        this.disable = disable
    }

    /**
     * Retrieves all submodules recursively. Defaults to {@code false}.
     */
    void recursive(boolean recursive = true) {
        this.recursive = recursive
    }

    /**
     * Retrieves the tip of the configured branch in {@code .gitmodules}. Defaults to {@code false}.
     */
    void tracking(boolean tracking = true) {
        this.tracking = tracking
    }

    /**
     * Specifies a folder containing a repository that will be used by Git as a reference during clone operations.
     *
     * @since 1.46
     */
    void reference(String reference) {
        this.reference = reference
    }

    /**
     * Specifies a timeout (in minutes) for submodules operations.
     *
     * @since 1.46
     */
    void timeout(Integer timeout) {
        this.timeout = timeout
    }

    /**
     * Allows to use credentials from the default remote of the parent project.
     *
     * @since 1.54
     */
    void parentCredentials(boolean parentCredentials = true) {
        this.parentCredentials = parentCredentials
    }
}
