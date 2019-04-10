#!/usr/bin/env groovy

def call(Map config) {
    script {
        CreateDefaultKubernetesManifests(config, ".deploy/Kubernetes/$config.FinalizeManifest", 'finalize') 

        if ( config.FinalizeManifest ) {
            sh "[ -f .deploy/Kubernetes/$config.FinalizeManifest ] && envsubst < .deploy/Kubernetes/$config.FinalizeManifest | kubectl apply -n $config.Namespace -f - || echo 'No Finalize Manifest to Deploy'"
         }

        if ( config.FinalizeaScript ) {
            sh "[ -f .deploy/Scripts/$config.FinalizeScript ] && envsubst < .deploy/Scripts/$config.FinalizeScript | sh || echo 'No Finalize Script to Deploy'"
         }
    } 
}
