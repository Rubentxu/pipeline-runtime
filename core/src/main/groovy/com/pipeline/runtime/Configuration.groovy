package com.pipeline.runtime

import com.pipeline.runtime.interfaces.IConfiguration
import org.yaml.snakeyaml.Yaml
import java.util.concurrent.ConcurrentHashMap

//import groovy.transform.CompileStatic

//@CompileStatic
class Configuration implements IConfiguration {
    private Map config = new ConcurrentHashMap()


    @Override
    def loadConfig(File configFile ) {
        Yaml parser = new Yaml()
        config = parser.load((configFile).text)
    }

    @Override
    def getValue(key) {
        def result = findDeep(config, key)
        if (result == null) throw new Exception("""
            No existe la configuración definida para '$key'.
            Definala en su configuración
        """)

        if (result instanceof String || result instanceof GString) {
            result = result.trim();
        }
        return result
    }

    @Override
    def containsKey(key) {
        def result = findDeep(config, key)
        result != null
    }

    @Override
    def getValueOrDefault(key, defaultValue) {
        try{
            this.getValue(key)
        } catch(Exception ex) {
            return defaultValue
        }
    }

    @Override
    def printConfiguration() {
        println "Configuration"
        config.each { println it }
    }

    def findDeep(Map m, String path) {
        if (m == null || !path) return null
        def slice = path.split('\\.')
        def key = slice[0]
        if (m.containsKey(key) && slice.size() == 1) {
            return m[key]
        } else if (m.containsKey(key)) {
            if (m[key] instanceof Map) {
                return findDeep(m[key], slice[1..-1].join('.'))
            }
        }
    }

}
