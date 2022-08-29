package com.pipeline.runtime.validations

import com.cloudbees.groovy.cps.NonCPS

class MapValidation extends Validation<MapValidation, Map> {
    def resolvedValue
    def engine = new groovy.text.SimpleTemplateEngine()

    private MapValidation(Map sut) {
        super(sut)
    }

    private MapValidation(Map sut, String tag) {
        super(sut, tag)
    }

    MapValidation(Validation validation) {
        super(validation.sut)
        resolvedValue = validation.sut
        this.onErrorMessages = validation.onErrorMessages
        this.validations = validation.validations
        this.tag = validation.tag
    }

    @NonCPS
    MapValidation notNull() {
        return super.notNull()
    }

    @NonCPS
    static MapValidation from(Map sut, String tag) {
        return new MapValidation(sut, tag)
    }

    @NonCPS
    MapValidation getKey() {
        resolvedValue = getResolvedValue(tag)
        return test("There is no configuration defined for key '$tag'") { resolvedValue != null }
    }

    @NonCPS
    MapValidation getKey(String key) {
        resolvedValue = getResolvedValue(key)
        return test("There is no configuration defined for key '$key'") { resolvedValue != null }
    }

    @NonCPS
    def getResolvedValue(key) {
        def result = findDeep(sut, key)

        if (result instanceof String || result instanceof GString) {
            result = result.trim()
        }
        return result
    }

    @NonCPS
    @Override
    def getValue() {
        return resolvedValue
    }

    @NonCPS
    private def findDeep(Map m, String path) {
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
