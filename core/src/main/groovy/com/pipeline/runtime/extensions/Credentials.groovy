package com.pipeline.runtime.extensions


import groovy.transform.NamedParam
import groovy.transform.NamedParams

trait Credentials {
    private final List credentials = []

    def usernamePassword(@NamedParams([
            @NamedParam(value = 'credentialsId', type = String.class),
            @NamedParam(value = 'usernameVariable', type = String.class),
            @NamedParam(value = 'passwordVariable', type = String.class)
    ]) Map<String, String> params) {
        def secret = credentials.find { it.id == params.credentialsId  }
        this.env[params.usernameVariable] = secret.user
        this.env[params.passwordVariable] = secret.pass
    }

    def string(@NamedParams([
            @NamedParam(value = 'credentialsId', type = String.class),
            @NamedParam(value = 'variable', type = String.class)
    ]) Map<String, String> params) {
        def secret = credentials.find { it.id == params.credentialsId }
        this.env[params.variable] = secret.text
    }

    def getCredentials() {
        println "Store Credentials $credentials"
        return this.credentials
    }

}


