#!/usr/bin/env groovy

def call(Map config) {
  script {
      DeployHelmServices(config)

      CreateDefaultKubernetesManifests(config, ".deploy/Kubernetes/$config.DeployManifest", 'deploy')

      if ( config.DeployManifest ) {
          sh "[ -f .deploy/Kubernetes/$config.DeployManifest ] && envsubst < .deploy/Kubernetes/$config.DeployManifest | kubectl apply -n $config.Namespace -f - && kubectl get pods -n $config.Namespace || echo 'No Deploment Manifest to Deploy'"
      }
      if ( config.DeployScript ) {
          sh "[ -f .deploy/Scripts/$config.DeployScript ] && envsubst < .deploy/Scripts/$config.DeployScript | sh || echo 'No Deployment Script to Deploy'"
      }
   }
}
