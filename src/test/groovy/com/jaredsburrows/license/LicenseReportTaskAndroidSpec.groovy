package com.jaredsburrows.license

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import spock.lang.Unroll

@Deprecated // TODO migrate to LicensePluginJavaSpec
final class LicenseReportTaskAndroidSpec extends Specification {
  @Rule TemporaryFolder testProjectDir = new TemporaryFolder()
  private String reportFolder
  private Project project
  private Project subproject
  def MANIFEST_FILE_PATH = 'src/main/AndroidManifest.xml'
  def MANIFEST = "<manifest xmlns:android=\"http://schemas.android.com/apk/res/android\" package=\"com.example\"/>"

  def 'setup'() {
    reportFolder = "${testProjectDir.root.path}/build/reports/licenses"

    project = ProjectBuilder.builder()
      .withProjectDir(testProjectDir.root)
      .withName('project')
      .build()
    project.repositories {
      maven { url getClass().getResource('/maven').toURI() }
    }

    subproject = ProjectBuilder.builder()
      .withParent(project)
      .withName('subproject')
      .build()
    subproject.repositories {
      maven { url getClass().getResource('/maven').toURI() }
    }

    testProjectDir.newFolder('src', 'main')
    testProjectDir.newFile(MANIFEST_FILE_PATH) << MANIFEST
  }

  // TODO migrate to LicensePluginJavaSpec
  @Unroll def '#taskName with reports enabled and copy enabled #copyEnabled'() {
    given:
    project.apply plugin: 'com.android.application'
    new LicensePlugin().apply(project)
    project.android {
      compileSdkVersion 28

      defaultConfig {
        applicationId 'com.example'
      }
    }
    project.dependencies {
      // Handles duplicates
      implementation 'com.android.support:appcompat-v7:26.1.0'
      implementation 'com.android.support:appcompat-v7:26.1.0'
      implementation 'com.android.support:design:26.1.0'
    }
    project.licenseReport {
      generateHtmlReport = true
      generateJsonReport = true
      copyHtmlReportToAssets = copyEnabled
      copyJsonReportToAssets = copyEnabled
    }

    when:
    project.evaluate()
    LicenseReportTask task = project.tasks.getByName(taskName)
    task.licenseReport()

    def actualHtmlFileExists = task.htmlFile.exists()
    def expectedHtmlFileExists = true

    def actualJsonFileExists = task.jsonFile.exists()
    def expectedJsonFileExists = true

    def assetsFiles = task.assetDirs[0].listFiles()
    def actualAssetsDirectoryContainsFiles = assetsFiles == null ? false : assetsFiles.length != 0
    def expectedAssetsDirectoryContainsFiles = copyEnabled

    then:
    actualHtmlFileExists == expectedHtmlFileExists
    actualJsonFileExists == expectedJsonFileExists
    actualAssetsDirectoryContainsFiles == expectedAssetsDirectoryContainsFiles

    where:
    taskName << ['licenseDebugReport', 'licenseReleaseReport']
    copyEnabled << [true, false]
  }

  // TODO migrate to LicensePluginJavaSpec
  @Unroll def '#taskName with reports disabled and copy enabled #copyEnabled'() {
    given:
    project.apply plugin: 'com.android.application'
    new LicensePlugin().apply(project)
    project.android {
      compileSdkVersion 28

      defaultConfig {
        applicationId 'com.example'
      }
    }
    project.dependencies {
      // Handles duplicates
      implementation 'com.android.support:appcompat-v7:26.1.0'
      implementation 'com.android.support:appcompat-v7:26.1.0'
      implementation 'com.android.support:design:26.1.0'
    }
    project.licenseReport {
      generateHtmlReport = false
      generateJsonReport = false
      copyHtmlReportToAssets = copyEnabled
      copyJsonReportToAssets = copyEnabled
    }

    when:
    project.evaluate()
    LicenseReportTask task = project.tasks.getByName(taskName)
    task.licenseReport()

    def actualHtmlFileExists = task.htmlFile.exists()
    def expectedHtmlFileExists = false

    def actualJsonFileExists = task.jsonFile.exists()
    def expectedJsonFileExists = false

    def assetsFiles = task.assetDirs[0].listFiles()
    def actualAssetsDirectoryContainsFiles = assetsFiles == null ? false : assetsFiles.length != 0
    def expectedAssetsDirectoryContainsFiles = false

    then:
    actualHtmlFileExists == expectedHtmlFileExists
    actualJsonFileExists == expectedJsonFileExists
    actualAssetsDirectoryContainsFiles == expectedAssetsDirectoryContainsFiles

    where:
    taskName << ['licenseDebugReport', 'licenseReleaseReport']
    copyEnabled << [true, false]
  }
}
