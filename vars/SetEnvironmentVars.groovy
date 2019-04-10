#!/usr/bin/env groovy

import static groovy.json.JsonOutput.*

def call(Map config) {
    script {
        println prettyPrint(toJson(config))  

       // sh 'env'
    }
}
