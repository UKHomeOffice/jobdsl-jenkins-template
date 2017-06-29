import groovy.xml.*
import groovy.json.*

def reader = new BufferedReader(
              new FileReader("${JENKINS_HOME}/ci-realm.json/ci-users-0.json"))
def keycloakExport = new JsonSlurper().parse(reader)

def users = keycloakExport.users

int outerIndex = 0
def developers = []
def testers = []
def techLeads = []
def admins = []

while(users[outerIndex] != null)
{
	String tempString = users[outerIndex].username
	println tempString

	if(users[outerIndex].groups.contains("/admins"))
	{
		admins.add(users[outerIndex].username)
	}
	if(users[outerIndex].groups.contains("/developers"))
	{
		developers.add(users[outerIndex].username)
	}
	if(users[outerIndex].groups.contains("/testers"))
	{
		testers.add(users[outerIndex].username)
	}
	if(users[outerIndex].groups.contains("/techLeads"))
	{
		techLeads.add(users[outerIndex].username)
	}

	outerIndex++
}


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

//def devUsers = ['ogbonnahd']
//def testUsers = ['testuser', 'newuser']

blueProjectsjobDefn.each { entry ->
  println "View  " + entry.key
	entry.value.each { job ->
        println "Job  " + job.key
		jobName = job.key;
		jobVCS = job.value;
		projectType = 'blueProject';
		tests = testers;
		devs = developers;
		buildMultiBranchJob(jobName, jobVCS, projectType, tests, devs)
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
		buildMultiBranchJob(jobName, jobVCS, projectType, tests, devs)
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
def buildMultiBranchJob(jobName, jobVCS, projectType, tests, devs) {


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
			while(innerIndex < devs.size())
			{
				permString = tempString + ":" + devs.get(innerIndex)
				PermissionsList.add(permString)
				innerIndex++
			}

			innerIndex = 0
			outerIndex++
		}

		outerIndex = 0

		while(outerIndex < testBlueProjectsPermissionsList.size())
		{
			String tempString = testBlueProjectsPermissionsList.get(outerIndex)
			while(innerIndex < tests.size())
			{
				permString = tempString + ":" + tests.get(innerIndex)
				PermissionsList.add(permString)
				innerIndex++
			}

			innerIndex = 0
			outerIndex++
		}

	} else {

		while(outerIndex < devRedProjectsPermissionsList.size())
		{
			String tempString = devRedProjectsPermissionsList.get(outerIndex)
			while(innerIndex < devs.size())
			{
				permString = tempString + ":" + devs.get(innerIndex)
				PermissionsList.add(permString)
				innerIndex++
			}

			innerIndex = 0
			outerIndex++
		}

		outerIndex = 0

		while(outerIndex < testRedProjectsPermissionsList.size())
		{
			String tempString = testRedProjectsPermissionsList.get(outerIndex)
			while(innerIndex < tests.size())
			{
				permString = tempString + ":" + tests.get(innerIndex)
				PermissionsList.add(permString)
				innerIndex++
			}

			innerIndex = 0
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
				permission('hudson.model.Item.Build:techLeads')
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
