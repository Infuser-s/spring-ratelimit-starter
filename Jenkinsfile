@Library('infusers-shared-lib') _

properties([
    disableConcurrentBuilds(abortPrevious: true),
    buildDiscarder(logRotator(numToKeepStr: '10'))
])
quietPeriod(0)

// Scripted on purpose - see infusers-auth/Jenkinsfile for why (executor pinning bug).
// Kills any lower-priority test/analysis job currently holding global-executor
// instead of waiting for it to finish naturally - see abortConflictingJobs.groovy.
abortConflictingJobs()
withExclusiveBuild(resource: "spring-ratelimit-starter-activity", priority: 10) {
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
                // coveragePublisher disabled: mvnInstall('dev') runs jacoco:report as part of its
                // own mvn ... install, so withMaven's implicit auto-publish would record a second,
                // redundant CoverageBuildAction alongside the explicit recordCoverage call below
                // (duplicate Coverage tab/trend chart - see jenkins-shared-lib
                // docs/jenkins-job-incidents.md 2026-11-05/06).
                withMaven(mavenLocalRepo: "${env.MAVEN_CACHE}", options: [coveragePublisher(disabled: true)]) {
                    com.infusers.util.BuildUtils.mvnInstall(this, env.PROJECT_TYPE, 'dev')
                }

                // mvnInstall('dev') already runs the test phase - publish what it produced.
                // Previously this ran but was never surfaced anywhere in Jenkins.
                // See docs/jenkins-job-incidents.md 2026-11-05.
                junit testResults: '**/target/surefire-reports/*.xml', allowEmptyResults: true
                if (fileExists('target/site/jacoco/jacoco.xml')) {
                    recordCoverage tools: [[parser: 'JACOCO', pattern: 'target/site/jacoco/jacoco.xml']]
                }
            }
        }
    } finally {
        postBuildUtils.deleteJenkinsJobAndNotify()
    }
}
