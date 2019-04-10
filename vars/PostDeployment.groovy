#!/usr/bin/env groovy

def call(Map config) {
  script {
      if ( config.PostDeployScript ) {
          sh "[ -f .deploy/Scripts/$config.PostDeployScript ] && envsubst < .deploy/Scripts/$config.PostDeployScript | sh || echo 'No Post-Deployment Script to Deploy'"
      }
  }
}
