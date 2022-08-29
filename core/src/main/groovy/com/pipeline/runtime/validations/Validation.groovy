package com.pipeline.runtime.validations

import com.cloudbees.groovy.cps.NonCPS
import com.pipeline.runtime.RunnerLogger
import org.codehaus.groovy.runtime.NullObject

import java.util.function.Predicate

class Validation<K extends Validation, T> {
    protected List<Boolean> validations
    protected List<String> onErrorMessages
    protected T sut
    protected String tag

    protected Validation(T sut) {
        this.validations = []
        this.onErrorMessages = []
        this.sut = sut
        this.tag = ''
        notNull()
    }

    protected Validation(T sut, String tag) {
        this.validations = []
        this.onErrorMessages = []
        this.sut = sut
        this.tag = tag
        notNull()
    }

    @NonCPS
    protected K test(String onErrorMessage, Predicate<T> predicate) {
        if (sut == null || sut instanceof NullObject) {
            this.onErrorMessages.add(onErrorMessage)
            this.validations.add(false)
        } else if (this.testPredicate(predicate)) {
            this.validations.add(true)
        } else {
            this.onErrorMessages.add(onErrorMessage)
            this.validations.add(false)
        }
        return this as K
    }

    @NonCPS
    protected Boolean testPredicate(Predicate<T> predicate) {
        try {
            return predicate.test(sut)
        } catch (Exception ex) {
            return false
        }
    }

    @NonCPS
    protected Boolean isvalid() {
        return !validations.any { it == false }
    }

    @NonCPS
    T throwIfInvalid(String customErrorMessage = '') {
        onErrorMessages = onErrorMessages.plus(0, customErrorMessage, this.tag)

        if (!isvalid()) {
            def message = RunnerLogger.createBanner(onErrorMessages.unique())
            throw new IllegalArgumentException(message)
        }
        return sut
    }

    @NonCPS
    T defaultValueIfInvalid(T defaultValue) {
        if (isvalid()) {
            return sut
        } else {
            return defaultValue
        }
    }

    @NonCPS
    def getValue() {
        return sut
    }

    @NonCPS
    K notNull() {
        K result = test("Must not be null.") { T s -> !(s instanceof NullObject) }
        return result
    }

    @NonCPS
    StringValidation isString() {
        K result = test("Must be type String. Current is ${sut.getClass().name}") { T s -> s instanceof String }
        return new StringValidation(result)
    }

    @NonCPS
    NumberValidation isNumber() {
        K result = test("Must be type Number. Current is ${sut.getClass().name}") { T s -> s instanceof Number }
        return new NumberValidation(result)
    }

    @NonCPS
    MapValidation isMap() {
        K result = test("Must be type Map. Current is ${sut.getClass().name}") { T s -> s instanceof Map }
        return new MapValidation(result)
    }

    @NonCPS
    <R> Validation<Validation, R> is(Class<R> clazz) {
        notNull()
        test("Must be type $clazz. Current is ${sut?.getClass()?.name}") { T s ->
            s.class == clazz || clazz.isAssignableFrom(s.class)
        }
        // TODO: hay que probar esto. Lo cambio a this porque sino no propaga los errores
        //return new Validation<Validation, R>(sut, tag)
        return this
    }

    @NonCPS
    <R> Validation<Validation, R> is(String onErrorMessage, Predicate<T> predicate) {
        test(onErrorMessage, predicate)
        // TODO: hay que probar esto. Lo cambio a this porque sino no propaga los errores
        //return new Validation<Validation, R>(sut, tag)
        return this
    }

    @NonCPS
    static <T> Validation<Validation, T> from(T sut) {
        return new Validation<Validation, T>(sut)
    }

    @NonCPS
    static <T> Validation<Validation, T> from(T sut, String tag) {
        return new Validation<Validation, T>(sut, tag)
    }

}
