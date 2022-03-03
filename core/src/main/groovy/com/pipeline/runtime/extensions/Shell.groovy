package com.pipeline.runtime.extensions

import com.pipeline.runtime.dsl.StepsExecutor
import groovy.transform.CompileStatic
import groovy.transform.NamedParam
import groovy.transform.NamedParams

import java.nio.file.FileSystems

//@CompileStatic
class Shell {

    long timeout = 15000
    boolean redirectErrorStream = false


    private static Process run(StepsExecutor self, String command) {

        if(self.getEnv().JAVA_HOME) {
            self.getEnv().PATH= "${self.getEnv().JAVA_HOME}/bin:${self.getEnv().PATH}"
        }

        if(self.getEnv().M2_HOME) {
            self.getEnv().PATH= "${self.getEnv().M2_HOME}/bin:${self.getEnv().PATH}"
        }
        def directory = self.toFullPath(self.getWorkingDir()).toFile()
        new ProcessBuilder(['sh', '-c', command])
                .directory(directory)
                .environment(self.getEnv().collect { "${it.key}=${it.value}" } as String[])
                .start()

    }


    private static Map runAndGet(StepsExecutor self, String command) {
        def sout = new StringBuilder(), serr = new StringBuilder()
        def proc = run(self, command)
        proc.consumeProcessOutput(sout, serr)
        proc.waitFor()
        return [sout: sout, serr: serr, exitValue: proc.exitValue()]
    }

    static void sh(StepsExecutor self,final String script) {
        sh(self,[script: script, returnStdout: false])
    }

    static Object sh(StepsExecutor self, @NamedParams([
            @NamedParam(value = "script", type = String, required = true),
            @NamedParam(value = "returnStdout", type = Boolean)
    ]) final Map param) {
        def result = runAndGet(self, param.script)

        println "+ ${param.script}"

        if (result.exitValue == 0) {
            if (param.returnStdout) {
                return result.sout
            }
            println result.sout
        } else {
            println result.serr
        }
    }

    Shell timeout(int timeout) {
        this.timeout = timeout
        return this
    }
}
