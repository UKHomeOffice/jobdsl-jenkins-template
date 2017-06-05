import groovy.xml.*

def jobDefn = 	[
					"Generated Projects"	:	// Each Element is a Entry with Key being the project Name and Value being the Git URL
								[
									"springboot-companies"     	: 	"https://github.com/dogbonnahNB/springboot.git",

								]

				]

def configFile = new XmlSlurper().parseText("${JENKINS_HOME}/jenkins_config.xml")
configFile.'**'
					.findAll { it.name() == 'role' && it.@name == 'admin'}
					.each {
							println it.permissions.text() + ":ogbonnahd"
					}



// Don't change anything below unless you know what you doing
jobDefn.each { entry ->
  println "View  " + entry.key
	entry.value.each { job ->
        println "Job  " + job.key
		jobName = job.key;
		jobVCS = job.value;
		buildMultiBranchJob(jobName, jobVCS)
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
def buildMultiBranchJob(jobName, jobVCS) {
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
