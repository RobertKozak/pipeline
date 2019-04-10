#!/usr/bin/env groovy

//  EXAMPLE
//  ASPNet {
//      Name = 'HelloWorld'
//      Env = 'dev'
//      AnsibleBuildFile = '.deploy/ansible/ansible.build'
//
//      Configurations = [ dev:
//                         [ CONNECTIONSTRING: 'server=db.example.dev;database=tracking;user=tracking_user;password=Testing.Dev' ],
//                         prod:
//                         [ CONNECTIONSTRING: 'server=db.example.com;database=tracking;user=tracking_user;password=needspassword' ]
//                       ]
//  }

// ImageTag = $config.Name.ToLowerCase()
// DryRun = true
// OTHER Defaults
//    Secrets = []
//    TestData = []

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
//    SkipAnsibleBuild = false
//    SkipPreDeployment = false
//    SkipPostDeployment = false
//    SkipDeployAnsible = false
//    SkipWaitForDeployment = false   
//    SkipCanaryTest = false


def call(Map config) {
    def defaultConfig = config

    script {
        bat 'type Jenkinsfile' 

        if ( !config.containsKey('Name') ) { 
           println "Name is a Required field in the Jenkinsfile"
           System.exit(1)
        }

        env.COMMIT_ID = "${env.GIT_COMMIT[0..6]}"  

        defaultConfig.ImageName = config.Name.toLowerCase()
        defaultConfig.SolutionName = config.Name
        defaultConfig.NugetSource = "https://www.nuget.org/api/v2" 
        defaultConfig.ReleaseConfiguration = 'Release'
        defaultConfig.Platform = 'Any CPU'
        defaultConfig.ToolsVersion = "14.0"
        defaultConfig.VersionNumber = new Date().format("yy.MM") +".${env.BUILD_NUMBER}"
        defaultConfig.MSBuildArgs = "/p:GenerateProjectSpecificOutputFolder=true /tv:${defaultConfig.ToolsVersion} /p:GenerateBuildInfoConfigFile=false /p:VisualStudioVersion=${defaultConfig.ToolsVersion}"
        defaultConfig.OctoPackArgs = "/p:RunOctoPack=true /p:OctoPackPackageVersion=${defaultConfig.VersionNumber}"
        defaultConfig.MSBuildCommandLine = "\"C:\\Program Files (x86)\\MSBuild\\14.0\\bin\\amd64\\MSBuild.exe\" ${config.SolutionName}.sln /p:Configuration=${config.ReleaseConfiguration} /p:Platform=\"${config.Platform}\" /p:ProductVersion=${defaultConfig.VersionNumber} ${defaultConfig.MSBuildArgs} ${defaultConfig.OctoPackArgs} " 

        if ( !config.containsKey('Env') ) { defaultConfig.Env = 'dev' } 
        if ( !config.containsKey('ImageTag') ) { defaultConfig.ImageTag = 'latest' } 
        if ( config.containsKey('DryRun') && config.DryRun ) { defaultConfig.DryRun = '--dry-run' } 

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


        if (config.containsKey('Configurations') ) {
           ConfigurationTemplate = "envFrom:\n"
           ConfigurationTemplate += "    - configMapRef:\n"
           ConfigurationTemplate += "        name: ${defaultConfig.Name}-environment"
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

    }

    return defaultConfig
}
