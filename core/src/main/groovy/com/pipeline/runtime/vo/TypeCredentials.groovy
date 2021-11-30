package com.pipeline.runtime.vo

trait TypeCredentials {
    String name
    String id
    abstract String type()
}

class UsernamePassword implements TypeCredentials {
    String user
    String pass

    @Override
    String type() {
        return 'username_password'
    }
}

class Certificate implements TypeCredentials {
    // PKCS#12 base64 encoded bytes
    String certificate
    String password

    @Override
    String type() {
        return 'certificate'
    }
}

class DockerHostCert implements TypeCredentials {
    String clientKey
    String clientCert
    String serverCert

    @Override
    String type() {
        return 'docker_host_cert'
    }
}

class KubernetesServiceAccount implements TypeCredentials {
    // "Actual token"
    String id

    @Override
    String type() {
        return 'kubernetes_service_account'
    }
}

class OpenshiftOAuthToken implements TypeCredentials {
    // "Actual token"
    String token

    @Override
    String type() {
        return 'openshift_oauth_token'
    }
}

class OpenshiftUser implements TypeCredentials {
    String user
    String pass

    @Override
    String type() {
        return 'openshift_user'
    }
}

class SecretFile implements TypeCredentials {
    String filename
    //# base64 encoded bytes
    String data

    @Override
    String type() {
        return 'secret_file'
    }
}

class SecretText implements TypeCredentials {
    String text

    @Override
    String type() {
        return 'secret_text'
    }
}

class SshUserPrivateKey implements TypeCredentials {
    String username
    String privateKey

    @Override
    String type() {
        return 'ssh_private_key'
    }
}

class SshUserPrivateKeyPassphrase implements TypeCredentials {
    String username
    String privateKey
    String passphrase

    @Override
    String type() {
        return 'ssh_private_key_passphrase'
    }
}