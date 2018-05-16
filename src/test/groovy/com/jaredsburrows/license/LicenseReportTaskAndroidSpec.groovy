package com.jaredsburrows.license

import org.gradle.testkit.runner.GradleRunner
import spock.lang.Unroll
import test.BaseAndroidSpecification

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

final class LicenseReportTaskAndroidSpec extends BaseAndroidSpecification {
  @Unroll def "android project running #taskName that has no dependencies"() {
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

    def expectedHtml =
      """
<html>
  <head>
    <style>body { font-family: sans-serif } pre { background-color: #eeeeee; padding: 1em; white-space: pre-wrap; display: inline-block }</style>
    <title>Open source licenses</title>
  </head>
  <body>
    <h3>None</h3>
  </body>
</html>
""".stripIndent().trim()
    def expectedJson =
      """
[]
""".stripIndent().trim()

    when:
    def result = GradleRunner.create()
      .withProjectDir(testProjectDir.root)
      .withArguments("${taskName}")
      .build()

    then:
    result.task(":${taskName}").outcome == SUCCESS
    result.output.find("Wrote HTML report to file:///.*/build/reports/licenses/${taskName}.html.")
    result.output.find("Wrote JSON report to file:///.*/build/reports/licenses/${taskName}.json.")

    def actualHtml = new File(new URI(result.output.find("file:///.*/build/reports/licenses/${taskName}.html")).path).text.stripIndent().trim()
    def actualJson = new File(new URI(result.output.find("file:///.*/build/reports/licenses/${taskName}.json")).path).text.stripIndent().trim()

    actualHtml == expectedHtml
    actualJson == expectedJson

    where:
    taskName << ["licenseDebugReport", "licenseReleaseReport"]
  }

  @Unroll def "android project #taskName with no open source dependencies"() {
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
    project.dependencies {
      compile FIREBASE_CORE
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
        "dependency_string": "com.google.firebase:firebase-core:10.0.1"
    }
]
""".stripIndent().trim()

    then:
    actualHtml == expectedHtml
    actualJson == expectedJson

    where:
    taskName << ["licenseDebugReport", "licenseReleaseReport"]
  }

  @Unroll def "android project running #taskName with default buildTypes"() {
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
    project.dependencies {
      // Handles duplicates
      compile APPCOMPAT_V7
      compile APPCOMPAT_V7
      compile DESIGN
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
        <a href='#1288284111'>Appcompat-v7</a>
      </li>
      <li>
        <a href='#1288284111'>Design</a>
      </li>
      <a name='1288284111' />
      <pre>${getLicenseText("apache-2.0.txt")}</pre>
    </ul>
  </body>
</html>
""".stripIndent().trim()
    def actualJson = task.jsonFile.text.stripIndent().trim()
    def expectedJson =
      """
[
    {
        "project": "Appcompat-v7",
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
        "dependency_string": "com.android.support:appcompat-v7:26.1.0"
    },
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
        "dependency_string": "com.android.support:design:26.1.0"
    }
]
""".stripIndent().trim()

    then:
    actualHtml == expectedHtml
    actualJson == expectedJson

    where:
    taskName << ["licenseDebugReport", "licenseReleaseReport"]
  }

  @Unroll def "android project running #taskName with buildTypes"() {
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
    project.dependencies {
      compile APPCOMPAT_V7

      debugCompile DESIGN
      releaseCompile DESIGN
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
        <a href='#1288284111'>Appcompat-v7</a>
      </li>
      <li>
        <a href='#1288284111'>Design</a>
      </li>
      <a name='1288284111' />
      <pre>${getLicenseText("apache-2.0.txt")}</pre>
    </ul>
  </body>
</html>
""".stripIndent().trim()
    def actualJson = task.jsonFile.text.stripIndent().trim()
    def expectedJson =
      """
[
    {
        "project": "Appcompat-v7",
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
        "dependency_string": "com.android.support:appcompat-v7:26.1.0"
    },
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
        "dependency_string": "com.android.support:design:26.1.0"
    }
]
""".stripIndent().trim()

    then:
    actualHtml == expectedHtml
    actualJson == expectedJson

    where:
    taskName << ["licenseDebugReport", "licenseReleaseReport"]
  }

  @Unroll def "android project running #taskName with buildTypes + productFlavors + flavorDimensions"() {
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
    project.dependencies {
      compile APPCOMPAT_V7

      debugCompile DESIGN
      releaseCompile DESIGN

      flavor1Compile SUPPORT_V4
      flavor2Compile SUPPORT_V4
      flavor3Compile SUPPORT_ANNOTATIONS
      flavor4Compile SUPPORT_ANNOTATIONS
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
        <a href='#1288284111'>Appcompat-v7</a>
      </li>
      <li>
        <a href='#1288284111'>Design</a>
      </li>
      <li>
        <a href='#1288284111'>Support-annotations</a>
      </li>
      <li>
        <a href='#1288284111'>Support-v4</a>
      </li>
      <a name='1288284111' />
      <pre>${getLicenseText("apache-2.0.txt")}</pre>
    </ul>
  </body>
</html>
""".stripIndent().trim()
    def actualJson = task.jsonFile.text.stripIndent().trim()
    def expectedJson =
      """
[
    {
        "project": "Appcompat-v7",
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
        "dependency_string": "com.android.support:appcompat-v7:26.1.0"
    },
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
        "dependency_string": "com.android.support:design:26.1.0"
    },
    {
        "project": "Support-annotations",
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
        "dependency_string": "com.android.support:support-annotations:26.1.0"
    },
    {
        "project": "Support-v4",
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
        "dependency_string": "com.android.support:support-v4:26.1.0"
    }
]
""".stripIndent().trim()

    then:
    actualHtml == expectedHtml
    actualJson == expectedJson

    where:
    taskName << ["licenseFlavor1Flavor3DebugReport", "licenseFlavor1Flavor3ReleaseReport",
                 "licenseFlavor2Flavor4DebugReport", "licenseFlavor2Flavor4ReleaseReport"]
  }

  @Unroll def "android project running #taskName from readme example"() {
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
    project.dependencies {
      debugCompile DESIGN
      debugCompile ANDROID_GIF_DRAWABLE
      releaseCompile DESIGN
      releaseCompile ANDROID_GIF_DRAWABLE
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
        <a href='#-989315363'>Android GIF Drawable Library</a>
      </li>
      <a name='-989315363' />
      <pre>${getLicenseText("mit.txt")}</pre>
      <li>
        <a href='#1288284111'>Design</a>
      </li>
      <a name='1288284111' />
      <pre>${getLicenseText("apache-2.0.txt")}</pre>
    </ul>
  </body>
</html>
""".stripIndent().trim()
    def actualJson = task.jsonFile.text.stripIndent().trim()
    def expectedJson =
      """
[
    {
        "project": "Android GIF Drawable Library",
        "description": "Views and Drawable for displaying animated GIFs for Android",
        "version": "1.2.3",
        "developers": [
            "Karol Wr\\u00c3\\u00b3tniak"
        ],
        "url": "https://github.com/koral--/android-gif-drawable",
        "year": null,
        "licenses": [
            {
                "license": "The MIT License",
                "license_url": "http://opensource.org/licenses/MIT"
            }
        ],
        "dependency_string": "pl.droidsonroids.gif:android-gif-drawable:1.2.3"
    },
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
        "dependency_string": "com.android.support:design:26.1.0"
    }
]
""".stripIndent().trim()

    then:
    actualHtml == expectedHtml
    actualJson == expectedJson

    where:
    taskName << ["licenseDebugReport", "licenseReleaseReport"]
  }

  @Unroll def "android project running #taskName with reports enabled and copy enabled #copyEnabled"() {
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
    project.dependencies {
      // Handles duplicates
      compile APPCOMPAT_V7
      compile APPCOMPAT_V7
      compile DESIGN
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
    taskName << ["licenseDebugReport", "licenseReleaseReport"]
    copyEnabled << [true, false]
  }

  @Unroll def "android project running #taskName with reports disabled and copy enabled #copyEnabled"() {
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
    project.dependencies {
      // Handles duplicates
      compile APPCOMPAT_V7
      compile APPCOMPAT_V7
      compile DESIGN
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
    taskName << ["licenseDebugReport", "licenseReleaseReport"]
    copyEnabled << [true, false]
  }

  @Unroll def "android project running #taskName with default buildTypes, multi module and android and java"() {
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
    project.dependencies {
      compile project.project(":subproject")
      compile FAKE_DEPENDENCY
    }

    subproject.apply plugin: "java-library"
    subproject.dependencies {
      compile DESIGN
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
http://website.tld/</pre>
      <li>
        <a href='#1288284111'>Design</a>
      </li>
      <a name='1288284111' />
      <pre>${getLicenseText("apache-2.0.txt")}</pre>
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
        "dependency_string": "com.android.support:design:26.1.0"
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
        "dependency_string": "group:name:1.0.0"
    }
]
""".stripIndent().trim()

    then:
    actualHtml == expectedHtml
    actualJson == expectedJson

    where:
    taskName << ["licenseDebugReport", "licenseReleaseReport"]
  }
}
