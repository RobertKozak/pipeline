#!/usr/bin/env groovy

def call(body) {
   def config = [:]
   body.resolveStrategy = Closure.DELEGATE_FIRST
   body.delegate = config
   body()

   pipeline {
      agent { 
          node { 
              label 'windows' 
              customWorkspace 'D:\\Jenkins\\src\\'
          } 
      }

      options { 
         disableConcurrentBuilds()
      }

      environment {
          DOCKER_HUB = credentials('artifactory-docker')
          //DOCKER_HUB = credentials('docker-hub-credentials')
      }

      stages {

         stage('Setup') {
            steps {
                script {
                    config = SetDefaultASPNetConfig(config)
                    SetEnvironmentVars(config)
//                    ConfigureTemplatedFiles(config)
                }
            }
         }

         stage('Initialization') {
             when {
                expression { !config.SkipInitialization }
             }
             steps {
println "Initialization step"
//                 InitializeAnsible(config)
             }
         }
 
         stage('Build ASPNet Site Deployment') {
            when {
               expression { !config.SkipDockerBuild }
            }
            steps  {
               script {
                  BuildASPNetDeployment(config)   
               }
            }
         }

         stage('Canary Test') {
             when {
                expression { !config.SkipCanaryTest }
             }
             steps {
println "Canary Test step" 
 //                CanaryTest(config)
             }
         }

         stage('Pre Deployment') {
              when {
                 expression { !config.SkipPreDeployment }
              }
              steps {
println "Pre Deployment step" 
//                 PreDeployment(config)
              }
         }

         stage('Deploy ASPNet') {
              when {
                 expression { !config.SkipDeployToKubernetes }
              }
              steps {
println "Deploy ASPNet step" 
//                 DeployASPNet(config)
              }
          } 

          stage('Post Deployment') {
              when {
                 expression { !config.SkipPostDeployment }
              }
              steps {
println "Post Deployment step" 
//                 PostDeployment(config)
              }
          }

          stage('Wait for Deployment to Complete') {
              when {
                 expression { !config.SkipWaitForDeployment }
              }
              steps {
println "Wait for Deployment step" 
//                  WaitASPNetDeployment(config)
              }
          }

          stage('Finalization') {
             when {
                expression { !config.SkipFinalization }
             }
             steps {
println "Finalization step" 
//                 FinalizeAnsible(config)
             }
         }  
      }

//      post {
//         always {
//            script {
//               server = Artifactory.server "Artifactory"
//               buildInfo = Artifactory.newBuildInfo()

//               buildInfo.env.filter.addInclude("*")
//               buildInfo.env.collect()

//               buildInfo.retention maxDays: 30, deleteBuildArtifacts: true

//               def uploadSpec = """{
//                   "files": [{
//                      "pattern": "**/*.yml",
//                      "target": "logs"
//                   }]
//               }"""
//               server.upload spec: uploadSpec, buildInfo: buildInfo
//               server.publishBuildInfo buildInfo
//            }
//         }
//      }
   }
}
