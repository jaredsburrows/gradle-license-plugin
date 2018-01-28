package com.jaredsburrows.license

import spock.lang.Unroll
import test.BaseAndroidSpecification

final class LicensePluginAndroidSpec extends BaseAndroidSpecification {
  @Unroll def "android - #projectPlugin project"() {
    given:
    project.apply plugin: projectPlugin

    when:
    new LicensePlugin().apply(project)

    then:
    noExceptionThrown()

    where:
    projectPlugin << LicensePlugin.ANDROID_PLUGINS
  }

  def "android - all tasks created"() {
    given:
    project.apply plugin: "com.android.application"
    new LicensePlugin().apply(project)
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
    project.tasks.getByName("licenseDebugReport")
  }

  def "android - [buildTypes] - all tasks created"() {
    given:
    project.apply plugin: "com.android.application"
    new LicensePlugin().apply(project)
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
    project.tasks.getByName("licenseDebugReport")
    project.tasks.getByName("licenseReleaseReport")
  }

  def "android - [buildTypes + productFlavors + flavorDimensions] - all tasks created"() {
    given:
    project.apply plugin: "com.android.application"
    new LicensePlugin().apply(project)
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
    project.tasks.getByName("licenseFlavor1Flavor3DebugReport")
    project.tasks.getByName("licenseFlavor1Flavor3ReleaseReport")
    project.tasks.getByName("licenseFlavor2Flavor4DebugReport")
    project.tasks.getByName("licenseFlavor2Flavor4ReleaseReport")
  }
}
