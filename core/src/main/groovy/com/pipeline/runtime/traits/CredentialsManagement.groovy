package com.pipeline.runtime.traits

import com.pipeline.runtime.vo.SecretText
import com.pipeline.runtime.vo.TypeCredentials
import com.pipeline.runtime.vo.UsernamePassword
import groovy.transform.NamedParam
import groovy.transform.NamedParams

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

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


