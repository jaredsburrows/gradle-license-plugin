package com.jaredsburrows.license

import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import spock.lang.Unroll

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS
import static test.TestUtils.gradleWithCommand
import static test.TestUtils.gradleWithCommandWithFail

final class LicensePluginSpec extends Specification {
  @Rule
  public final TemporaryFolder testProjectDir = new TemporaryFolder()
  private int compileSdkVersion = 34
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

  def 'apply plugin with buildscript dsl'() {
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

      apply plugin: 'java-library'
      apply plugin: 'com.jaredsburrows.license'
      """

    when:
    def result = gradleWithCommand(testProjectDir.root, 'licenseReport', '-s')

    then:
    result.task(':licenseReport').outcome == SUCCESS
  }

  def 'apply plugin with buildscript dsl and no other plugins'() {
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
    def result = gradleWithCommandWithFail(testProjectDir.root, 'licenseReport', '-s')

    then:
    result.output.contains("'com.jaredsburrows.license' requires Java, Kotlin or Android Gradle based plugins.")
  }

  def 'apply plugin with plugins dsl'() {
    given:
    buildFile <<
      """
      plugins {
        id 'java-library'
        id 'com.jaredsburrows.license'
      }
      """

    when:
    def result = gradleWithCommand(testProjectDir.root, 'licenseReport', '-s')

    then:
    result.task(':licenseReport').outcome == SUCCESS
  }

  def 'apply plugin with plugins dsl and no other plugins'() {
    given:
    buildFile <<
      """
      plugins {
        id 'com.jaredsburrows.license'
      }
      """

    when:
    def result = gradleWithCommandWithFail(testProjectDir.root, 'licenseReport', '-s')

    then:
    result.output.contains("'com.jaredsburrows.license' requires Java, Kotlin or Android Gradle based plugins.")
  }

  @Unroll
  def 'apply with non java or agp plugins: #plugin'() {
    given:
    buildFile <<
      """
      plugins {
        id '${plugin}'
        id 'com.jaredsburrows.license'
      }
      """

    when:
    def result = gradleWithCommandWithFail(testProjectDir.root, 'licenseReport', '-s')

    then:
    result.output.contains("'com.jaredsburrows.license' requires Java, Kotlin or Android Gradle based plugins.")

    where:
    // https://github.com/gradle/gradle/find/master, search for "gradle-plugins"
    plugin << [
      'assembler', // AssemblerPlugin
      'assembler-lang', // AssemblerLangPlugin
      'c', // CPlugin
      'c-lang', // CLangPlugin
      'cpp', // CppPlugin
      'cpp-application', // CppApplicationPlugin
      'cpp-lang', // CppLangPlugin
      'cpp-library', // CppLibraryPlugin
      'objective-c', // ObjectiveCPlugin
      'objective-c-lang', // ObjectiveCLangPlugin
      'objective-cpp', // ObjectiveCppPlugin
      'objective-cpp-lang', // ObjectiveCppLangPlugin
      'swift-application', // SwiftApplicationPlugin
      'swift-library', // SwiftLibraryPlugin
    ]
  }

  @Unroll
  def 'apply with allowed java plugins: #javaPlugin'() {
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
    // https://github.com/gradle/gradle/find/master, search for "gradle-plugins"
    // Look into supporting java-platform, jvm-ecosystem
    javaPlugin << [
      'antlr', // AntlrPlugin, applies JavaLibraryPlugin, JavaPlugin
      'application', // JavaApplicationPlugin, applies JavaPlugin
      'groovy', // GroovyPlugin, applies JavaPlugin
      'java', // JavaPlugin, applies JavaBasePlugin
      'java-gradle-plugin', // JavaGradlePluginPlugin, applies JavaLibraryPlugin, JavaPlugin
      'java-library', // JavaLibraryPlugin, applies JavaPlugin
      'java-library-distribution', // JavaLibraryDistributionPlugin, applies JavaPlugin
      'scala', // ScalaPlugin, applies JavaPlugin
      'war', // WarPlugin, applies JavaPlugin
    ]
  }

  @Unroll
  def 'apply with allowed android plugins: #androidPlugin'() {
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
          classpath files($classpathString)
        }
      }

      apply plugin: 'com.android.application'
      apply plugin: 'com.jaredsburrows.license'

      android {
        compileSdkVersion $compileSdkVersion

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
          classpath files($classpathString)
        }
      }

      apply plugin: '${androidPlugin}'
      apply plugin: 'com.jaredsburrows.license'

      android {
        compileSdkVersion $compileSdkVersion

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
      // LibraryPlugin
      'android-library',
      'com.android.library',
      // TestPlugin
      'com.android.test',
    ]
  }
}
