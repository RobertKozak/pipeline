#!/usr/bin/env groovy
import groovy.io.FileType
import groovy.text.StreamingTemplateEngine

def renderTemplate(input, variables) {
    sh 'env > env.tmp'
    readFile('env.tmp').split("\r?\n").each { env ->
        def (key, value) = env.split("=")
        variables["${key}"]="${value}"
    }
    sh 'rm env.tmp'

    def engine = new StreamingTemplateEngine()
    return engine.createTemplate(input).make(variables).toString()
}

def call(Map config) {
    def dockerfile_build = ''
    if ( !fileExists( '.deploy/Docker/Dockerfile.build' ) && !fileExists('.deploy/Docker/Dockerfile') ) {
        println "building Dockerfile.build from template"
        try {
          dockerfile_build = libraryResource "io/k8s/Docker/$config.BuildType/Dockerfile.build"
        } catch(all) {
        }
    }

    if ( fileExists( '.deploy/Docker/Dockerfile.build' ) ) {
       println "building Dockerfile.build from .deploy folder"  
       dockerfile_build = sh (
       script: "cat .deploy/Docker/Dockerfile.build",
           returnStdout: true
        ).trim()
    }

    if ( dockerfile_build?.trim() ) {
        dockerfile_build = renderTemplate(dockerfile_build, config)
        writeFile file: 'Dockerfile.build', text: "${dockerfile_build}"
    }                                                                                                                                                                                                           

    if ( fileExists( 'Dockerfile.build' ) ) {
        sh 'envsubst < Dockerfile.build > Dockerfile.build.tmp && mv Dockerfile.build.tmp Dockerfile.build'
        sh 'cat Dockerfile.build'
    }

    def dockerfile = ''
    if ( !fileExists( ".deploy/Docker/Dockerfile" ) ) { 
        println "building Dockerfile from template"
        dockerfile = libraryResource "io/k8s/Docker/$config.BuildType/Dockerfile"
    }
    else {
       println "building Dockerfile from .deploy folder"
       dockerfile = sh (
           script: "cat .deploy/Docker/Dockerfile",
           returnStdout: true
       ).trim()
    }

    if ( dockerfile?.trim() )
    {
        dockerfile = renderTemplate(dockerfile, config)
        writeFile file: 'Dockerfile', text: "${dockerfile}"
    }

    if ( !config.SkipDockerVarReplacement ) {
        sh 'envsubst < Dockerfile > Dockerfile.tmp && mv Dockerfile.tmp Dockerfile'
    }
    sh 'cat Dockerfile'
}
