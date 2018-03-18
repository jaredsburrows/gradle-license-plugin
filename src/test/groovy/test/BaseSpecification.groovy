package test

import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class BaseSpecification extends Specification {
  @Rule TemporaryFolder testProjectDir = new TemporaryFolder()
  def buildFile
  def mainPluginClasspath = []
  def mainTestPluginClasspath = []

  def "setup"() {
    buildFile = testProjectDir.newFile("build.gradle")

    def mainPluginClasspathResource = getClass().classLoader.findResource("main-plugin-classpath.txt")
    if (mainPluginClasspathResource == null) {
      throw new IllegalStateException(
        "Did not find plugin classpath resource, run `testClasses` build task.")
    }

    mainPluginClasspath = mainPluginClasspathResource.readLines().collect { new File(it) }

    def mainTestPluginClasspathResource = getClass().classLoader.findResource("main-test-plugin-classpath.txt")
    if (mainTestPluginClasspathResource == null) {
      throw new IllegalStateException(
        "Did not find plugin classpath resource, run `testClasses` build task.")
    }

    mainTestPluginClasspath = mainTestPluginClasspathResource.readLines().collect { new File(it) }
  }
}
