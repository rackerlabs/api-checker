node ('build') {
    // Number of builds to keep
    properties ([
            buildDiscarder(logRotator(numToKeepStr: '10')),
            disableConcurrentBuilds()
    ])

    // Configure Java
    env.JAVA_HOME="${tool 'jdk-8u77'}"
    env.PATH="${env.JAVA_HOME}/bin:${env.PATH}"
    env.SAXON_HOME="/var/lib/jenkins/saxon_ee/"

    // Configure GIT
    env.EMAIL="reposecore@rackspace.com"

    def isPR = env.BRANCH_NAME.startsWith("PR-")    // PRs start with PR-
    def isRelease = env.BRANCH_NAME.equals("master") // To Identify if its release

    stage ("Pre Build") {
        // Clean the workspace before using it
        deleteDir()

        // Print Environment and Java Version details
        sh 'printenv'
        sh 'java -version'

        if (isRelease) {
            git url: 'git@github.com:rackerlabs/api-checker.git',
                credentialsId: 'repose-bot-PAT',
                branch: env.BRANCH_NAME
        } else {
            // check out code, repo and branch are set by the actual job
            checkout scm
        }
    }

    def skipBuild = sh (script: "git log -1 | grep '\\[maven-release-plugin\\]'", returnStatus: true)
    if (skipBuild == 0) {
        echo "Maven release detected"
        currentBuild.result = 'SUCCESS'
        return
    }

    stage ("Build and Test") {
        // setting an environment block here limits these variables to anything in this stage
        environment {
            JENKINS_MAVEN_AGENT_DISABLED='true'
        }

        try {
            // See https://wiki.jenkins-ci.org/display/JENKINS/Pipeline+Maven+Plugin
            // Run the maven build
            if (isRelease) {
                echo "Release build"
                withMaven(maven: 'Maven3.3.9',
                    mavenSettingsConfig: 'api-checker-maven-artifactory-settings.xml',
                    mavenLocalRepo: '.repository',
                    mavenOpts: '-Xms1024m -Xmx2048m') {
                    def pom = readMavenPom file: 'pom.xml'
                    def version  = pom.version
                    withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: 'repose-bot-PAT',
                                      usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD']]) {
                        sh """
                            mvn clean -B -V -e -U \
                                -Dresume=false \
                                -DdevelopmentVersion=${getNextDevVersion(version)} \
                                -DreleaseVersion=${getReleaseVersion(version)} \
                                -Dtag=${pom.artifactId}-${getReleaseVersion(version)} \
                                -Dusername=${USERNAME} \
                                -Dpassword=${PASSWORD} \
                                -Darguments="-Dmaven.javadoc.skip=true" \
                                release:prepare release:perform
                        """
                    }
                }
            } else {
                echo "Not a release"
                withMaven(maven: 'Maven3.3.9',
                    mavenLocalRepo: '.repository',
                    mavenOpts: '-Xms1024m -Xmx2048m') {
                    sh "mvn clean"
                    sh "mvn -B install"
                    sh "mvn -B -P xerces-only install"
                }
            }
        } catch(e) {
            currentBuild.result = "FAILED"

            // the withMaven plugin archives the surefire reports but not the failsafe or scalatest reports
            // on a failure, doing it manually here. TODO: fix failure reporting if needed
            step([$class: 'JUnitResultArchiver', testResults: '**/target/failsafe-reports/TEST-*.xml'])
            step([$class: 'JUnitResultArchiver', testResults: '**/target/scalatest-reports/TEST-*.xml'])

            throw e
        }
    }
}

String getReleaseVersion(String version) {
    return version.replace("-SNAPSHOT", "")
}

String getNextDevVersion(String version) {
    String currentRelease = getReleaseVersion(version)

    String[] split = currentRelease.split("\\.")
    int nextMajor = split[1].toInteger() + 1

    return "${split[0]}.${nextMajor}.0-SNAPSHOT"
}
