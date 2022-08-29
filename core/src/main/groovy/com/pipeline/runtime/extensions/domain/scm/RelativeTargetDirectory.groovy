package com.pipeline.runtime.extensions.domain.scm

class RelativeTargetDirectory implements ScmExtensions{

    String relativeTargetDirectory

    RelativeTargetDirectory(String relativeTargetDirectory) {
        this.relativeTargetDirectory = relativeTargetDirectory
    }

    @Override
    Map toMap() {
        return [$class: 'RelativeTargetDirectory', relativeTargetDir: relativeTargetDirectory]
    }
}
