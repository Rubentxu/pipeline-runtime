package com.pipeline.runtime.extensions

import com.pipeline.runtime.dsl.StepsExecutor
import groovy.transform.NamedParam
import groovy.transform.NamedParams

//@CompileStatic
class Shell {

    long timeout = 15000


    private static Process run(StepsExecutor self, String command) {

        if (self.getEnv().JAVA_HOME) {
            self.getEnv().PATH = "${self.getEnv().JAVA_HOME}/bin:${self.getEnv().PATH}"
        }

        if (self.getEnv().M2_HOME) {
            self.getEnv().PATH = "${self.getEnv().M2_HOME}/bin:${self.getEnv().PATH}"
        }
        def directory = self.toFullPath(self.getWorkingDir()).toFile()
        self.log.system "+sh in workindir ${directory}"
        new ProcessBuilder(['sh', '-c', command])
                .directory(directory)
                .environment(self.getEnv().collect { "${it.key}=${it.value}" } as String[])
                .start()

    }


    private static Map runAndGet(StepsExecutor self, String command) {
        def process = run(self, command)

        def output = new StringBuffer()

        def outline = { line -> output += line + '\n' }

        def inThread = Thread.start {
            process.in.eachLine {
                  outline it
                  println(it)
            }
        }

        def errThread = Thread.start {
            process.err.eachLine {
                outline it
                println(it)
            }
        }

        inThread.join()
        errThread.join()
        process.waitFor()
        if(!output) output = ''
        return [sout: output.toString(), exitValue: process.exitValue()]
    }

    static def sh(StepsExecutor self, final String script) {
        sh(self, [script: script, returnStdout: false])
    }

    static def sh(StepsExecutor self, @NamedParams([
            @NamedParam(value = "script", type = String, required = true),
            @NamedParam(value = "returnStdout", type = Boolean)
    ]) final Map param) {
        def result = runAndGet(self, param.script)

        self.log.info "+sh ${param.script}"
        if (result.exitValue == 0) {
            if (param.returnStdout) {
                self.log.debug "Return Stdout with ${result.sout}<--End"
                return result.sout.toString()
            }
        } else {
            self.log.error result.serr
            return result.serr.toString()
        }
    }

    Shell timeout(int timeout) {
        this.timeout = timeout
        return this
    }
}
