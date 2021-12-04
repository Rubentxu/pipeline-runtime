package com.pipeline.runtime.dsl.traits

import java.util.concurrent.ConcurrentHashMap

trait DynamicObject {
    private Map props = new ConcurrentHashMap()
    def methodMissing(String methodName, args) {
        throw new Exception("\u001B[1;31m************ 'Method Missing with name $methodName and args $args **************\u001B[0m'")
    }
    def propertyMissing(String prop) {
        props[prop]
    }
    void setProperty(String prop, Object value) {
        props[prop] = value
    }
}