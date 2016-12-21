package com.jaredsburrows.license

import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

/**
 * @author <a href="mailto:jaredsburrows@gmail.com">Jared Burrows</a>
 */
final class LicensePluginSpec extends Specification {
  def project

  def "setup"() {
    given:
    project = ProjectBuilder.builder().build()
  }

  def "test non android project"() {
    when:
    new LicensePlugin().apply(project)

    then:
    IllegalStateException exception = thrown()
    exception.message == "License report plugin can only be applied to android projects"
  }

  def "test groovy project"() {
    given:
    project.apply plugin: "groovy"

    when:
    new LicensePlugin().apply(project)

    then:
    IllegalStateException exception = thrown()
    exception.message == "License report plugin can only be applied to android projects"
  }

  def "test java project"() {
    given:
    project.apply plugin: "java"

    when:
    new LicensePlugin().apply(project)

    then:
    IllegalStateException exception = thrown()
    exception.message == "License report plugin can only be applied to android projects"
  }

  def "test application project"() {
    given:
    project.apply plugin: "com.android.application"

    when:
    new LicensePlugin().apply(project)

    then:
    noExceptionThrown()
  }

  def "test library project"() {
    given:
    project.apply plugin: "com.android.library"

    when:
    new LicensePlugin().apply(project)

    then:
    noExceptionThrown()
  }

  def "test default all tasks created"() {
    given:
    project.apply plugin: "com.android.application"
    project.android {
      compileSdkVersion 25
      buildToolsVersion "25.0.2"

      defaultConfig {
        applicationId "com.example"
      }
    }

    when:
    project.evaluate()
    new LicensePlugin().apply(project)

    then:
    project.tasks.getByName("licenseDebugReport")
  }

  def "test buildTypes all tasks created"() {
    given:
    project.apply plugin: "com.android.application"
    project.android {
      compileSdkVersion 25
      buildToolsVersion "25.0.2"

      defaultConfig {
        applicationId "com.example"
      }

      buildTypes {
        debug {}
        release {}
      }
    }

    when:
    project.evaluate()
    new LicensePlugin().apply(project)

    then:
    project.tasks.getByName("licenseDebugReport")
    project.tasks.getByName("licenseReleaseReport")
  }

  def "test buildTypes productFlavors all tasks created"() {
    given:
    project.apply plugin: "com.android.application"
    project.android {
      compileSdkVersion 25
      buildToolsVersion "25.0.2"

      defaultConfig {
        applicationId "com.example"
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
    new LicensePlugin().apply(project)

    then:
    project.tasks.getByName("licenseFlavor1DebugReport")
    project.tasks.getByName("licenseFlavor1ReleaseReport")
    project.tasks.getByName("licenseFlavor2DebugReport")
    project.tasks.getByName("licenseFlavor2ReleaseReport")
  }

  def "test buildTypes productFlavors flavorDimensions all tasks created"() {
    given:
    project.apply plugin: "com.android.application"
    project.android {
      compileSdkVersion 25
      buildToolsVersion "25.0.2"

      defaultConfig {
        applicationId "com.example"
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
    new LicensePlugin().apply(project)

    then:
    project.tasks.getByName("licenseFlavor1Flavor3DebugReport")
    project.tasks.getByName("licenseFlavor1Flavor3ReleaseReport")
    project.tasks.getByName("licenseFlavor2Flavor4DebugReport")
    project.tasks.getByName("licenseFlavor2Flavor4ReleaseReport")
  }
}
