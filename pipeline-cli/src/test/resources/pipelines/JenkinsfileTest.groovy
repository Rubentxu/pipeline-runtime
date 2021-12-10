package pipelines
library('commons')


import demo.Greeter


println "Groovy versión :: ${GroovySystem.getVersion()}"
println new Greeter().sayHello()
hello()



//node {
//    checkout scm
//}

println '''
   ___                                ___ _            _ _
  / _ \\_ __ ___   _____   ___   _    / _ (_)_ __   ___| (_)_ __   ___
 / /_\\/ '__/ _ \\ / _ \\ \\ / / | | |  / /_)/ | '_ \\ / _ \\ | | '_ \\ / _ \\
/ /_\\\\| | | (_) | (_) \\ V /| |_| | / ___/| | |_) |  __/ | | | | |  __/
\\____/|_|  \\___/ \\___/ \\_/  \\__, | \\/    |_| .__/ \\___|_|_|_| |_|\\___|
                            |___/          |_|
'''


pipeline {
    agent any

    environment {
        SOME_NUMBER = 123
        SOME_STRING = "foobar"
    }

    stages {
        stage("Build") {
            steps {
                sh "ls -la"
                sh 'pwd'
                sh(script: 'date +%Y-%m-%d', returnStdout: false)
                echo "Groovy rocks!"
                echo "env.SOME_STRING=${env.SOME_STRING}"
                echo "SOME_STRING=${SOME_STRING}"
                echo "Mensaje ${MENSAJE}"
                hello()
                sh 'echo $PATH'
                sh '''echo "Who I'm $SHELL"'''
                checkout scm
                sh "ls -la"
                sh 'pwd'

            }
        }
        stage("Test") {
            steps {
                sh "gradle assemble  --stacktrace"
                sh "mvn -version"
                sh "java -version"
                withCredentials([ string(credentialsId: 'gitlab_token', variable: 'TOKEN')
                        ]) {
                    echo "Pintamos el token: <${env.TOKEN}>"
                }
                withCredentials([ usernamePassword(credentialsId: 'gitlab', usernameVariable: 'USER', passwordVariable: 'PASS')]) {
                    echo "Pintamos el USER: <${USER}>"
                    echo "Pintamos el PASS: <${PASS}>"
                }
            }
        }
    }
}
