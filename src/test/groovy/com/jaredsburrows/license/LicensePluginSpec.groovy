package com.jaredsburrows.license

import com.android.build.gradle.internal.SdkHandler
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Unroll
import test.BaseSpecification

/**
 * @author <a href="mailto:jaredsburrows@gmail.com">Jared Burrows</a>
 */
final class LicensePluginSpec extends BaseSpecification {
  def project

  def "setup"() {
    project = ProjectBuilder.builder()
      .withProjectDir(new File("src/test/resources/project"))
      .withName("project")
      .build()

    // Set mock test sdk, we only need to test the plugins tasks
    SdkHandler.sTestSdkFolder = project.file(TEST_ANDROID_SDK)
  }

  def "unsupported project project"() {
    when:
    new LicensePlugin().apply(project) // project.apply plugin: "com.jaredsburrows.license"

    then:
    def e = thrown(IllegalStateException)
    e.message == "License report plugin can only be applied to android or java projects."
  }

  @Unroll "all - #projectPlugin project"() {
    given:
    project.apply plugin: projectPlugin

    when:
    project.apply plugin: "com.jaredsburrows.license"

    then:
    noExceptionThrown()

    where:
    projectPlugin << LicensePlugin.JVM_PLUGINS + LicensePlugin.ANDROID_PLUGINS
  }

  @Unroll "jvm - #projectPlugin - all tasks created"() {
    given:
    project.apply plugin: projectPlugin
    project.apply plugin: "com.jaredsburrows.license"

    when:
    project.evaluate()

    then:
    project.tasks.getByName("licenseReport")

    where:
    projectPlugin << LicensePlugin.JVM_PLUGINS
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
    project.tasks.getByName("licenseDebugReport")
  }

  def "android - [buildTypes] - all tasks created"() {
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
    project.tasks.getByName("licenseDebugReport")
    project.tasks.getByName("licenseReleaseReport")
  }

  def "android - [buildTypes + productFlavors] - all tasks created"() {
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
    project.tasks.getByName("licenseFlavor1DebugReport")
    project.tasks.getByName("licenseFlavor1ReleaseReport")
    project.tasks.getByName("licenseFlavor2DebugReport")
    project.tasks.getByName("licenseFlavor2ReleaseReport")
  }

  def "android - [buildTypes + productFlavors + flavorDimensions] - all tasks created"() {
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
    project.tasks.getByName("licenseFlavor1Flavor3DebugReport")
    project.tasks.getByName("licenseFlavor1Flavor3ReleaseReport")
    project.tasks.getByName("licenseFlavor2Flavor4DebugReport")
    project.tasks.getByName("licenseFlavor2Flavor4ReleaseReport")
  }
}
