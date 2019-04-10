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

def buildFile(file, config, type)
{
    def template
    if ( !fileExists( "${file}" ) ) {
        template = libraryResource "io/k8s/Kubernetes/${type}"
    }
    else
    {
       template = sh (
           script: "cat ${file}",
           returnStdout: true
       ).trim()
    }

    if ( template?.trim() )
    {
        template = renderTemplate(template, config)
        writeFile file: "${file}", text: "${template}"
    }

    if ( fileExists( "${file}" ) ) {
       sh "envsubst < ${file} > ${file}.tmp && mv ${file}.tmp ${file}" 
       sh "cat  ${file}"
    }
}

def call(Map config, String fileName, String configType) {
    sh 'mkdir -p .deploy/Kubernetes' 

    try {
       configType = configType.toLowerCase()
       buildFile("$fileName", config, "${configType}.yml")
    } catch(all) { 
        println "$all" 
    }
}

