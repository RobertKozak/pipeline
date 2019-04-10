#!/usr/bin/env groovy

def call(Map config) {
    println "Enter BuildAnsibleDeployment"
    sshagent (credentials: ['devops-github-access-token']) {
        // fix up permssions for ssh key volume mount
        sh 'mkdir /root/.ssh'
        sh 'cp /root/ssh/ssh-key /root/.ssh/id_rsa'
        sh 'cp /root/ssh/ssh-key-public /root/.ssh/id_rsa.pub'
        sh 'chmod 600 /root/.ssh/id*'
        sh 'touch /etc/ssh/ssh_known_hosts'
        sh 'chmod 666 /etc/ssh/ssh_known_hosts'
        sh 'ssh-keyscan -H github.com > /etc/ssh/ssh_known_hosts'
        
        sh "docker login -u $DOCKER_HUB_USR -p $DOCKER_HUB_PSW $config.DockerRepo"

        sh "cat $config.AnsibleBuildFile"
        sh "ansible-deploy --build --config $config.AnsibleBuildFile --no-prune"
    }
}

