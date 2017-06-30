# JenkinsJobs

## Overview

  This document explains how to generate multibranch pipeline jobs, using the JobDSL plugin on a Jenkins instance which is configured to use the Keycloak authentication plugin in conjunction with Project-based Matrix Authorization Strategy  

##  Prerequisites

  See screenshots below for guidance. (Setup may appear slightly different on London Jenkins)

1. Keycloak server setup

2. Roles created on Keycloak which have the same name as their corresponding group on Jenkins. These roles should be assigned via role mappings to similarly named Keycloak groups within the Keycloak realm associated with Jenkins

![alt text](screenshots/keycloak_roles.png "Keycloak Roles page")

![alt text](screenshots/keycloak_groups_rolemappings.png "An example of a Keycloak group with available roles and applied role mappings")

![alt text](screenshots/jenkins_configureglobalsecurity.png "Jenkins Configure Security page")

## Editing the Job DSL groovy script

To define each view and the projects within them change:

```

def blueProjectsjobDefn = 	[
					"Blue Projects"	:	// Each Element is a Entry with Key being the project Name and Value being the Git URL
								[
									"springboot-companies"     	: 	"https://github.com/dogbonnahNB/springboot.git",

								]

				]

def redProjectsjobDefn = 	[
					"Red Projects"	:	// Each Element is a Entry with Key being the project Name and Value being the Git URL
								[
									"DavidIMS"     	: 	"https://github.com/dogbonnahNB/DavidIMS.git",

								]

				]

```

This section of code defines the names for two views and the repositories within them. This should be changed to contain the correct view header name, and more repositories can be added to the view e.g:

```
  [
    "DavidIMS"     	: 	"https://github.com/dogbonnahNB/DavidIMS.git",
    "fake-repo"     :   "https://github.com/nonexistantuser/fake-repo.git"  
  ]

```
