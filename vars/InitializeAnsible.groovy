#!/usr/bin/env groovy

def call(Map config) {
  script {
      CreateDefaultKubernetesManifests(config, ".deploy/Kubernetes/$config.InitializeManifest", 'initialize') 

      if ( config.InitializeManifest ) {
          sh "[ -f .deploy/Kubernetes/$config.InitializeManifest ] && envsubst < .deploy/Kubernetes/$config.InitializeManifest | kubectl apply -n $config.Namespace -f - || echo 'No Initialize Manifest to Deploy'"
      }
      if ( config.InitializeScript ) {
          sh "[ -f .deploy/Scripts/$config.InitializeScript ] && envsubst < .deploy/Scripts/$config.InitializeScript | sh || echo 'No Initialize Script to Deploy'"   
      }
  }
}
