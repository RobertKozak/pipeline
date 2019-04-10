#!/usr/bin/env groovy

def call(Map config, String deployScript = 'deploy.yml') {
    // Need a better way to check if allowed to do RollingUpdate
    // Right now just checking for RollingUpdate string in yml file but this is a default. What if it is not there?
    script {
    //  sh "grep 'RollingUpdate' .deploy/Kubernetes/$deployScript"
    //  sh 'echo result: \$?"
    //  sh "[ \$? -eq 0 ] && [ -f .deploy/Kubernetes/$deployScript ] && envsubst < .deploy/Kubernetes/$deployScript | kubectl rollout status -n $config.Namespace -f -"
    
    //  sh "[ -f .deploy/Kubernetes/$deployScript ] && envsubst < .deploy/Kubernetes/$deployScript | kubectl rollout status -n $config.Namespace -f -"
    }
}
