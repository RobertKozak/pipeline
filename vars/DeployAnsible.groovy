#!/usr/bin/env groovy

def call(Map config) {
  script {
      sh "ansible-deploy --name $config.ImageName -i $config.Env --repo-version $config.ImageTag $config.DryRun $config.AnsibleArgs"
   }
}
