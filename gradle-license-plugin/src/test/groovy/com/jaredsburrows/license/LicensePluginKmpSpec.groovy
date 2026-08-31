package com.jaredsburrows.license

import groovy.json.JsonSlurper
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS
import static test.TestUtils.gradleWithCommand

final class LicensePluginKmpSpec extends Specification {
  @Rule
  public final TemporaryFolder testProjectDir = new TemporaryFolder()
  private String mavenRepoUrl
  private File buildFile
  private String reportFolder
  private String classpathString

  def 'setup'() {
    // withPluginClasspath() exposes only the plugin's own runtime classpath, so a test that needs
    // the Kotlin Gradle Plugin reads the full test runtime classpath instead, as the Android spec
    // does for AGP.
    def pluginClasspathResource = getClass().classLoader.getResource('plugin-classpath.txt')
    if (pluginClasspathResource == null) {
      throw new IllegalStateException(
        'Did not find plugin classpath resource, run `testClasses` build task.')
    }
    classpathString = pluginClasspathResource.readLines()
      .collect { new File(it).absolutePath.replace('\\', '\\\\') }
      .collect { "'$it'" }
      .join(', ')

    mavenRepoUrl = getClass().getResource('/maven').toURI()
    buildFile = testProjectDir.newFile('build.gradle')
    // In case we're on Windows, fix the \s in the string containing the name
    reportFolder = "${testProjectDir.root.path.replaceAll("\\\\", '/')}/build/reports/licenses"
  }

  def 'licenseReport supports a Kotlin Multiplatform project'() {
    given: 'a plain KMP project with a jvm target'
    buildFile <<
      """
      buildscript {
        dependencies {
          classpath files($classpathString)
        }
      }

      apply plugin: 'org.jetbrains.kotlin.multiplatform'
      apply plugin: 'com.jaredsburrows.license'

      repositories {
        maven {
          url '${mavenRepoUrl}'
        }
      }

      kotlin {
        jvm()
        sourceSets {
          jvmMain {
            dependencies {
              implementation 'group:name:1.0.0'
            }
          }
        }
      }
      """

    when: 'the per-target report task is run'
    def result = gradleWithCommand(testProjectDir.root, 'licenseJvmReport', '-s')
    def json = new JsonSlurper().parseText(new File(reportFolder, 'licenseJvmReport.json').text)

    then: 'applying the plugin no longer fails the build'
    result.task(':licenseJvmReport').outcome == SUCCESS

    and: 'the jvm target dependencies are resolved and attributed'
    json*.dependency == ['group:name:1.0.0']
  }
}
