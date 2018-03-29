package com.jaredsburrows.license

import org.gradle.testkit.runner.GradleRunner
import spock.lang.Unroll
import test.BaseAndroidSpecification

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

final class LicensePluginAndroidSpec extends BaseAndroidSpecification {
  @Unroll def "android project running licenseDebugReport using with gradle #gradleVersion with android gradle plugin #agpVersion"() {
    given:
    def classpathString = mainPluginClasspath
      .collect { it.absolutePath.replace('\\', '\\\\') } // escape backslashes in Windows paths
      .collect { "'$it'" }
      .join(", ")

    buildFile <<
      """
        buildscript {
          repositories {
            jcenter()
            google()
          }

          dependencies {
            classpath "com.android.tools.build:gradle:${agpVersion}"
            classpath files($classpathString)
          }
        }

        apply plugin: "com.android.application"
        apply plugin: "com.jaredsburrows.license"

        android {
          compileSdkVersion ${COMPILE_SDK_VERSION}
          buildToolsVersion "${BUILD_TOOLS_VERSION}"

          defaultConfig {
            applicationId "${APPLICATION_ID}"
          }
        }
      """.stripIndent().trim()

    when:
    def result = GradleRunner.create()
      .withGradleVersion(gradleVersion)
      .withProjectDir(testProjectDir.root)
      .withArguments("licenseDebugReport")
      .build()

    then:
    result.task(":licenseDebugReport").outcome == SUCCESS
    result.output.find("Wrote HTML report to file:///.*/build/reports/licenses/licenseDebugReport.html.")
    result.output.find("Wrote JSON report to file:///.*/build/reports/licenses/licenseDebugReport.json.")

    where:
    [gradleVersion, agpVersion] << [
      [
        "4.1",
        "4.2",
        "4.3",
        "4.4",
        "4.5",
        "4.6"
      ],
      [
        "2.3.0",
        "3.0.0",
        "3.1.0"
      ]
    ].combinations()
  }

  def "android project with buildTypes using with gradle #gradleVersion"() {
    given:
    def classpathString = mainTestPluginClasspath
      .collect { it.absolutePath.replace('\\', '\\\\') } // escape backslashes in Windows paths
      .collect { "'$it'" }
      .join(", ")

    buildFile <<
      """
        buildscript {
          dependencies {
            classpath files($classpathString)
          }
        }

        apply plugin: "com.android.application"
        apply plugin: "com.jaredsburrows.license"

        android {
          compileSdkVersion ${COMPILE_SDK_VERSION}
          buildToolsVersion "${BUILD_TOOLS_VERSION}"

          defaultConfig {
            applicationId "${APPLICATION_ID}"
          }
        }
      """.stripIndent().trim()

    when:
    def result = GradleRunner.create()
      .withGradleVersion(gradleVersion)
      .withProjectDir(testProjectDir.root)
      .withArguments("licenseDebugReport")
      .build()

    then:
    result.task(":licenseDebugReport").outcome == SUCCESS
    result.output.find("Wrote HTML report to file:///.*/build/reports/licenses/licenseDebugReport.html.")
    result.output.find("Wrote JSON report to file:///.*/build/reports/licenses/licenseDebugReport.json.")

    where:
    gradleVersion << [
      "4.1",
      "4.2",
      "4.3",
      "4.4",
      "4.5",
      "4.6",
    ]
  }

  def "android project with buildTypes"() {
    given:
    def classpathString = mainTestPluginClasspath
      .collect { it.absolutePath.replace('\\', '\\\\') } // escape backslashes in Windows paths
      .collect { "'$it'" }
      .join(", ")

    buildFile <<
      """
        buildscript {
          dependencies {
            classpath files($classpathString)
          }
        }

        apply plugin: "com.android.application"
        apply plugin: "com.jaredsburrows.license"

        android {
          compileSdkVersion ${COMPILE_SDK_VERSION}
          buildToolsVersion "${BUILD_TOOLS_VERSION}"

          defaultConfig {
            applicationId "${APPLICATION_ID}"
          }
        }
      """.stripIndent().trim()

    when:
    def result = GradleRunner.create()
      .withProjectDir(testProjectDir.root)
      .withArguments("licenseDebugReport")
      .build()

    then:
    result.task(":licenseDebugReport").outcome == SUCCESS
    result.output.find("Wrote HTML report to file:///.*/build/reports/licenses/licenseDebugReport.html.")
    result.output.find("Wrote JSON report to file:///.*/build/reports/licenses/licenseDebugReport.json.")
  }

  def "android project with buildTypes and productFlavors"() {
    given:
    def classpathString = mainTestPluginClasspath
      .collect { it.absolutePath.replace('\\', '\\\\') } // escape backslashes in Windows paths
      .collect { "'$it'" }
      .join(", ")

    buildFile <<
      """
        buildscript {
          dependencies {
            classpath files($classpathString)
          }
        }

        apply plugin: "com.android.application"
        apply plugin: "com.jaredsburrows.license"

        android {
          compileSdkVersion ${COMPILE_SDK_VERSION}
          buildToolsVersion "${BUILD_TOOLS_VERSION}"

          defaultConfig {
            applicationId "${APPLICATION_ID}"
          }

          flavorDimensions "a", "b"

          productFlavors {
            flavor1 { dimension "a" }
            flavor2 { dimension "a" }
            flavor3 { dimension "b" }
            flavor4 { dimension "b" }
          }
        }
      """.stripIndent().trim()

    when:
    def result = GradleRunner.create()
      .withProjectDir(testProjectDir.root)
      .withArguments("licenseFlavor1Flavor3DebugReport")
      .build()

    then:
    result.task(":licenseFlavor1Flavor3DebugReport").outcome == SUCCESS
    result.output.find("Wrote HTML report to file:///.*/build/reports/licenses/licenseFlavor1Flavor3DebugReport.html.")
    result.output.find("Wrote JSON report to file:///.*/build/reports/licenses/licenseFlavor1Flavor3DebugReport.json.")
  }
}
