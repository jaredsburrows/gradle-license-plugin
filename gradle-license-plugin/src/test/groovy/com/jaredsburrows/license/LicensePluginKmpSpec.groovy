package com.jaredsburrows.license

import groovy.json.JsonSlurper
import groovy.transform.TypeChecked
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS
import static test.TestUtils.gradleWithCommand

@TypeChecked
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
    URL pluginClasspathResource = getClass().classLoader.getResource('plugin-classpath.txt')
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
    BuildResult result = gradleWithCommand(testProjectDir.root, 'licenseJvmReport', '-s')
    List<Map<String, Object>> json =
      (List<Map<String, Object>>) new JsonSlurper().parseText(new File(reportFolder, 'licenseJvmReport.json').text)
    TaskOutcome outcome = result.task(':licenseJvmReport').outcome
    List<Object> dependencies = json.collect { it.dependency }

    then: 'applying the plugin no longer fails the build'
    outcome == SUCCESS

    and: 'the jvm target dependencies are resolved and attributed'
    dependencies == ['group:name:1.0.0']
  }

  def 'licenseReport resolves a native target, whose configurations are not named like the JVM ones'() {
    given: 'a multiplatform project with a Kotlin/Native target'
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
        mavenCentral()
      }

      kotlin {
        linuxX64()
        sourceSets {
          commonMain {
            dependencies {
              implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0'
            }
          }
        }
      }
      """

    when: 'the per-target report runs'
    BuildResult result = gradleWithCommand(testProjectDir.root, 'licenseLinuxX64Report', '-s')
    TaskOutcome outcome = result.task(':licenseLinuxX64Report').outcome
    String reportText = new File(reportFolder, 'licenseLinuxX64Report.json').text.trim()
    boolean attributesCoroutines = reportText.contains('org.jetbrains.kotlinx:kotlinx-coroutines-core')

    then:
    outcome == SUCCESS

    and: 'the native target resolves linuxX64CompileKlibraries, not a jvm-style name, so it is not empty'
    reportText != '[]'
    attributesCoroutines
  }

}
