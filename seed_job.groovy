//Replace with real project group names
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

blueProjectsjobDefn.each { entry ->
  println "View  " + entry.key
	entry.value.each { job ->
        println "Job  " + job.key
		jobName = job.key;
		jobVCS = job.value;
		projectType = 'blueProject';
		buildMultiBranchJob(jobName, jobVCS, projectType)
	}
  listView("${entry.key}") {
    jobs {
      entry.value.each { job ->
        name("${job.key}")
      }
    }
    columns {
        status()
        weather()
        name()
        lastSuccess()
        lastFailure()
        lastDuration()
        buildButton()
    }
  }

}

redProjectsjobDefn.each { entry ->
  println "View  " + entry.key
	entry.value.each { job ->
        println "Job  " + job.key
		jobName = job.key;
		jobVCS = job.value;
		projectType = 'redProject';
		tests = testers;
		devs = developers;
		buildMultiBranchJob(jobName, jobVCS, projectType)
	}
  listView("${entry.key}") {
    jobs {
      entry.value.each { job ->
        name("${job.key}")
      }
    }
    columns {
				status()
				weather()
				name()
				lastSuccess()
				lastFailure()
				lastDuration()
				buildButton()
    }
  }

}



// Define method to build the job
def buildMultiBranchJob(jobName, jobVCS, projectType) {

  //Each list represents a set of permissions for a group permissions for one set of projects
	def testBlueProjectsPermissionsList = ['hudson.model.Item.Delete']
	def testRedProjectsPermissionsList = [ 'hudson.model.Item.Read', 'hudson.model.Item.Build', 'hudson.model.Item.Move', 'hudson.model.Item.Discover', ]
	def devBlueProjectsPermissionsList = [ 'hudson.model.Item.Workspace', 'hudson.model.Item.Read', 'hudson.model.Item.Configure', 'hudson.model.Item.Delete', 'hudson.model.Item.Cancel', 'hudson.model.Item.Move', 'hudson.model.Item.Discover', 'hudson.model.Item.Create']
	def devRedProjectsPermissionsList = [ 'hudson.model.Item.Workspace', 'hudson.model.Item.Read', 'hudson.model.Item.Build', 'hudson.model.Item.Configure', 'hudson.model.Item.Delete', 'hudson.model.Item.Cancel', 'hudson.model.Item.Move', 'hudson.model.Item.Discover', 'hudson.model.Item.Create']
	def PermissionsList = []

	int outerIndex = 0
	int innerIndex = 0
	def index = 0

	if(projectType.equals('blueProject')) {

		while(outerIndex < devBlueProjectsPermissionsList.size())
		{
			String tempString = devBlueProjectsPermissionsList.get(outerIndex)

			permString = tempString + ":" + "developers"
			PermissionsList.add(permString)

			outerIndex++
		}

		outerIndex = 0

		while(outerIndex < testBlueProjectsPermissionsList.size())
		{
			String tempString = testBlueProjectsPermissionsList.get(outerIndex)

      permString = tempString + ":" + "testers"
      PermissionsList.add(permString)

			outerIndex++
		}

	} else {

		while(outerIndex < devRedProjectsPermissionsList.size())
		{
			String tempString = devRedProjectsPermissionsList.get(outerIndex)

			permString = tempString + ":" + "developers"
			PermissionsList.add(permString)

			outerIndex++
		}

		outerIndex = 0

		while(outerIndex < testRedProjectsPermissionsList.size())
		{
			String tempString = testRedProjectsPermissionsList.get(outerIndex)

			permString = tempString + ":" + "testers"
			PermissionsList.add(permString)

			outerIndex++
		}
	}

	while(index < PermissionsList.size())
	{
		String tempString = PermissionsList.get(index)
		println tempString
		index++
	}

	// Create job
	multibranchPipelineJob(jobName) {
		// Define source
		branchSources {
			branchSourceNodes << new NodeBuilder().'jenkins.branch.BranchSource' {
				source(class: 'jenkins.plugins.git.GitSCMSource') {
					id(UUID.randomUUID())
					remote(jobVCS)
					includes('*')
					excludes('')
					ignoreOnPushNotifications(false)
					extensions {
						localBranch(class: "hudson.plugins.git.extensions.impl.LocalBranch") {
							localBranch('**')
						}
					}
				}
			}
		} // End source

		// Triggers
		triggers {

		  cron('H/15 * * * *')

		} // End of Triggers

		configure { node ->
			node / 'properties' / 'com.cloudbees.hudson.plugins.folder.properties.AuthorizationMatrixProperty' {
				for(int i = 0; i < PermissionsList.size(); i++) {
					String perm = PermissionsList.getAt(i)
					permission(perm)
				}
			}
		}

    configure { node ->
      node / 'properties' << 'org.jenkinsci.plugins.workflow.libs.FolderLibraries' (plugin: 'workflow-cps-global-lib@2.4'){
        libraries {
          'org.jenkinsci.plugins.workflow.libs.LibraryConfiguration' {
            name('common')
              retriever(class: 'org.jenkinsci.plugins.workflow.libs.SCMRetriever') {
                scm(class: 'hudson.plugins.git.GitSCM', plugin: 'git@3.0.0') {
                  configVersion('2')
                    userRemoteConfigs {
                      'hudson.plugins.git.UserRemoteConfig' {
                        url("https://github.com/dogbonnahNB/springboot.git")
                      }
                    }
                    branches {
                      'hudson.plugins.git.BranchSpec' {
                        name('*/master')
                      }
                    }
                    doGenerateSubmoduleConfigurations('false')
                    submoduleCfg(class: 'list')
                    extensions
                }
              }
              defaultVersion('master')
              implicit('false')
              allowVersionOverride('true')
          }

        }
      }
    }

		// How Many Items in the history
		orphanedItemStrategy {
		discardOldItems {
			daysToKeep(0)
			numToKeep(0)
			}
		} // End Orphaned
	} // End Creating
} // End Method
