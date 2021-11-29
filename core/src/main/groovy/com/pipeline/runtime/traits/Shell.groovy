package com.pipeline.runtime.traits

import groovy.transform.NamedParam
import groovy.transform.NamedParams

/*
  Ejemplo
  Shell shell = new Shell {currentDir: new File{"path/temp"}}
  shell.environment["GREETINGS"] = "Hola"
  print shell.runAndGet('echo $GREETINS')
 */

trait Shell {
    private File currentDir = new File('.')
    private Map environment = [*        : System.env,
                             M2_HOME  : '/home/rubentxu/.sdkman/candidates/maven/3.8.1',
                             JAVA_HOME: '/home/rubentxu/.sdkman/candidates/java/21.2.0.r8-grl',
//                             PATH: '$PATH:$JAVA_HOME/bin:$M2_HOME/bin',
    ]
    long timeout = 15000
    boolean redirectErrorStream = false


    Process run(String command) {

        if(environment.JAVA_HOME) {
            environment.PATH= "${environment.JAVA_HOME}/bin:${environment.PATH}"
        }

        if(environment.M2_HOME) {
            environment.PATH= "${environment.M2_HOME}/bin:${environment.PATH}"
        }


        new ProcessBuilder(['sh', '-c', command])
                .directory(currentDir)
                .environment(environment.collect { "${it.key}=${it.value}" } as String[])
                .start()

    }

    Map runAndGet(String command) {
        def sout = new StringBuilder(), serr = new StringBuilder()
        def proc = run(command)
        proc.consumeProcessOutput(sout, serr)
        proc.waitFor()
        return [sout: sout, serr: serr, exitValue: proc.exitValue()]
    }

    void sh(final String script) {
        sh(script: script, returnStdout: false)
    }

    Object sh(@NamedParams([
            @NamedParam(value = "script", type = String, required = true),
            @NamedParam(value = "returnStdout", type = Boolean)
    ]) final Map param) {
        def result = runAndGet(param.script)

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

    Shell env(Map env) {
        this.environment.putAll(env)
        return this
    }

    Shell dir(File dir) {
        this.currentDir = dir
        return this
    }

    Shell dir(String dir) {
        this.currentDir = new File(dir)
        return this
    }

    Shell timeout(int timeout) {
        this.timeout = timeout
        return this
    }
}
