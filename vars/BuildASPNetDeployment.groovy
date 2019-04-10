#!/usr/bin/env groovy

def call(Map config) {
    println "Enter BuildAnsibleDeployment"
    bat "D:\\Jenkins\\Tools\\nuget.exe restore ${config.SolutionName}.sln -source ${config.NugetSource}"
    bat "${config.MSBuildCommandLine} /m" 
}
