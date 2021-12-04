package com.pipeline.runtime.dsl.traits


import groovy.transform.NamedParam
import groovy.transform.NamedParams

trait CredentialsManagement extends Environment {
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

    def getCredentialsStore() {
        println "Store Credentials $credentials"
        return this.credentials
    }

}

