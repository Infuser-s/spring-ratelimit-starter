@Library('infusers-shared-lib') _

properties([
    disableConcurrentBuilds(abortPrevious: true),
    buildDiscarder(logRotator(numToKeepStr: '10'))
])
quietPeriod(0)

// Scripted on purpose - see infusers-auth/Jenkinsfile for why (executor pinning bug).
withExclusiveBuild(resource: "spring-ratelimit-starter-activity") {
    env.PROJECT_TYPE = 'springboot'
    env.MAVEN_CACHE = "${HOME}/maven-caches/spring-ratelimit-starter/dev"

    try {
        timeout(time: 15, unit: 'MINUTES') {
            stage("Notify") {
                notify(notify.STATUS_STARTED)
            }

            // Declarative's implicit SCM auto-checkout is gone in scripted pipelines -
            // this job's own pom.xml has to be checked out explicitly before mvnInstall.
            stage('Checkout') {
                checkout scm
            }

            stage('Build & Test') {
                // -Pdev keeps GPG signing skipped — this pipeline only verifies
                // compile + tests on every push, it never publishes anywhere.
                sh "mkdir -p ${env.MAVEN_CACHE}"
                withMaven(mavenLocalRepo: "${env.MAVEN_CACHE}") {
                    com.infusers.util.BuildUtils.mvnInstall(this, env.PROJECT_TYPE, 'dev')
                }
            }
        }
    } finally {
        postBuildUtils.deleteJenkinsJobAndNotify()
    }
}
