package com.jaredsburrows.license

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Ignore
import spock.lang.Specification
import spock.lang.Unroll
import test.TestUtils

@Deprecated // TODO migrate to LicensePluginJavaSpec
final class LicenseReportTaskAndroidSpec extends Specification {
  @Rule TemporaryFolder testProjectDir = new TemporaryFolder()
  def buildFile
  def pluginClasspath = []
  Project project
  Project subproject
  def MANIFEST_FILE_PATH = 'src/main/AndroidManifest.xml'
  def MANIFEST = "<manifest xmlns:android=\"http://schemas.android.com/apk/res/android\" package=\"com.example\"/>"

  def 'setup'() {
    // Setup project
    project = ProjectBuilder.builder()
      .withProjectDir(testProjectDir.root)
      .withName('project')
      .build()
    project.repositories {
      maven { url getClass().getResource('/maven').toURI() }
    }

    // Setup subproject
    subproject = ProjectBuilder.builder()
      .withParent(project)
      .withName('subproject')
      .build()
    subproject.repositories {
      maven { url getClass().getResource('/maven').toURI() }
    }

    buildFile = testProjectDir.newFile('build.gradle')

    def pluginClasspathResource = getClass().classLoader.findResource('plugin-classpath.txt')
    if (pluginClasspathResource == null) {
      throw new IllegalStateException(
        'Did not find plugin classpath resource, run `testClasses` build task.')
    }

    pluginClasspath = pluginClasspathResource.readLines().collect { new File(it) }

    // Make sure Android projects have a manifest
    testProjectDir.newFolder('src', 'main')
    testProjectDir.newFile(MANIFEST_FILE_PATH) << MANIFEST
  }

  @Ignore('migrate to android sdk')
  @Unroll def 'android project #taskName with no open source dependencies'() {
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
      implementation 'com.google.firebase:firebase-core:10.0.1'
    }

    when:
    project.evaluate()
    LicenseReportTask task = project.tasks.getByName(taskName)
    task.execute()

    def actualHtml = task.htmlFile.text.stripIndent().trim()
    def expectedHtml =
      """
<html>
  <head>
    <style>body { font-family: sans-serif } pre { background-color: #eeeeee; padding: 1em; white-space: pre-wrap; display: inline-block }</style>
    <title>Open source licenses</title>
  </head>
  <body>
    <h3>Notice for packages:</h3>
    <ul>
      <li>
        <a href='#76480'>Firebase-core</a>
      </li>
      <pre>No license found</pre>
    </ul>
  </body>
</html>
""".stripIndent().trim()
    def actualJson = task.jsonFile.text.stripIndent().trim()
    def expectedJson =
      """
[
    {
        "project": "Firebase-core",
        "description": null,
        "version": "10.0.1",
        "developers": [
            
        ],
        "url": null,
        "year": null,
        "licenses": [
            
        ],
        "dependency": "com.google.firebase:firebase-core:10.0.1"
    }
]
""".stripIndent().trim()

    then:
    actualHtml == expectedHtml
    actualJson == expectedJson

    where:
    taskName << ['licenseDebugReport', 'licenseReleaseReport']
  }

  @Unroll def 'android project running #taskName with default buildTypes, multi module and android and java'() {
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
      implementation project.project(':subproject')
      implementation 'group:name:1.0.0'
    }

    subproject.apply plugin: 'java-library'
    subproject.dependencies {
      implementation 'com.android.support:design:26.1.0'
    }

    when:
    project.evaluate()
    LicenseReportTask task = project.tasks.getByName(taskName)
    task.execute()

    def actualHtml = task.htmlFile.text.stripIndent().trim()
    def expectedHtml =
      """
<html>
  <head>
    <style>body { font-family: sans-serif } pre { background-color: #eeeeee; padding: 1em; white-space: pre-wrap; display: inline-block }</style>
    <title>Open source licenses</title>
  </head>
  <body>
    <h3>Notice for packages:</h3>
    <ul>
      <li>
        <a href='#755498312'>Fake dependency name</a>
      </li>
      <pre>Some license
<a href='http://website.tld/'>http://website.tld/</a></pre>
      <li>
        <a href='#1288284111'>Design</a>
      </li>
      <a name='1288284111' />
      <pre>${TestUtils.getLicenseText("apache-2.0.txt")}</pre>
    </ul>
  </body>
</html>
""".stripIndent().trim()
    def actualJson = task.jsonFile.text.stripIndent().trim()
    def expectedJson =
      """
[
    {
        "project": "Design",
        "description": null,
        "version": "26.1.0",
        "developers": [
            
        ],
        "url": null,
        "year": null,
        "licenses": [
            {
                "license": "The Apache Software License",
                "license_url": "http://www.apache.org/licenses/LICENSE-2.0.txt"
            }
        ],
        "dependency": "com.android.support:design:26.1.0"
    },
    {
        "project": "Fake dependency name",
        "description": "Fake dependency description",
        "version": "1.0.0",
        "developers": [
            "name"
        ],
        "url": "https://github.com/user/repo",
        "year": "2017",
        "licenses": [
            {
                "license": "Some license",
                "license_url": "http://website.tld/"
            }
        ],
        "dependency": "group:name:1.0.0"
    }
]
""".stripIndent().trim()

    then:
    actualHtml == expectedHtml
    actualJson == expectedJson

    where:
    taskName << ['licenseDebugReport', 'licenseReleaseReport']
  }

  @Unroll def 'android project running #taskName with reports enabled and copy enabled #copyEnabled'() {
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
    task.execute()

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

  @Unroll def 'android project running #taskName with reports disabled and copy enabled #copyEnabled'() {
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
    task.execute()

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
