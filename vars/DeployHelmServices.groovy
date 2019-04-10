#!/usr/bin/env groovy

def call(Map config) {
    if (config.containsKey('Helm') ) {
        config['Helm'].each{ key, value ->

println "Checking for existing Helm service"

            existingHelmService = sh (
            script: value.CheckInstalledCommand,
            returnStdout: true
            ).trim()

            if (existingHelmService != value.ServiceName) {
//                sh "${value.InstallCommand}"
            }
        }
     }
}

