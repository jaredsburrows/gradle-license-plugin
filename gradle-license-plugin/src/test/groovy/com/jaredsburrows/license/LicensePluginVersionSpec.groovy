package com.jaredsburrows.license

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import spock.lang.Unroll

final class LicensePluginVersionSpec extends Specification {
  @Rule
  public final TemporaryFolder testProjectDir = new TemporaryFolder()
  private int compileSdkVersion = 34
  private List<File> pluginClasspath
  private String classpathString
  private File buildFile
  private String reportFolder

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
    // In case we're on Windows, fix the \s in the string containing the name
    reportFolder = "${testProjectDir.root.path.replaceAll('\\\\', '/')}/build/reports/licenses"
  }

  @Unroll
  def 'licenseReport using with gradle #gradleVersion'() {
    given:
    buildFile <<
      """
      plugins {
        id 'java'
        id 'com.jaredsburrows.license'
      }
      """

    when:
    def result = GradleRunner.create()
      .withGradleVersion(gradleVersion)
      .withProjectDir(testProjectDir.root)
      .withArguments('licenseReport', '-s')
      .withPluginClasspath()
      .build()

    then:
    result.task(':licenseReport').outcome == SUCCESS
    result.output.find("Wrote CSV report to .*${reportFolder}/licenseReport.csv.")
    result.output.find("Wrote HTML report to .*${reportFolder}/licenseReport.html.")
    result.output.find("Wrote JSON report to .*${reportFolder}/licenseReport.json.")

    where:
    gradleVersion << [
      '7.0.2',
      '7.1.1',
      '7.2',
      '7.3.3',
      '7.4.2' // Always have latest
    ]
  }

  @Unroll
  def 'AGP version 3.6+, gradle: #gradleVersion and AGP: #agpVersion'() {
    given:
    buildFile <<
      """
      buildscript {
        repositories {
          mavenCentral()
          google()
        }

        dependencies {
          classpath "com.android.tools.build:gradle:${agpVersion}"
          classpath files($classpathString)
        }
      }

      apply plugin: 'com.android.application'
      apply plugin: 'com.jaredsburrows.license'

      android {
        compileSdkVersion $compileSdkVersion

        defaultConfig {
          applicationId 'com.example'
        }
      }
      """

    when:
    def result = GradleRunner.create()
      .withGradleVersion(gradleVersion as String)
      .withProjectDir(testProjectDir.root)
      .withArguments('licenseDebugReport', '-s')
      .build()

    then:
    result.task(':licenseDebugReport').outcome == SUCCESS
    result.output.find("Wrote CSV report to .*${reportFolder}/licenseDebugReport.csv.")
    result.output.find("Wrote HTML report to .*${reportFolder}/licenseDebugReport.html.")
    result.output.find("Wrote JSON report to .*${reportFolder}/licenseDebugReport.json.")

    where:
    // https://docs.gradle.org/current/userguide/compatibility.html
    // https://developer.android.com/studio/releases/gradle-plugin
    // 5.6.4+, 3.6.0-3.6.4
    [gradleVersion, agpVersion] << [
      [
        '7.0.2',
        '7.1.1',
        '7.2',
        '7.3.3',
        '7.4.2',
      ],
      [
        '3.6.4',
      ]
    ].combinations()
  }

  @Unroll
  def 'AGP version 4.0+, gradle: #gradleVersion and AGP: #agpVersion'() {
    given:
    buildFile <<
      """
      buildscript {
        repositories {
          mavenCentral()
          google()
        }

        dependencies {
          classpath "com.android.tools.build:gradle:${agpVersion}"
          classpath files($classpathString)
        }
      }

      apply plugin: 'com.android.application'
      apply plugin: 'com.jaredsburrows.license'

      android {
        compileSdkVersion $compileSdkVersion

        defaultConfig {
          applicationId 'com.example'
        }
      }
      """

    when:
    def result = GradleRunner.create()
      .withGradleVersion(gradleVersion as String)
      .withProjectDir(testProjectDir.root)
      .withArguments('licenseDebugReport', '-s')
      .build()

    then:
    result.task(':licenseDebugReport').outcome == SUCCESS
    result.output.find("Wrote CSV report to .*${reportFolder}/licenseDebugReport.csv.")
    result.output.find("Wrote HTML report to .*${reportFolder}/licenseDebugReport.html.")
    result.output.find("Wrote JSON report to .*${reportFolder}/licenseDebugReport.json.")

    where:
    // https://docs.gradle.org/current/userguide/compatibility.html
    // https://developer.android.com/studio/releases/gradle-plugin
    // 6.1.1+, 4.0.0+
    [gradleVersion, agpVersion] << [
      [
        '7.0.2',
        '7.1.1',
        '7.2',
        '7.3.3',
        '7.4.2',
      ],
      [
        '4.0.2',
      ]
    ].combinations()
  }

  @Unroll
  def 'agp version 4.1+, gradle: #gradleVersion and agp: #agpVersion'() {
    given:
    buildFile <<
      """
      buildscript {
        repositories {
          mavenCentral()
          google()
        }

        dependencies {
          classpath "com.android.tools.build:gradle:${agpVersion}"
          classpath files($classpathString)
        }
      }

      apply plugin: 'com.android.application'
      apply plugin: 'com.jaredsburrows.license'

      android {
        compileSdkVersion $compileSdkVersion

        defaultConfig {
          applicationId 'com.example'
        }
      }
      """

    when:
    def result = GradleRunner.create()
      .withGradleVersion(gradleVersion as String)
      .withProjectDir(testProjectDir.root)
      .withArguments('licenseDebugReport', '-s')
      .build()

    then:
    result.task(':licenseDebugReport').outcome == SUCCESS
    result.output.find("Wrote CSV report to .*${reportFolder}/licenseDebugReport.csv.")
    result.output.find("Wrote HTML report to .*${reportFolder}/licenseDebugReport.html.")
    result.output.find("Wrote JSON report to .*${reportFolder}/licenseDebugReport.json.")

    where:
    // https://docs.gradle.org/current/userguide/compatibility.html
    // https://developer.android.com/studio/releases/gradle-plugin
    // 6.5+, 4.1.0+
    [gradleVersion, agpVersion] << [
      [
        '7.0.2',
        '7.1.1',
        '7.2',
        '7.3.3',
        '7.4.2',
      ],
      [
        '4.1.3',
      ]
    ].combinations()
  }

  @Unroll
  def 'agp version 4.2+, gradle: #gradleVersion and agp: #agpVersion'() {
    given:
    buildFile <<
      """
      buildscript {
        repositories {
          mavenCentral()
          google()
        }

        dependencies {
          classpath "com.android.tools.build:gradle:${agpVersion}"
          classpath files($classpathString)
        }
      }

      apply plugin: 'com.android.application'
      apply plugin: 'com.jaredsburrows.license'

      android {
        compileSdkVersion $compileSdkVersion

        defaultConfig {
          applicationId 'com.example'
        }
      }
      """

    when:
    def result = GradleRunner.create()
      .withGradleVersion(gradleVersion as String)
      .withProjectDir(testProjectDir.root)
      .withArguments('licenseDebugReport', '-s')
      .build()

    then:
    result.task(':licenseDebugReport').outcome == SUCCESS
    result.output.find("Wrote CSV report to .*${reportFolder}/licenseDebugReport.csv.")
    result.output.find("Wrote HTML report to .*${reportFolder}/licenseDebugReport.html.")
    result.output.find("Wrote JSON report to .*${reportFolder}/licenseDebugReport.json.")

    where:
    // https://docs.gradle.org/current/userguide/compatibility.html
    // https://developer.android.com/studio/releases/gradle-plugin
    // 6.7.1+, 4.2.0+
    [gradleVersion, agpVersion] << [
      [
        '7.0.2',
        '7.1.1',
        '7.2',
        '7.3.3',
        '7.4.2',
      ],
      [
        '4.2.2',
      ]
    ].combinations()
  }

  @Unroll
  def 'agp version 7.0+, gradle: #gradleVersion and agp: #agpVersion'() {
    given:
    buildFile <<
      """
      buildscript {
        repositories {
          mavenCentral()
          google()
        }

        dependencies {
          classpath "com.android.tools.build:gradle:${agpVersion}"
          classpath files($classpathString)
        }
      }

      apply plugin: 'com.android.application'
      apply plugin: 'com.jaredsburrows.license'

      android {
        compileSdkVersion $compileSdkVersion

        defaultConfig {
          applicationId 'com.example'
        }
      }
      """

    when:
    def result = GradleRunner.create()
      .withGradleVersion(gradleVersion as String)
      .withProjectDir(testProjectDir.root)
      .withArguments('licenseDebugReport', '-s')
      .build()

    then:
    result.task(':licenseDebugReport').outcome == SUCCESS
    result.output.find("Wrote CSV report to .*${reportFolder}/licenseDebugReport.csv.")
    result.output.find("Wrote HTML report to .*${reportFolder}/licenseDebugReport.html.")
    result.output.find("Wrote JSON report to .*${reportFolder}/licenseDebugReport.json.")

    where:
    // https://docs.gradle.org/current/userguide/compatibility.html
    // https://developer.android.com/studio/releases/gradle-plugin
    // 7.0+, 7.0
    [gradleVersion, agpVersion] << [
      [
        '7.0.2',
        '7.1.1',
        '7.2',
        '7.3.3',
        '7.4.2',
      ],
      [
        '7.0.4',
      ]
    ].combinations()
  }

  @Unroll
  def 'agp version 7.1+, gradle: #gradleVersion and agp: #agpVersion'() {
    given:
    buildFile <<
      """
      buildscript {
        repositories {
          mavenCentral()
          google()
        }

        dependencies {
          classpath "com.android.tools.build:gradle:${agpVersion}"
          classpath files($classpathString)
        }
      }

      apply plugin: 'com.android.application'
      apply plugin: 'com.jaredsburrows.license'

      android {
        compileSdkVersion $compileSdkVersion

        defaultConfig {
          applicationId 'com.example'
        }
      }
      """

    when:
    def result = GradleRunner.create()
      .withGradleVersion(gradleVersion as String)
      .withProjectDir(testProjectDir.root)
      .withArguments('licenseDebugReport', '-s')
      .build()

    then:
    result.task(':licenseDebugReport').outcome == SUCCESS
    result.output.find("Wrote CSV report to .*${reportFolder}/licenseDebugReport.csv.")
    result.output.find("Wrote HTML report to .*${reportFolder}/licenseDebugReport.html.")
    result.output.find("Wrote JSON report to .*${reportFolder}/licenseDebugReport.json.")

    where:
    // https://docs.gradle.org/current/userguide/compatibility.html
    // https://developer.android.com/studio/releases/gradle-plugin
    // 7.2+, 7.1
    [gradleVersion, agpVersion] << [
      [
        '7.2',
        '7.3.3',
        '7.4.2',
      ],
      [
        '7.1.3',
      ]
    ].combinations()
  }


  @Unroll
  def 'agp version 7.2+, gradle: #gradleVersion and agp: #agpVersion'() {
    given:
    buildFile <<
      """
      buildscript {
        repositories {
          mavenCentral()
          google()
        }

        dependencies {
          classpath "com.android.tools.build:gradle:${agpVersion}"
          classpath files($classpathString)
        }
      }

      apply plugin: 'com.android.application'
      apply plugin: 'com.jaredsburrows.license'

      android {
        compileSdkVersion $compileSdkVersion

        defaultConfig {
          applicationId 'com.example'
        }
      }
      """

    when:
    def result = GradleRunner.create()
      .withGradleVersion(gradleVersion as String)
      .withProjectDir(testProjectDir.root)
      .withArguments('licenseDebugReport', '-s')
      .build()

    then:
    result.task(':licenseDebugReport').outcome == SUCCESS
    result.output.find("Wrote CSV report to .*${reportFolder}/licenseDebugReport.csv.")
    result.output.find("Wrote HTML report to .*${reportFolder}/licenseDebugReport.html.")
    result.output.find("Wrote JSON report to .*${reportFolder}/licenseDebugReport.json.")

    where:
    // https://docs.gradle.org/current/userguide/compatibility.html
    // https://developer.android.com/studio/releases/gradle-plugin
    // 7.3+, 7.2
    [gradleVersion, agpVersion] << [
      [
        '7.3.3',
        '7.4.2',
      ],
      [
        '7.2.1',
      ]
    ].combinations()
  }
}
