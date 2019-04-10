#!/usr/bin/env groovy

def call(Map config) {
  script {
      if ( config.CanaryTestScript ) {
          sh "[ -f .deploy/Test/$config.CanaryTestScript ] && envsubst < .deploy/Test/$config.CanaryTestScript | sh || echo 'No Canary Test Script to Test'"
      }
  }
}
