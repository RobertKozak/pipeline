#!/usr/bin/env groovy

//  EXAMPLE
//  Kubernetes {
//      Name = 'Tracking'
//      Namespace = 'service'
//      Cluster = 'dev'
//      NodePort = 30002 // Or TargetPort: 8080
//
//      Configurations = [ dev:
//                         [ CONNECTIONSTRING: 'server=db.example.dev;database=tracking;user=tracking_user;password=Testing.Dev' ],
//                         prod:
//                         [ CONNECTIONSTRING: 'server=db.example.com;database=tracking;user=tracking_user;password=needspassword' ]
//                       ]
//      Volumes = [ [ Name: 'Templates', HostPath: '/var/templates', MountPath: '/var/templates' ] ]      
//
//      Services = [ 
//                   [ 
//                     Name: 'redis', 
//                     Repo: 'git@github.com:kubernetes/charts.git', 
//                     Wait: false, 
//                     ValuesFile: 'redis-values.yml', 
//                     DryRun: false, 
//                     Timeout: 300, 
//                     Namespace: 'service', 
//                     Version: '1.0.0.0',
//                     Values: [] 
//                   ] 
//                 ]
//
//
//      TestData = [ 
//                   TEST_Templates: '316',
//                   TEST_Version: '1.0.7.31'
//                 ]

//  }


// OTHER Defaults
//    ImageTag = $COMMIT_ID 
//    ImageName = 'tracking'  
//    BuildType = angular, dotnetcore, dotnet, php, etc
//    WorkDir = '/app'
//    Replicas = 2
//    Port = 80
//    MarkAsLatest = true
//    BuildFiles = 'Tracking/out
//    ImagePullPolicy = IfNotPresent
//    BuildType = angular 

//    ReadinessProbe = [ Path: '/home/readyz', Port: 8080, Delay: 10, Period: 10 ]
//    LivenessProbe = [ Path: '/home/readyz', Port: 8080, Delay: 10, Period: 10 ]

//    Secrets = []
//    TestData = []
//    ResourceRequest = [ CPU: '250m', Memory: '256Mi' ]
//    ResourceLimit = [ CPU: '250m', Memory: '256Mi' ]

//    DeployManifest = 'deploy.yml'
//    InitializeManifest = 'initialize.yml'
//    FinalizeManifest = 'finalize.yml'
//    PreDeployScript = 'predeploy.sh'   
//    DeployScript = 'deploy.sh'   
//    PostDeployScript = 'postdeploy.sh'
//    InitializeScript = 'initialize.sh'
//    FinalizeScript = 'finalize.sh'
//    CanaryTestScript = 'canarytest.sh'

//    SkipInitialization = false
//    SkipFinalization = false
//    SkipDockerBuild = false
//    SkipPreDeployment = false
//    SkipPostDeployment = false
//    SkipDeployToKubernetes = false
//    SkipWaitForDeployment = false   
//    SkipCanaryTest = false

//    SkipDockerVarReplacement = false

//    DockerRepo = repo.example.com/docker
def call(Map config) {
    def defaultConfig = config

    script {
        def jenkinsfile = new File("Jenkinsfile")
        if ( jenkinsfile.exists()) {
            sh 'cat Jenkinsfile' 
        }
        

        if ( !config.containsKey('MarkAsLatest') ) { defaultConfig.MarkAsLatest = false }

        // default: repo.example.com/docker/ but set to docker.io/ now with empty string
        if ( !config.containsKey('DockerRepo') ) { 
            defaultConfig.DockerRepo = 'docker.io/' 
        } 

        if ( !config.containsKey('Name') ) { 
           println "Name is a Required field in the Jenkinsfile"
           System.exit(1)
        }

        if ( !config.containsKey('ServiceName') ) {
           defaultConfig.ServiceName = defaultConfig.Name.toLowerCase()
        }
        else {
           defaultConfig.ServiceName = defaultConfig.ServiceName.toLowerCase()
        }

        env.COMMIT_ID = "${env.GIT_COMMIT[0..6]}"  
        defaultConfig.ReadinessProbeTemplate = ''
        defaultConfig.LivenessProbeTemplate = ''
        defaultConfig.ConfigurationTemplate = ''
        defaultConfig.TestDataTemplate = ''
        defaultConfig.VolumesTemplate = ''
        defaultConfig.VolumeMountsTemplate = ''
        defaultConfig.ServicePortTemplate = ''

        // Initialize and Finalize Scripts
        if ( !config.containsKey('InitializeManifest') ) { defaultConfig.InitializeManifest = 'initialize.yml' }
        if ( !config.containsKey('FinalizeManifest') ) { defaultConfig.FinalizeManifest = 'finalize.yml' }
        if ( !config.containsKey('DeployManifest') ) { defaultConfig.DeployManifest = 'deploy.yml' }
        if ( !config.containsKey('InitializeScript') ) { defaultConfig.InitializeScript = 'initialize.sh' }
        if ( !config.containsKey('FinalizeScript') ) { defaultConfig.FinalizeScript = 'finalize.sh' }
        if ( !config.containsKey('PreDeployScript') ) { defaultConfig.PreDeployScript = 'predeploy.sh' }
        if ( !config.containsKey('DeployScript') ) { defaultConfig.DeployScript = 'deploy.sh' }
        if ( !config.containsKey('PostDeployScript') ) { defaultConfig.PostDeployScript = 'postdeploy.sh' }
        if ( !config.containsKey('CanaryTestScript') ) { defaultConfig.CanaryTestScript = 'canarytest.sh' }      

        defaultConfig.SkipPreDeployment = defaultConfig.SkipPreDeployment || !fileExists( ".deploy/Scripts/$defaultConfig.PreDeployScript" )
        defaultConfig.SkipPostDeployment = defaultConfig.SkipPostDeployment || !fileExists( ".deploy/Scripts/$defaultConfig.PostDeployScript" ) 
        defaultConfig.SkipWaitForDeployment = defaultConfig.SkipWaitForDeployment || defaultConfig.SkipDeployToKubernetes
        defaultConfig.SkipCanaryTest = defaultConfig.SkipCanaryTest || !fileExists( ".deploy/Test/$defaultConfig.CanaryTestScript" )

        if ( !config.containsKey('SkipDockerVarReplacement') ) { defaultConfig.SkipDockerVarReplacement = false } 

        // Kubernetes configs
        if ( !config.containsKey('ImagePullPolicy') ) { defaultConfig.ImagePullPolicy = 'IfNotPresent' }
        if ( !config.containsKey('Replicas') ) { defaultConfig.Replicas = 2 }
        if ( !config.containsKey('Port') ) { defaultConfig.Port = 80 }
        if ( !config.containsKey('BuildType') ) { defaultConfig.BuildType = 'angular' }
        if ( !config.containsKey('Cluster') ) { defaultConfig.Cluster = 'dev' }    
        if ( !config.containsKey('Namespace') ) { defaultConfig.Namespace = 'service' } else { defaultConfig.Namespace = defaultConfig.Namespace.toLowerCase() }

        if ( config.containsKey('ReadinessProbe') ) {
           if ( !defaultConfig.ReadinessProbe.containsKey('Path') ) { defaultConfig.ReadinessProbe.Path = '/home/healthz' }
	   if ( !defaultConfig.ReadinessProbe.containsKey('Port') ) { defaultConfig.ReadinessProbe.Port = 80 }
           if ( !defaultConfig.ReadinessProbe.containsKey('Delay') ) { defaultConfig.ReadinessProbe.Delay = 10 } 
           if ( !defaultConfig.ReadinessProbe.containsKey('Period') ) { defaultConfig.ReadinessProbe.Period = 10 }

           ReadinessProbeTemplate = 
"""readinessProbe:
          httpGet:
            path: $defaultConfig.ReadinessProbe.Path
            port: $defaultConfig.ReadinessProbe.Port
          initialDelaySeconds: $defaultConfig.ReadinessProbe.Delay
          periodSeconds: $defaultConfig.ReadinessProbe.Period"""

           defaultConfig.ReadinessProbeTemplate = "${ReadinessProbeTemplate}"
        }

        if ( config.containsKey('LivenessProbe') ) {
           if ( !defaultConfig.LivenessProbe.containsKey('Path') ) { defaultConfig.LivenessProbe.Path = '/home/readyz' }
           if ( !defaultConfig.LivenessProbe.containsKey('Port') ) { defaultConfig.LivenessProbe.Port = 80 }
           if ( !defaultConfig.LivenessProbe.containsKey('Delay') ) { defaultConfig.LivenessProbe.Delay = 10 }
           if ( !defaultConfig.LivenessProbe.containsKey('Period') ) { defaultConfig.LivenessProbe.Period = 10 }

           LivenessProbeTemplate =
"""livenessProbe:
          httpGet:
            path: $defaultConfig.LivenessProbe.Path
            port: $defaultConfig.LivenessProbe.Port
          initialDelaySeconds: $defaultConfig.LivenessProbe.Delay
          periodSeconds: $defaultConfig.LivenessProbe.Period"""
 
           defaultConfig.LivenessProbeTemplate = "${LivenessProbeTemplate}" 
        }

        if ( config.containsKey('ResourceRequest') ) {
           if ( !defaultConfig.ResourceRequest.containsKey('CPU') ) { defaultConfig.ResourceRequest.CPU = '250m' }
           if ( !defaultConfig.ResourceRequest.containsKey('Memory') ) { defaultConfig.ResourceRequest.Memory = '256Mi' }
        }
        else {
           defaultConfig.ResourceRequest = [ CPU: '250m', Memory: '256Mi' ]
        }

        if ( config.containsKey('ResourceLimit') ) {
           if ( !defaultConfig.ResourceLimit.containsKey('CPU') ) { defaultConfig.ResourceLimit.CPU = '250m' }
           if ( !defaultConfig.ResourceLimit.containsKey('Memory') ) { defaultConfig.ResourceLimit.Memory = '256Mi' }
        }
        else {
           defaultConfig.ResourceLimit = [ CPU: '250m', Memory: '256Mi' ]
        }

        if (config.containsKey('Configurations') ) {
           ConfigurationTemplate = "envFrom:\n"
           ConfigurationTemplate += "        - configMapRef:\n"
           ConfigurationTemplate += "            name: ${defaultConfig.Name}-environment"
           defaultConfig.ConfigurationTemplate = "${ConfigurationTemplate}" 
        }

// Not convinced this is the right thing to do. Maybe better would be to define a set of Containers to run
//        if (config.containsKey('TestData') ) {
//           if ( !config.containsKey('Configurations') ) {
//               TestDataTemplate ="envFrom:\n"
//           } 
//           TestDataTemplate += "    - configMapRef:\n"
//           TestDataTemplate += "        name: ${defaultConfig.Name}-test-data"
//           defaultConfig.TestDataTemplate = "${TestDataTemplate}"                                                                                                                  
//        } 

        // Volumes
        // Volumes = [ [ Name: 'Templates', HostPath: '/var/templates', MountPath: '/var/templates', SubPath: appsettings.json ] ]
        if (config.containsKey('Volumes') ) {
            VolumesTemplate ="volumes:"
            defaultConfig['Volumes'].each{ volume ->
                VolumesTemplate += "\n        - name: ${volume['Name']}"
                if ( volume['HostPath'] != null ) {
                    VolumesTemplate += '\n          hostPath:'
                    VolumesTemplate += "\n            path: ${volume['HostPath']}"
                }
                if ( volume['ConfigMap'] != null ) {
                    VolumesTemplate += '\n          configMap:'
                    VolumesTemplate += "\n            name: ${volume['ConfigMap']}" 
                }
            }

            VolumeMountsTemplate ="volumeMounts:"
            defaultConfig['Volumes'].each{ volume ->
                VolumeMountsTemplate += "\n          - name: ${volume['Name']}"
                VolumeMountsTemplate += "\n            mountPath: ${volume['MountPath']}"
                if ( volume['SubPath'] != null ) {
                    VolumeMountsTemplate += "\n            subPath: ${volume['SubPath']}"
                }
            }

            defaultConfig.VolumesTemplate = VolumesTemplate
            defaultConfig.VolumeMountsTemplate = VolumeMountsTemplate
        }  

       // Labels
       // Labels = [ Key: 'Value' ] 
       LabelsTemplate = "\n    app: ${defaultConfig.ServiceName}\n    service: ${defaultConfig.ServiceName}"
       ContainerLabelsTemplate = "\n        app: ${defaultConfig.ServiceName}\n        service: ${defaultConfig.ServiceName}"   

       if ( config.containsKey('Labels') ) {
           defaultConfig.Labels.each { key, value ->
              LabelsTemplate += "\n    ${key}: ${value}"
              ContainerLabelsTemplate += "\n        ${key}: ${value}"
           }
       }

       defaultConfig.LabelsTemplate = LabelsTemplate 
       defaultConfig.ContainerLabelsTemplate = ContainerLabelsTemplate

       // Services: Helm installations

       if ( config.containsKey('Services') ) {
           defaultConfig.Helm = [:]

           config['Services'].each{ service ->
               defaultConfig.Helm[service['Name']] = [
                    Service: "${service['Name']}",
                    Namespace: "${defaultConfig.Namespace}",
                    Repo: "git@github.com:kubernetes/charts.git",
                    Wait: false,
                    ValuesFile: "${service['Name']}-values.yml",
                    DryRun: false,
                    Timeout: 300
               ]

               if ( service.containsKey('Namespace') ) { defaultConfig.Helm[service.Name].Namespace = "${service.Namespace}" }
               if ( service.containsKey('Service') ) { defaultConfig.Helm[service.Name].Service = "${service.Service}" }
               if ( service.containsKey('Repo') ) { defaultConfig.Helm[service.Name].Repo = "${service.Repo}" }
               if ( service.containsKey('Wait') ) { defaultConfig.Helm[service.Name].Wait = "${service.Wait}" }
               if ( service.containsKey('ValuesFile') ) { defaultConfig.Helm[service.Name].ValuesFile = "${service.ValuesFile}" }
               if ( service.containsKey('DryRun') ) { defaultConfig.Helm[service.Name].DryRun = "${service.DryRun}" }
               if ( service.containsKey('Timeout') ) { defaultConfig.Helm[service.Name].Timeout = "${service.Timeout}" }
               if ( service.containsKey('Version') ) { defaultConfig.Helm[service.Name].Version = "${service.Version}" }

               defaultConfig.Helm[service.Name].CheckInstalledCommand = "helm list --namespace ${service.Namespace} --all --short \'\b"+defaultConfig.Helm[service.Name].Service+"\b\'" 
               defaultConfig.Helm[service.Name].InstallCommand = "helm install stable/"+defaultConfig.Helm[service.Name].Service+" --name ${service.Name} --namespace ${service.Namespace} "
           }
       }

       // ServicePort
       // NodePort = 30002 or TargetPort = 8080
       // default: TargetPort = ${Port}
       ServicePortTemplate = "targetPort: ${defaultConfig.Port}"
       defaultConfig.ServiceType = 'ClusterIP' 

       if ( config.containsKey('NodePort') ) {
          ServicePortTemplate = "nodePort: ${defaultConfig.NodePort}"
          defaultConfig.ServiceType = 'NodePort'
       }
       if ( config.containsKey('TargetPort') ) {
          ServicePortTemplate = "targetPort: ${defaultConfig.TargetPort}"       
       } 
      
       defaultConfig.ServicePortTemplate = ServicePortTemplate

       // Containers configs
       if ( !config.containsKey('WorkDir') ) { defaultConfig.WorkDir = '/app' }
       if ( !config.containsKey('ImageName') ) { defaultConfig.ImageName = "${defaultConfig.Name}".toLowerCase() }
       if ( !config.containsKey('ImageTag') ) { defaultConfig.ImageTag = env.COMMIT_ID }        
       if ( !config.containsKey('BuildFiles') && defaultConfig.BuildType == 'dotnetcore' ) { defaultConfig.BuildFiles = "${defaultConfig.Name}/out" }
       if ( !config.containsKey('BuildFiles') && defaultConfig.BuildType == 'aspnetcore' ) { defaultConfig.BuildFiles = "${defaultConfig.Name}/out" }
       if ( !config.containsKey('BuildFiles') && defaultConfig.BuildType == 'angular' ) { defaultConfig.BuildFiles = "" } 

       if ( !config.containsKey('DockerBuildVersion') && defaultConfig.BuildType == 'dotnetcore' ) { defaultConfig.DockerBuildVersion = "2.0-sdk" }
       if ( !config.containsKey('DockerRunVersion') && defaultConfig.BuildType == 'dotnetcore' ) { defaultConfig.DockerRunVersion = "2.0-runtime" }    
       if ( !config.containsKey('DockerBuildVersion') && defaultConfig.BuildType == 'aspnetcore' ) { defaultConfig.DockerBuildVersion = "2.0.6-2.1.101" }
       if ( !config.containsKey('DockerRunVersion') && defaultConfig.BuildType == 'aspnetcore' ) { defaultConfig.DockerRunVersion = "2.0.6" }
       if ( !config.containsKey('DockerBuildVersion') && defaultConfig.BuildType == 'angular' ) { defaultConfig.DockerBuildVersion = "alpine" }
       if ( !config.containsKey('DockerRunVersion') && defaultConfig.BuildType == 'angular' ) { defaultConfig.DockerRunVersion = "1.12-alpine" }
    }

    return defaultConfig
}
