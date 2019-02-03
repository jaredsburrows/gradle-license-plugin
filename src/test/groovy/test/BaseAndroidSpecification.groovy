package test

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

@Deprecated // TODO remove test inheritance, migrate methods to test utils
class BaseAndroidSpecification extends Specification {
  @Rule TemporaryFolder testProjectDir = new TemporaryFolder()
  def buildFile
  def pluginClasspath = []
  Project project
  Project subproject
  def MANIFEST_FILE_PATH = 'src/main/AndroidManifest.xml'
  def MANIFEST = "<manifest xmlns:android=\"http://schemas.android.com/apk/res/android\" package=\"com.example\"/>"

  def 'setup'() {
    // Setup project
    project = ProjectBuilder.builder()
      .withProjectDir(testProjectDir.root)
      .withName('project')
      .build()
    project.repositories {
      maven { url getClass().getResource('/maven').toURI() }
    }

    // Setup subproject
    subproject = ProjectBuilder.builder()
      .withParent(project)
      .withName('subproject')
      .build()
    subproject.repositories {
      maven { url getClass().getResource('/maven').toURI() }
    }

    buildFile = testProjectDir.newFile('build.gradle')

    def pluginClasspathResource = getClass().classLoader.findResource('plugin-classpath.txt')
    if (pluginClasspathResource == null) {
      throw new IllegalStateException(
        'Did not find plugin classpath resource, run `testClasses` build task.')
    }

    pluginClasspath = pluginClasspathResource.readLines().collect { new File(it) }

    // Make sure Android projects have a manifest
    testProjectDir.newFolder('src', 'main')
    testProjectDir.newFile(MANIFEST_FILE_PATH) << MANIFEST
  }
}
