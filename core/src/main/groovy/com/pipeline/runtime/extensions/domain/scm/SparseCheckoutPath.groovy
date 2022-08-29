package com.pipeline.runtime.extensions.domain.scm

class SparseCheckoutPath implements ScmExtensions{
    String sparseCheckoutPath

    SparseCheckoutPath(String sparseCheckoutPath) {
        this.sparseCheckoutPath = sparseCheckoutPath
    }

    @Override
    Map toMap() {
        return [$class: 'SparseCheckoutPaths', sparseCheckoutPaths: [[path: sparseCheckoutPath]]]
    }
}
