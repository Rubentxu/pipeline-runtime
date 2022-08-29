package com.pipeline.runtime.validations

import com.cloudbees.groovy.cps.NonCPS

class StringValidation extends Validation<StringValidation, String> {
    def EMAIL_REGEX = /^(([^<>()[\]\.,;:\s@\"]]+(\.[^<>()[\]\.,;:\s@\"]]+)*)|(\".+\"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/
    def HTTP_PROTOCOL_REGEX = /^(?:http[s]?:\/\/.)(?:www\.)?[-a-zA-Z0-9@%._\+~#=]{2,256}\.[a-z]{2,6}\b(?:[-a-zA-Z0-9@:%_\+.~#?&\/\/=]*)/

    private StringValidation(String sut) {
        super(sut)
    }

    private StringValidation(String sut, String tag) {
        super(sut, tag)
    }

    StringValidation(Validation validation) {
        super(validation.sut)
        this.onErrorMessages = validation.onErrorMessages
        this.validations = validation.validations
        this.tag = validation.tag
    }

    @NonCPS
    StringValidation moreThan(int size) {
        return test("Must have more than $size chars.") { String s -> s.length() >= size }
    }

    @NonCPS
    StringValidation lessThan(int size) {
        return test("Must have less than $size chars.") { String s -> s.length() <= size }
    }

    @NonCPS
    StringValidation between(int minSize, int maxSize) {
        moreThan(minSize)
        return lessThan(maxSize)
    }

    @NonCPS
    StringValidation contains(String c) {
        return test("Must contain $c") { String s -> s.contains(c) }
    }

    @NonCPS
    StringValidation isEmail() {
        return test("Must be email type") { String s -> s ==~ EMAIL_REGEX }
    }

    @NonCPS
    StringValidation matchRegex(regex) {
        return test("Must be match Regular Expression '/$regex/'") { String s -> s ==~ regex }
    }

    @NonCPS
    StringValidation isHttpProtocol() {
        return test("Must be http protocol type") { String s -> s ==~ HTTP_PROTOCOL_REGEX }
    }

    @NonCPS
    StringValidation notNull() {
        return super.notNull()
    }

    @NonCPS
    StringValidation notEmpty() {
        return test("Must not be empty") { s -> s != null && s != '' }
    }

    @NonCPS
    static StringValidation from(String sut) {
        return new StringValidation(sut)
    }

    @NonCPS
    static StringValidation from(String sut, String tag) {
        return new StringValidation(sut, tag)
    }
}
