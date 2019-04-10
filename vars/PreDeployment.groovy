#!/usr/bin/env groovy

def call(Map config) {
  script {
      if ( config.PreDeployScript ) {
          sh "[ -f .deploy/Scripts/$config.PreDeployScript ] && envsubst < .deploy/Scripts/$config.PreDeployScript | sh || echo 'No Pre-Deployment Script to Deploy'"
      }
  }
}
