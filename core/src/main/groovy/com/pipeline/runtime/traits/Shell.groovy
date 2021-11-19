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
    File currentDir = new File('.')
    final Map environment

    Process run(String command) {
        return command.execute (
            environment.collect { "${it.key}=${it.value}"},
            currentDir)
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
        def result = runAndGet(param.script.toString())

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
//
//    Object sh(@NamedParams([
//            @NamedParam(value = "script", type = String, required = true),
//            @NamedParam(value = "returnStdout", type = Boolean)
//    ]) final Map param) {
//
//        final Process p = param.script.toString().execute()
//        p.waitFor()
//
//        println "+ ${param.script}"
//
//        if (p.exitValue() == 0) {
//            if (param.returnStdout) {
//                return p.text
//            }
//
//            println p.text
//        } else {
//            println p.err.text
//        }
//
//    }

}
