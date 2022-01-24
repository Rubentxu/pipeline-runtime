package com.pipeline.runtime.extensions

import com.pipeline.runtime.dsl.StepsExecutor
import groovy.transform.NamedParam
import groovy.transform.NamedParams

class Credentials {
    static final List credentials = []

    static def usernamePassword(StepsExecutor self, @NamedParams([
            @NamedParam(value = 'credentialsId', type = String.class),
            @NamedParam(value = 'usernameVariable', type = String.class),
            @NamedParam(value = 'passwordVariable', type = String.class)
    ]) Map<String, String> params) {
        def secret = findCredentials(self, params.credentialsId)
        self.env[params.usernameVariable] = secret.user
        self.env[params.passwordVariable] = secret.pass
    }

    static def string(StepsExecutor self, @NamedParams([
            @NamedParam(value = 'credentialsId', type = String.class),
            @NamedParam(value = 'variable', type = String.class)
    ]) Map<String, String> params) {
        def secret = credentials.find { it.id == params.credentialsId }
        self.env[params.variable] = secret.secret
    }

    static def getCredentials(StepsExecutor self) {
        println "Store Credentials $credentials"
        return credentials
    }

    static def findCredentials(StepsExecutor self, String credentialsId) {
        return credentials.find { it.id == credentialsId  }
    }

    static def getTypeCredentials(StepsExecutor self, String credentialsId) {
        return findCredentials(self,credentialsId)?.type
    }

}


