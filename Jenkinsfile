def projectName = 'lite-exporter-dashboard'
pipeline {
  agent {
    node {
      label 'docker.ci.uktrade.io'
    }
  }

  stages {
    stage('prep') {
      steps {
        script {
          deleteDir()
          checkout scm
          deployer = docker.image("ukti/lite-image-builder")
          deployer.pull()
          env.BUILD_VERSION = ''
        }
      }
    }

    stage('test') {
      steps {
        script {
          deployer.inside {
            try {
              sh 'sbt -no-colors test'
            }
            finally {
              step([$class: 'JUnitResultArchiver', testResults: 'target/test-reports/**/*.xml'])
            }
          }
        }
      }
    }

    stage('build') {
      steps {
        script {
          echo 'build'
          def buildPaasAppResult = build job: 'build-paas-app', parameters: [
              string(name: 'BRANCH', value: env.BRANCH_NAME),
              string(name: 'PROJECT_NAME', value: projectName),
              string(name: 'BUILD_TYPE', value: 'zip')
          ]
          env.BUILD_VERSION = buildPaasAppResult.getBuildVariables().BUILD_VERSION
        }
      }
    }

    stage('deploy') {
      steps {
        build job: 'deploy', parameters: [
            string(name: 'PROJECT_NAME', value: projectName),
            string(name: 'BUILD_VERSION', value: env.BUILD_VERSION),
            string(name: 'ENVIRONMENT', value: 'dev')
        ]
      }
    }
  }

  post {
    always {
      script {
        currentBuild.displayName = "#${currentBuild.number} - ${env.BUILD_VERSION}"
        deleteDir()
      }
    }
  }
}
