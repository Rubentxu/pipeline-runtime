package com.pipeline.runtime.extensions

trait Workspace {
    String workingDir = "build/workspace"


    def deleteDir() {
        println "Se ejecuto deleteDir"
    }
}
