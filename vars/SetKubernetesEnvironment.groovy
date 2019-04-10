#!/usr/bin/env groovy

import static groovy.json.JsonOutput.*

def call(Map config) {
    script {
        println prettyPrint(toJson(config))  

        env.KUBECONFIG = "/home/jenkins/.kube/${config.Cluster}-config"

        env_name = "${config.Name}-environment".toLowerCase()
        env_filename = "${env_name}.yml"

        env_vars_config = "kind: ConfigMap\n"
        env_vars_config += "apiVersion: v1\n"
        env_vars_config += "metadata:\n"
        env_vars_config += "  name: ${env_name}\n"
        env_vars_config += "data:\n"

        if (config.Configurations && config.Configurations[config.Cluster]) {
            env = config.Configurations[config.Cluster]
            env.each{ key, value -> 
                env_vars_config += "  ${key}: ${value}\n"
                sh "export ${key}=${value}"
            }

            println prettyPrint(toJson(env_vars_config))

            writeFile file: "${env_filename}", text: env_vars_config

            sh "kubectl apply -n $config.Namespace -f $env_filename && rm $env_filename"
        }
        
println "*** display env vars"
sh 'env'
    }
}
