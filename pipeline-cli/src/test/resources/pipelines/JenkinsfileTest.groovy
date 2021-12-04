package pipelines
library('commons')

import static com.pipeline.runtime.dsl.Dsl.pipeline
import static com.pipeline.runtime.dsl.Dsl.initialize
import demo.Greeter



new Greeter().sayHello()
hello()

initialize(this)
//println new Greeter().sayHello()

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
                sh(script: 'date +%Y-%m-%d', returnStdout: false)
                echo "Groovy rocks!"
                echo "env.SOME_STRING=${env.SOME_STRING}"
                echo "Mensaje ${env.MENSAJE}"

                sh 'echo $PATH'
                sh '''echo "Who I'm $SHELL"'''

            }
        }
        stage("Test") {
            steps {
                sh "gradle -v"
                sh "java -version"
                sh "mvn -version"
                withCredentials([ string(credentialsId: 'gitlab_token', variable: 'TOKEN')
                        ]) {
                    echo "Pintamos el token: <${env.TOKEN}>"
                }
                withCredentials([ usernamePassword(credentialsId: 'gitlab', usernameVariable: 'USER', passwordVariable: 'PASS')]) {
                    echo "Pintamos el USER: <${env.USER}>"
                    echo "Pintamos el PASS: <${env.PASS}>"
                }
            }
        }
    }
}
