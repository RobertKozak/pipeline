#!/usr/bin/env groovy

def renderTemplate(input, variables) {
   sh 'env > env.tmp'
   readFile('env.tmp').split("\r?\n").each { env ->
       def (key, value) = env.split("=")
       variables["${key}"]="${value}"
   }
   sh 'rm env.tmp'

   parsedTemplate = input
   variables.each() { key, value ->
      token = '${'+key+'}'
      parsedTemplate = parsedTemplate.replace(token, "${value}")
      token = '__'+key+'__'   
      parsedTemplate = parsedTemplate.replace(token, "${value}") 
   }
   return parsedTemplate
}

def buildFile(file, config)
{
    def fileContents = sh (
        script: "cat ${file}",
        returnStdout: true
    ).trim()

    if ( fileContents?.trim() ) {
        def template = renderTemplate(fileContents, config)
    
        def newfile = "${file}".replace('.template','')
        writeFile file: "${newfile}", text: "${template}"
        sh "cat ${newfile}"
    }
}

def call(Map config) {
    sh 'find . -name *.template > .templatefiles'
    def files = readFile( ".templatefiles" ).split( "\\r?\\n" );
    sh 'rm -f .templatefiles'

    if ( files ) {
        files.each { file -> 
            buildFile(file, config) 
        }
    }
}

