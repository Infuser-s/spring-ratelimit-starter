@Library('infusers-shared-lib') _

pipeline {
    agent any

    environment {
        PROJECT_TYPE = 'springboot'
        MAVEN_CACHE = "${HOME}/maven-caches/spring-ratelimit-starter/dev"
    }

    options {
        quietPeriod(0)
        disableConcurrentBuilds(abortPrevious: true)
        timeout(time: 15, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '10'))
        lock(resource: "spring-ratelimit-starter-activity", quantity: 1)
    }

    stages {
        stage("Notify") {
            steps {
                script {
                    notify(notify.STATUS_STARTED)
                }
            }
        }

        stage('Build & Test') {
            steps {
                script {
                    // -Pdev keeps GPG signing skipped — this pipeline only verifies
                    // compile + tests on every push, it never publishes anywhere.
                    sh "mkdir -p ${env.MAVEN_CACHE}"
                    withMaven(mavenLocalRepo: "${env.MAVEN_CACHE}") {
                        com.infusers.util.BuildUtils.mvnInstall(this, env.PROJECT_TYPE, 'dev')
                    }
                }
            }
        }
    }
    post {
        always {
            script {
                postBuildUtils.deleteJenkinsJobAndNotify()
            }
        }
    }
}
