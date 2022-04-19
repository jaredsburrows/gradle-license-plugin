package com.jaredsburrows.license

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS
import static test.TestUtils.gradleWithCommand

import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import spock.lang.Unroll

final class LicensePluginSpec extends Specification {
  @Rule public final TemporaryFolder testProjectDir = new TemporaryFolder()
  private List<File> pluginClasspath
  private String classpathString
  private File buildFile

  def 'setup'() {
    def pluginClasspathResource = getClass().classLoader.getResource('plugin-classpath.txt')
    if (pluginClasspathResource == null) {
      throw new IllegalStateException(
        'Did not find plugin classpath resource, run `testClasses` build task.')
    }

    pluginClasspath = pluginClasspathResource.readLines().collect { new File(it) }
    classpathString = pluginClasspath
      .collect { it.absolutePath.replace('\\', '\\\\') } // escape backslashes in Windows paths
      .collect { "'$it'" }
      .join(", ")
    buildFile = testProjectDir.newFile('build.gradle')
  }

  def 'apply with buildscript'() {
    given:
    buildFile <<
      """
      buildscript {
        repositories {
          mavenCentral()
          google()
        }

        dependencies {
          classpath files($classpathString)
        }
      }

      apply plugin: 'java'
      apply plugin: 'com.jaredsburrows.license'
      """

    when:
    def result = gradleWithCommand(testProjectDir.root, 'licenseReport', '-s')

    then:
    result.task(':licenseReport').outcome == SUCCESS
  }

  def 'apply with plugins'() {
    given:
    buildFile <<
      """
      plugins {
        id 'java'
        id 'com.jaredsburrows.license'
      }
      """

    when:
    def result = gradleWithCommand(testProjectDir.root, 'licenseReport', '-s')

    then:
    result.task(':licenseReport').outcome == SUCCESS
  }

  @Unroll def 'apply with allowed java plugins: #javaPlugin'() {
    given:
    buildFile <<
      """
      plugins {
        id '${javaPlugin}'
        id 'com.jaredsburrows.license'
      }
      """

    when:
    def result = gradleWithCommand(testProjectDir.root, 'licenseReport', '-s')

    then:
    result.task(':licenseReport').outcome == SUCCESS

    where:
    javaPlugin << [
      'application', // JavaApplicationPlugin
      'groovy', // GroovyPlugin
      'java', // JavaPlugin
      'java-gradle-plugin', // JavaGradlePluginPlugin
      'java-library', // JavaLibraryPlugin
      'scala', // ScalaPlugin
    ]
  }

  @Unroll def 'apply with allowed android plugins: #androidPlugin'() {
    given:
    testProjectDir.newFile('settings.gradle') <<
      """
      include 'subproject'
      """

    testProjectDir.newFolder('subproject')

    testProjectDir.newFile('subproject/build.gradle') <<
      """
     buildscript {
        repositories {
          mavenCentral()
          google()
        }

        dependencies {
          classpath "com.android.tools.build:gradle:3.6.4"
          classpath files($classpathString)
        }
      }

      apply plugin: 'com.android.application'
      apply plugin: 'com.jaredsburrows.license'

      android {
        compileSdkVersion 28

        defaultConfig {
          if (project.plugins.hasPlugin("com.android.application")) applicationId 'com.example'
          if (project.plugins.hasPlugin("com.android.test")) targetProjectPath ':subproject'
        }
      }
      """

    buildFile <<
      """
      buildscript {
        repositories {
          mavenCentral()
          google()
        }

        dependencies {
          classpath "com.android.tools.build:gradle:3.6.4"
          classpath files($classpathString)
        }
      }

      apply plugin: '${androidPlugin}'
      apply plugin: 'com.jaredsburrows.license'

      android {
        compileSdkVersion 28

        defaultConfig {
          if (project.plugins.hasPlugin("com.android.application")) applicationId 'com.example'
          if (project.plugins.hasPlugin("com.android.test")) targetProjectPath ':subproject'
        }
      }
      """

    when:
    def result = gradleWithCommand(testProjectDir.root, 'licenseDebugReport', '-s')

    then:
    result.task(':licenseDebugReport').outcome == SUCCESS

    where:
    androidPlugin << [
      // AppPlugin
      'android',
      'com.android.application',
      // FeaturePlugin
      'com.android.feature',
      // LibraryPlugin
      'android-library',
      'com.android.library',
      // TestPlugin
      'com.android.test',
    ]
  }
}
