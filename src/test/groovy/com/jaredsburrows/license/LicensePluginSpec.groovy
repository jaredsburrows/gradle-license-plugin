package com.jaredsburrows.license

import com.android.build.gradle.internal.SdkHandler
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification
import spock.lang.Unroll

/**
 * @author <a href="mailto:jaredsburrows@gmail.com">Jared Burrows</a>
 */
final class LicensePluginSpec extends Specification {
  final static def COMPILE_SDK_VERSION = LicenseReportTaskSpec.COMPILE_SDK_VERSION
  final static def BUILD_TOOLS_VERSION = LicenseReportTaskSpec.BUILD_TOOLS_VERSION
  final static def APPLICATION_ID = LicenseReportTaskSpec.APPLICATION_ID
  final static def TEST_ANDROID_SDK = LicenseReportTaskSpec.TEST_ANDROID_SDK
  def project

  def "setup"() {
    project = ProjectBuilder.builder().build()

    // Set mock test sdk, we only need to test the plugins tasks
    SdkHandler.sTestSdkFolder = project.file(TEST_ANDROID_SDK)
  }

  def "unsupported project project"() {
    when:
    new LicensePlugin().apply project // project.apply plugin: "com.jaredsburrows.license"

    then:
    def e = thrown IllegalStateException
    e.message == "License report plugin can only be applied to android or java projects."
  }

  @Unroll def "#projectPlugin project"() {
    given:
    project.apply plugin: projectPlugin

    when:
    project.apply plugin: "com.jaredsburrows.license"

    then:
    noExceptionThrown()

    where:
    projectPlugin << ["groovy", "java", "com.android.application", "com.android.library", "com.android.test"]
  }

  @Unroll def "#projectPlugin - all tasks created"() {
    given:
    project.apply plugin: projectPlugin
    project.apply plugin: "com.jaredsburrows.license"

    when:
    project.evaluate()

    then:
    project.tasks.getByName "licenseReport"

    where:
    projectPlugin << ["groovy", "java"]
  }

  def "android - all tasks created"() {
    given:
    project.apply plugin: "com.android.application"
    project.apply plugin: "com.jaredsburrows.license"
    project.android {
      compileSdkVersion COMPILE_SDK_VERSION
      buildToolsVersion BUILD_TOOLS_VERSION

      defaultConfig {
        applicationId APPLICATION_ID
      }
    }

    when:
    project.evaluate()

    then:
    project.tasks.getByName "licenseDebugReport"
  }

  def "android [buildTypes] - all tasks created"() {
    given:
    project.apply plugin: "com.android.application"
    project.apply plugin: "com.jaredsburrows.license"
    project.android {
      compileSdkVersion COMPILE_SDK_VERSION
      buildToolsVersion BUILD_TOOLS_VERSION

      defaultConfig {
        applicationId APPLICATION_ID
      }

      buildTypes {
        debug {}
        release {}
      }
    }

    when:
    project.evaluate()

    then:
    project.tasks.getByName "licenseDebugReport"
    project.tasks.getByName "licenseReleaseReport"
  }

  def "android [buildTypes + productFlavors] - all tasks created"() {
    given:
    project.apply plugin: "com.android.application"
    project.apply plugin: "com.jaredsburrows.license"
    project.android {
      compileSdkVersion COMPILE_SDK_VERSION
      buildToolsVersion BUILD_TOOLS_VERSION

      defaultConfig {
        applicationId APPLICATION_ID
      }

      buildTypes {
        debug {}
        release {}
      }

      productFlavors {
        flavor1 {}
        flavor2 {}
      }
    }

    when:
    project.evaluate()

    then:
    project.tasks.getByName "licenseFlavor1DebugReport"
    project.tasks.getByName "licenseFlavor1ReleaseReport"
    project.tasks.getByName "licenseFlavor2DebugReport"
    project.tasks.getByName "licenseFlavor2ReleaseReport"
  }

  def "android [buildTypes + productFlavors + flavorDimensions] - all tasks created"() {
    given:
    project.apply plugin: "com.android.application"
    project.apply plugin: "com.jaredsburrows.license"
    project.android {
      compileSdkVersion COMPILE_SDK_VERSION
      buildToolsVersion BUILD_TOOLS_VERSION

      defaultConfig {
        applicationId APPLICATION_ID
      }

      buildTypes {
        debug {}
        release {}
      }

      flavorDimensions "a", "b"

      productFlavors {
        flavor1 { dimension "a" }
        flavor2 { dimension "a" }
        flavor3 { dimension "b" }
        flavor4 { dimension "b" }
      }
    }

    when:
    project.evaluate()

    then:
    project.tasks.getByName "licenseFlavor1Flavor3DebugReport"
    project.tasks.getByName "licenseFlavor1Flavor3ReleaseReport"
    project.tasks.getByName "licenseFlavor2Flavor4DebugReport"
    project.tasks.getByName "licenseFlavor2Flavor4ReleaseReport"
  }
}
