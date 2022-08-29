package com.pipeline.runtime.validations

import com.cloudbees.groovy.cps.NonCPS

//@Category
class ValidationCategory {

    @NonCPS
    static StringValidation validate(String self) {
        return StringValidation.from(self)

    }

    @NonCPS
    static StringValidation validate(String self, String tag) {
        return StringValidation.from(self, tag)

    }

    @NonCPS
    static NumberValidation validate(Number self) {
        return NumberValidation.from(self)
    }

    @NonCPS
    static NumberValidation validate(Number self, String tag) {
        return NumberValidation.from(self, tag)
    }

    @NonCPS
    static MapValidation validate(Map self, String tag) {
        return MapValidation.from(self, tag)
    }

    @NonCPS
    static Validation validateAndGet(Map self, String tag) {
        def value = MapValidation.from(self, tag).getKey().getValue()
        return Validation.from(value, tag)
    }

    @NonCPS
    static <T> Validation<Validation, T> validate(T self) {
        return Validation.from(self)
    }

    @NonCPS
    static <T> Validation<Validation, T> validate(T self, String tag) {
        return Validation.from(self, tag)
    }
}
