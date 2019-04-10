#!/usr/bin/env groovy

def call(Map config) {
    println "Enter BuildDockerImage"

    sh "docker login -u $DOCKER_HUB_USR -p $DOCKER_HUB_PSW $config.DockerRepo"
 
    CreateDefaultDockerFiles(config)    
    if ( fileExists('Dockerfile.build') )
    {
        sh "docker build -t $config.ImageName:build --file Dockerfile.build ."
        try { 
            script {
                sh "docker create --name extract_$COMMIT_ID $config.ImageName:build"
         
                sh "mkdir -p $COMMIT_ID"
                sh "docker cp extract_$COMMIT_ID:$config.WorkDir/$config.BuildFiles $COMMIT_ID/$config.WorkDir/"
                sh "cp Dockerfile $COMMIT_ID/Dockerfile"

                sh "cd $COMMIT_ID && docker build -t $config.ImageName . && docker tag $config.ImageName $config.DockerRepo$config.ImageName:$config.ImageTag && docker push $config.DockerRepo$config.ImageName:$config.ImageTag"
    
                if ( config.MarkAsLatest ) {
                    sh "docker tag $config.ImageName $config.DockerRepo$config.ImageName:latest && docker push $config.DockerRepo$config.ImageName:latest"
                }
            }
        }
        finally {
           sh "docker rm extract_$COMMIT_ID"
           sh "docker rmi $config.ImageName:build"   
        }
    }
    else
    {
        sh "docker build -t $config.ImageName ." 
//        image = docker.build("$config.ImageName")
        sh "docker tag $config.ImageName $config.DockerRepo$config.ImageName:$config.ImageTag"
        sh "docker push $config.DockerRepo$config.ImageName:$config.ImageTag" 
//        image.push("$config.ImageTag")

        if ( config.MarkAsLatest) {
            sh "docker push $config.DockerRepo$config.ImageName:latest"
//            image.push('latest')
        }
    }
}

