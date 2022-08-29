package com.pipeline.runtime.validations

class NumberValidation extends Validation<NumberValidation, Number> {

    private NumberValidation(Number sut) {
        super(sut)
    }

    private NumberValidation(Number sut, String tag) {
        super(sut, tag)
    }

    NumberValidation(Validation validation) {
        super(validation.sut)
        this.onErrorMessages = validation.onErrorMessages
        this.validations = validation.validations
        this.tag = validation.tag
    }

    NumberValidation lowerThan(Number max) {
        return test("Must be lower than $max.") { Number n -> n < max }
    }

    NumberValidation greaterThan(int min) {
        return test("Must be greater than $min.") { Number n -> n > min }
    }

    NumberValidation numberBetween(Number min, Number max) {
        greaterThan(min)
        return lowerThan(max)
    }

    NumberValidation notNull() {
        return super.notNull()
    }

    static NumberValidation from(Number sut) {
        return new NumberValidation(sut)
    }

    static NumberValidation from(Number sut, String tag) {
        return new NumberValidation(sut, tag)
    }


}
