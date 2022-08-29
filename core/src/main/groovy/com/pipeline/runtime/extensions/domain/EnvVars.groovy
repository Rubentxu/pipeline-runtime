package com.pipeline.runtime.extensions.domain

import java.util.concurrent.ConcurrentHashMap

class EnvVars extends ConcurrentHashMap<String, String> {
    def engine = new groovy.text.SimpleTemplateEngine()

    def getProperty(String name) {
        this.get(name)
    }

    void setProperty(String name, Object value) {
        this.put(name,value)
    }

    EnvVars getEnvironment() {
        return this
    }

    String expand(String s) {
        try {
            return engine.createTemplate(s).make(this).toString()
        } catch(Exception ex) {
            def variable = ex.message =~ /.+: ([A-Z_]+) for class:.*/
            throw new Exception("Not Found Environment Var ${variable[0][1]}")
        }
    }
}
