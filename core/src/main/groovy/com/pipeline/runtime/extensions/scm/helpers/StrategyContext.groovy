package com.pipeline.runtime.extensions.scm.helpers

import com.pipeline.runtime.dsl.Context
import groovy.transform.CompileStatic

@CompileStatic
class StrategyContext implements Context {
/**
 * This strategy must be selected when using the
 * <a href="https://wiki.jenkins-ci.org/display/JENKINS/Gerrit+Trigger">Gerrit Trigger Plugin</a>.
 */
    void gerritTrigger() {

    }

    /**
     * Build all branches except for those which match the branch specifiers.
     */
    void inverse() {

    }

    /**
     * Selects commits to be build by maximum age and ancestor commit.
     */
    void ancestry(int maxAge, String commit) {
//        {
//            maximumAgeInDays(maxAge)
//            ancestorCommitSha1(commit)
//        }
    }

    /**
     * Selects branches in priority order based on which branch exists.
     *
     * @since 1.45
     */
    void alternative() {

    }
}