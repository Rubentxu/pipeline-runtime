package com.pipeline.runtime.extensions.domain.scm

import groovy.transform.builder.Builder

@Builder
class CloneOption implements ScmExtensions{
    Integer depth
    Integer timeout
    Boolean noTags
    Boolean shallow

    @Override
    Map toMap() {
        return [$class: 'CloneOption', depth: depth, noTags: noTags, shallow: shallow, timeout: timeout]
    }
}
