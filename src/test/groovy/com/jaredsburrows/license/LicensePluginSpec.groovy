package com.jaredsburrows.license

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
  final LicensePlugin plugin = new LicensePlugin()
  def project

  def "setup"() {
    project = ProjectBuilder.builder().build()
  }

  def "test unsupported project project"() {
    when:
    plugin.apply project

    then:
    def e = thrown(IllegalStateException)
    e.message == "License report plugin can only be applied to android or java projects."
  }

  @Unroll def "test #projectPlugin project"() {
    given:
    project.apply plugin: projectPlugin

    when:
    plugin.apply project

    then:
    notThrown(IllegalStateException)

    where:
    projectPlugin << ["groovy", "java", "com.android.application", "com.android.library", "com.android.test"]
  }

  @Unroll def "test #projectPlugin - all tasks created"() {
    given:
    project.apply plugin: projectPlugin

    when:
    project.evaluate()
    plugin.apply project

    then:
    project.tasks.getByName "licenseReport"

    where:
    projectPlugin << ["groovy", "java"]
  }

  def "test android - all tasks created"() {
    given:
    project.apply plugin: "com.android.application"
    project.android {
      compileSdkVersion COMPILE_SDK_VERSION
      buildToolsVersion BUILD_TOOLS_VERSION

      defaultConfig {
        applicationId APPLICATION_ID
      }
    }

    when:
    project.evaluate()
    plugin.apply project

    then:
    project.tasks.getByName "licenseDebugReport"
  }

  def "test android [buildTypes] - all tasks created"() {
    given:
    project.apply plugin: "com.android.application"
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
    plugin.apply project

    then:
    project.tasks.getByName "licenseDebugReport"
    project.tasks.getByName "licenseReleaseReport"
  }

  def "test android [buildTypes + productFlavors] - all tasks created"() {
    given:
    project.apply plugin: "com.android.application"
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
    plugin.apply project

    then:
    project.tasks.getByName "licenseFlavor1DebugReport"
    project.tasks.getByName "licenseFlavor1ReleaseReport"
    project.tasks.getByName "licenseFlavor2DebugReport"
    project.tasks.getByName "licenseFlavor2ReleaseReport"
  }

  def "test android [buildTypes + productFlavors + flavorDimensions] - all tasks created"() {
    given:
    project.apply plugin: "com.android.application"
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
    plugin.apply project

    then:
    project.tasks.getByName "licenseFlavor1Flavor3DebugReport"
    project.tasks.getByName "licenseFlavor1Flavor3ReleaseReport"
    project.tasks.getByName "licenseFlavor2Flavor4DebugReport"
    project.tasks.getByName "licenseFlavor2Flavor4ReleaseReport"
  }
}
