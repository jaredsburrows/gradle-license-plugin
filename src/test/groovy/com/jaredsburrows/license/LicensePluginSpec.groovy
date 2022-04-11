package com.jaredsburrows.license

import org.gradle.testfixtures.ProjectBuilder
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS
import static test.TestUtils.gradleWithCommand

final class LicensePluginSpec extends Specification {
  @Rule
  public final TemporaryFolder testProjectDir = new TemporaryFolder()
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

  def 'licenseReport with no java or android plugins'() {
    when:
    def project = ProjectBuilder.builder().build()
    new LicensePlugin().apply(project) // apply plugin: "com.jaredsburrows.license"

    then:
    def e = thrown UnsupportedOperationException
    e.message == "'com.jaredsburrows.license' requires Java or Android Gradle Plugins."
  }

  def '2 licenseReport with no java or android plugins'() {
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

      apply plugin: 'com.jaredsburrows.license'
      """

    when:
//    def result = gradleWithCommand(testProjectDir.root, 'licenseReport', '-s')
    def result = GradleRunner.create()
      .withProjectDir(testProjectDir.root)
      .withArguments('licenseReport', '-s')
      .withPluginClasspath()
      .buildAndFail()

    then:
//    result.task(':licenseReport').outcome == SUCCESS
    def e = thrown UnsupportedOperationException
    e.message == "'com.jaredsburrows.license' requires Java or Android Gradle Plugins."
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
}
