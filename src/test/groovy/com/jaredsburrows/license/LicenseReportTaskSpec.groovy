package com.jaredsburrows.license

import com.android.build.gradle.internal.SdkHandler
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification
import spock.lang.Unroll

/**
 * @author <a href="mailto:jaredsburrows@gmail.com">Jared Burrows</a>
 */
final class LicenseReportTaskSpec extends Specification {
  final static COMPILE_SDK_VERSION = 25
  final static BUILD_TOOLS_VERSION = "25.0.2"
  final static APPLICATION_ID = "com.example"
  final static SUPPORT_VERSION = "25.1.0"
  // Maven repo - "file://${System.env.ANDROID_HOME}/extras/android/m2repository"
  final static APPCOMPAT_V7 = "com.android.support:appcompat-v7:$SUPPORT_VERSION"
  final static DESIGN = "com.android.support:design:$SUPPORT_VERSION"
  final static SUPPORT_ANNOTATIONS = "com.android.support:support-annotations:$SUPPORT_VERSION"
  final static SUPPORT_V4 = "com.android.support:support-v4:$SUPPORT_VERSION"
  // Maven repo - "file://${System.env.ANDROID_HOME}/extras/google/m2repository"
  final static FIREBASE_CORE = "com.google.firebase:firebase-core:10.0.1"
  // Others
  final static ANDROID_GIF_DRAWABLE = "pl.droidsonroids.gif:android-gif-drawable:1.2.3"
  final static FAKE_DEPENDENCY = "group:name:1.0.0" // Single license
  final static FAKE_DEPENDENCY2 = "group:name2:1.0.0" // Multiple license
  final static FAKE_DEPENDENCY3 = "group:name3:1.0.0" // Bad license
  // Test fixture that emulates a mavenCentral()/jcenter()/"https://plugins.gradle.org/m2/"
  final static TEST_MAVEN_REPOSITORY = getClass().getResource("/maven/").toURI()
  // Test fixture that emulates a local android sdk
  final static TEST_ANDROID_SDK = getClass().getResource("/android-sdk/").toURI()
  // Projects
  def project
  def subproject

  def "setup"() {
    // Configure test projects
    project = ProjectBuilder.builder()
      .withName("project")
      .build()
    project.repositories {
      maven { url TEST_MAVEN_REPOSITORY }
    }
    subproject = ProjectBuilder.builder()
      .withParent(project)
      .withName("subproject")
      .build()
    subproject.repositories {
      maven { url TEST_MAVEN_REPOSITORY }
    }

    // Set mock test sdk, we only need to test the plugins tasks
    SdkHandler.sTestSdkFolder = project.file TEST_ANDROID_SDK
  }

  @Unroll "#projectPlugin licenseReport - no dependencies"() {
    given:
    project.apply plugin: projectPlugin
    project.apply plugin: "com.jaredsburrows.license"

    when:
    project.evaluate()
    LicenseReportTask task = project.tasks.getByName("licenseReport")
    task.execute()

    def actualHtml = task.htmlFile.text.trim()
    def expectedHtml =
      """
<html>
  <head>
    <style>body{font-family: sans-serif} pre{background-color: #eeeeee; padding: 1em; white-space: pre-wrap}</style>
    <title>Open source licenses</title>
  </head>
  <body>
    <h3>No open source libraries</h3>
  </body>
</html>
""".trim()
    def actualJson = task.jsonFile.text.trim()
    def expectedJson =
      """
[]
""".trim()

    then:
    actualHtml == expectedHtml
    actualJson == expectedJson

    where:
    projectPlugin << ["groovy", "java"]
  }

  @Unroll "#projectPlugin licenseReport - project dependencies"() {
    given:
    project.apply plugin: projectPlugin
    project.apply plugin: "com.jaredsburrows.license"
    project.dependencies {
      compile APPCOMPAT_V7
      compile project.project(":subproject")
    }

    subproject.apply plugin: "java"
    subproject.dependencies {
      compile DESIGN
    }

    when:
    project.evaluate()
    LicenseReportTask task = project.tasks.getByName("licenseReport")
    task.execute()

    def actualHtml = task.htmlFile.text.trim()
    def expectedHtml =
      """
<html>
  <head>
    <style>body{font-family: sans-serif} pre{background-color: #eeeeee; padding: 1em; white-space: pre-wrap}</style>
    <title>Open source licenses</title>
  </head>
  <body>
    <h3>Notice for libraries:</h3>
    <ul>
      <li>
        <a href='#1288288048'>Appcompat-v7</a>
      </li>
      <li>
        <a href='#1288288048'>Design</a>
      </li>
    </ul>
    <a name='1288288048' />
    <h3>The Apache Software License</h3>
    <pre>The Apache Software License, http://www.apache.org/licenses/LICENSE-2.0.txt</pre>
  </body>
</html>
""".trim()
    def actualJson = task.jsonFile.text.trim()
    def expectedJson =
      """
[
    {
        "project": "Appcompat-v7",
        "developers": null,
        "url": null,
        "year": null,
        "license": "The Apache Software License",
        "license_url": "http://www.apache.org/licenses/LICENSE-2.0.txt"
    },
    {
        "project": "Design",
        "developers": null,
        "url": null,
        "year": null,
        "license": "The Apache Software License",
        "license_url": "http://www.apache.org/licenses/LICENSE-2.0.txt"
    }
]
""".trim()

    then:
    actualHtml == expectedHtml
    actualJson == expectedJson

    where:
    projectPlugin << ["groovy", "java"]
  }

  @Unroll "android #taskName - no dependencies"() {
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
    LicenseReportTask task = project.tasks.getByName(taskName)
    task.execute()

    def actualHtml = task.htmlFile.text.trim()
    def expectedHtml =
      """
<html>
  <head>
    <style>body{font-family: sans-serif} pre{background-color: #eeeeee; padding: 1em; white-space: pre-wrap}</style>
    <title>Open source licenses</title>
  </head>
  <body>
    <h3>No open source libraries</h3>
  </body>
</html>
""".trim()
    def actualJson = task.jsonFile.text.trim()
    def expectedJson =
      """
[]
""".trim()

    then:
    actualHtml == expectedHtml
    actualJson == expectedJson

    where:
    taskName << ["licenseDebugReport", "licenseReleaseReport"]
  }

  @Unroll "#projectPlugin licenseReport - no open source dependencies"() {
    given:
    project.apply plugin: projectPlugin
    project.apply plugin: "com.jaredsburrows.license"
    project.dependencies {
      compile FIREBASE_CORE
    }

    when:
    project.evaluate()
    LicenseReportTask task = project.tasks.getByName("licenseReport")
    task.execute()

    def actualHtml = task.htmlFile.text.trim()
    def expectedHtml =
      """
<html>
  <head>
    <style>body{font-family: sans-serif} pre{background-color: #eeeeee; padding: 1em; white-space: pre-wrap}</style>
    <title>Open source licenses</title>
  </head>
  <body>
    <h3>No open source libraries</h3>
  </body>
</html>
""".trim()
    def actualJson = task.jsonFile.text.trim()
    def expectedJson =
      """
[]
""".trim()

    then:
    actualHtml == expectedHtml
    actualJson == expectedJson

    where:
    projectPlugin << ["groovy", "java"]
  }

  @Unroll "android #taskName - no open source dependencies"() {
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
    project.dependencies {
      compile FIREBASE_CORE
    }

    when:
    project.evaluate()
    LicenseReportTask task = project.tasks.getByName(taskName)
    task.execute()

    def actualHtml = task.htmlFile.text.trim()
    def expectedHtml =
      """
<html>
  <head>
    <style>body{font-family: sans-serif} pre{background-color: #eeeeee; padding: 1em; white-space: pre-wrap}</style>
    <title>Open source licenses</title>
  </head>
  <body>
    <h3>No open source libraries</h3>
  </body>
</html>
""".trim()
    def actualJson = task.jsonFile.text.trim()
    def expectedJson =
      """
[]
""".trim()

    then:
    actualHtml == expectedHtml
    actualJson == expectedJson

    where:
    taskName << ["licenseDebugReport", "licenseReleaseReport"]
  }

  @Unroll "#projectPlugin licenseReport"() {
    given:
    project.apply plugin: projectPlugin
    project.apply plugin: "com.jaredsburrows.license"
    project.dependencies {
      // Handles duplicates
      compile APPCOMPAT_V7
      compile APPCOMPAT_V7
      compile DESIGN
    }

    when:
    project.evaluate()
    LicenseReportTask task = project.tasks.getByName("licenseReport")
    task.execute()

    def actualHtml = task.htmlFile.text.trim()
    def expectedHtml =
      """
<html>
  <head>
    <style>body{font-family: sans-serif} pre{background-color: #eeeeee; padding: 1em; white-space: pre-wrap}</style>
    <title>Open source licenses</title>
  </head>
  <body>
    <h3>Notice for libraries:</h3>
    <ul>
      <li>
        <a href='#1288288048'>Appcompat-v7</a>
      </li>
      <li>
        <a href='#1288288048'>Design</a>
      </li>
    </ul>
    <a name='1288288048' />
    <h3>The Apache Software License</h3>
    <pre>The Apache Software License, http://www.apache.org/licenses/LICENSE-2.0.txt</pre>
  </body>
</html>
""".trim()
    def actualJson = task.jsonFile.text.trim()
    def expectedJson =
      """
[
    {
        "project": "Appcompat-v7",
        "developers": null,
        "url": null,
        "year": null,
        "license": "The Apache Software License",
        "license_url": "http://www.apache.org/licenses/LICENSE-2.0.txt"
    },
    {
        "project": "Design",
        "developers": null,
        "url": null,
        "year": null,
        "license": "The Apache Software License",
        "license_url": "http://www.apache.org/licenses/LICENSE-2.0.txt"
    }
]
""".trim()

    then:
    actualHtml == expectedHtml
    actualJson == expectedJson

    where:
    projectPlugin << ["groovy", "java"]
  }

  @Unroll "android #taskName - default buildTypes"() {
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

    def actualHtml = task.htmlFile.text.trim()
    def expectedHtml =
      """
<html>
  <head>
    <style>body{font-family: sans-serif} pre{background-color: #eeeeee; padding: 1em; white-space: pre-wrap}</style>
    <title>Open source licenses</title>
  </head>
  <body>
    <h3>Notice for libraries:</h3>
    <ul>
      <li>
        <a href='#1288288048'>Appcompat-v7</a>
      </li>
      <li>
        <a href='#1288288048'>Design</a>
      </li>
    </ul>
    <a name='1288288048' />
    <h3>The Apache Software License</h3>
    <pre>The Apache Software License, http://www.apache.org/licenses/LICENSE-2.0.txt</pre>
  </body>
</html>
""".trim()
    def actualJson = task.jsonFile.text.trim()
    def expectedJson =
      """
[
    {
        "project": "Appcompat-v7",
        "developers": null,
        "url": null,
        "year": null,
        "license": "The Apache Software License",
        "license_url": "http://www.apache.org/licenses/LICENSE-2.0.txt"
    },
    {
        "project": "Design",
        "developers": null,
        "url": null,
        "year": null,
        "license": "The Apache Software License",
        "license_url": "http://www.apache.org/licenses/LICENSE-2.0.txt"
    }
]
""".trim()

    then:
    actualHtml == expectedHtml
    actualJson == expectedJson

    where:
    taskName << ["licenseDebugReport", "licenseReleaseReport"]
  }

  @Unroll "android #taskName - buildTypes"() {
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
    project.dependencies {
      compile APPCOMPAT_V7

      debugCompile DESIGN
      releaseCompile DESIGN
    }

    when:
    project.evaluate()
    LicenseReportTask task = project.tasks.getByName(taskName)
    task.execute()

    def actualHtml = task.htmlFile.text.trim()
    def expectedHtml =
      """
<html>
  <head>
    <style>body{font-family: sans-serif} pre{background-color: #eeeeee; padding: 1em; white-space: pre-wrap}</style>
    <title>Open source licenses</title>
  </head>
  <body>
    <h3>Notice for libraries:</h3>
    <ul>
      <li>
        <a href='#1288288048'>Appcompat-v7</a>
      </li>
      <li>
        <a href='#1288288048'>Design</a>
      </li>
    </ul>
    <a name='1288288048' />
    <h3>The Apache Software License</h3>
    <pre>The Apache Software License, http://www.apache.org/licenses/LICENSE-2.0.txt</pre>
  </body>
</html>
""".trim()
    def actualJson = task.jsonFile.text.trim()
    def expectedJson =
      """
[
    {
        "project": "Appcompat-v7",
        "developers": null,
        "url": null,
        "year": null,
        "license": "The Apache Software License",
        "license_url": "http://www.apache.org/licenses/LICENSE-2.0.txt"
    },
    {
        "project": "Design",
        "developers": null,
        "url": null,
        "year": null,
        "license": "The Apache Software License",
        "license_url": "http://www.apache.org/licenses/LICENSE-2.0.txt"
    }
]
""".trim()

    then:
    actualHtml == expectedHtml
    actualJson == expectedJson

    where:
    taskName << ["licenseDebugReport", "licenseReleaseReport"]
  }

  @Unroll "android #taskName - buildTypes + productFlavors"() {
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
    project.dependencies {
      compile APPCOMPAT_V7

      debugCompile DESIGN
      releaseCompile DESIGN

      flavor1Compile SUPPORT_V4
      flavor2Compile SUPPORT_V4
    }

    when:
    project.evaluate()
    LicenseReportTask task = project.tasks.getByName(taskName)
    task.execute()

    def actualHtml = task.htmlFile.text.trim()
    def expectedHtml =
      """
<html>
  <head>
    <style>body{font-family: sans-serif} pre{background-color: #eeeeee; padding: 1em; white-space: pre-wrap}</style>
    <title>Open source licenses</title>
  </head>
  <body>
    <h3>Notice for libraries:</h3>
    <ul>
      <li>
        <a href='#1288288048'>Appcompat-v7</a>
      </li>
      <li>
        <a href='#1288288048'>Design</a>
      </li>
      <li>
        <a href='#1288288048'>Support-v4</a>
      </li>
    </ul>
    <a name='1288288048' />
    <h3>The Apache Software License</h3>
    <pre>The Apache Software License, http://www.apache.org/licenses/LICENSE-2.0.txt</pre>
  </body>
</html>
""".trim()
    def actualJson = task.jsonFile.text.trim()
    def expectedJson =
      """
[
    {
        "project": "Appcompat-v7",
        "developers": null,
        "url": null,
        "year": null,
        "license": "The Apache Software License",
        "license_url": "http://www.apache.org/licenses/LICENSE-2.0.txt"
    },
    {
        "project": "Design",
        "developers": null,
        "url": null,
        "year": null,
        "license": "The Apache Software License",
        "license_url": "http://www.apache.org/licenses/LICENSE-2.0.txt"
    },
    {
        "project": "Support-v4",
        "developers": null,
        "url": null,
        "year": null,
        "license": "The Apache Software License",
        "license_url": "http://www.apache.org/licenses/LICENSE-2.0.txt"
    }
]
""".trim()

    then:
    actualHtml == expectedHtml
    actualJson == expectedJson

    where:
    taskName << ["licenseFlavor1DebugReport", "licenseFlavor2DebugReport",
                 "licenseFlavor1ReleaseReport", "licenseFlavor2ReleaseReport"]
  }

  @Unroll "android #taskName - buildTypes + productFlavors + flavorDimensions"() {
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

    def actualHtml = task.htmlFile.text.trim()
    def expectedHtml =
      """
<html>
  <head>
    <style>body{font-family: sans-serif} pre{background-color: #eeeeee; padding: 1em; white-space: pre-wrap}</style>
    <title>Open source licenses</title>
  </head>
  <body>
    <h3>Notice for libraries:</h3>
    <ul>
      <li>
        <a href='#1288288048'>Appcompat-v7</a>
      </li>
      <li>
        <a href='#1288288048'>Design</a>
      </li>
      <li>
        <a href='#1288288048'>Support-annotations</a>
      </li>
      <li>
        <a href='#1288288048'>Support-v4</a>
      </li>
    </ul>
    <a name='1288288048' />
    <h3>The Apache Software License</h3>
    <pre>The Apache Software License, http://www.apache.org/licenses/LICENSE-2.0.txt</pre>
  </body>
</html>
""".trim()
    def actualJson = task.jsonFile.text.trim()
    def expectedJson =
      """
[
    {
        "project": "Appcompat-v7",
        "developers": null,
        "url": null,
        "year": null,
        "license": "The Apache Software License",
        "license_url": "http://www.apache.org/licenses/LICENSE-2.0.txt"
    },
    {
        "project": "Design",
        "developers": null,
        "url": null,
        "year": null,
        "license": "The Apache Software License",
        "license_url": "http://www.apache.org/licenses/LICENSE-2.0.txt"
    },
    {
        "project": "Support-annotations",
        "developers": null,
        "url": null,
        "year": null,
        "license": "The Apache Software License",
        "license_url": "http://www.apache.org/licenses/LICENSE-2.0.txt"
    },
    {
        "project": "Support-v4",
        "developers": null,
        "url": null,
        "year": null,
        "license": "The Apache Software License",
        "license_url": "http://www.apache.org/licenses/LICENSE-2.0.txt"
    }
]
""".trim()

    then:
    actualHtml == expectedHtml
    actualJson == expectedJson

    where:
    taskName << ["licenseFlavor1Flavor3DebugReport", "licenseFlavor1Flavor3ReleaseReport",
                 "licenseFlavor2Flavor4DebugReport", "licenseFlavor2Flavor4ReleaseReport"]
  }

  def "dependency with full pom - project name, developers, url, year, bad license"() {
    given:
    project.apply plugin: "java"
    project.apply plugin: "com.jaredsburrows.license"
    project.dependencies {
      compile FAKE_DEPENDENCY3
    }

    when:
    project.evaluate()
    LicenseReportTask task = project.tasks.getByName("licenseReport")
    task.execute()

    def actualHtml = task.htmlFile.text.trim()
    def expectedHtml =
      """
<html>
  <head>
    <style>body{font-family: sans-serif} pre{background-color: #eeeeee; padding: 1em; white-space: pre-wrap}</style>
    <title>Open source licenses</title>
  </head>
  <body>
    <h3>No open source libraries</h3>
  </body>
</html>
""".trim()
    def actualJson = task.jsonFile.text.trim()
    def expectedJson =
      """
[]
""".trim()

    then:
    actualHtml == expectedHtml
    actualJson == expectedJson
  }

  def "dependency with full pom - project name, developers, url, year, single license"() {
    given:
    project.apply plugin: "java"
    project.apply plugin: "com.jaredsburrows.license"
    project.dependencies {
      compile FAKE_DEPENDENCY
    }

    when:
    project.evaluate()
    LicenseReportTask task = project.tasks.getByName("licenseReport")
    task.execute()

    def actualHtml = task.htmlFile.text.trim()
    def expectedHtml =
      """
<html>
  <head>
    <style>body{font-family: sans-serif} pre{background-color: #eeeeee; padding: 1em; white-space: pre-wrap}</style>
    <title>Open source licenses</title>
  </head>
  <body>
    <h3>Notice for libraries:</h3>
    <ul>
      <li>
        <a href='#755502249'>Fake dependency name</a>
      </li>
    </ul>
    <a name='755502249' />
    <h3>Some license</h3>
    <pre>Some license, http://website.tld/</pre>
  </body>
</html>
""".trim()
    def actualJson = task.jsonFile.text.trim()
    def expectedJson =
      """
[
    {
        "project": "Fake dependency name",
        "developers": "name",
        "url": "https://github.com/user/repo.git",
        "year": "2017",
        "license": "Some license",
        "license_url": "http://website.tld/"
    }
]
""".trim()

    then:
    actualHtml == expectedHtml
    actualJson == expectedJson
  }

  def "dependency with full pom - project name, multiple developers, url, year, multiple licenses"() {
    given:
    project.apply plugin: "java"
    project.apply plugin: "com.jaredsburrows.license"
    project.dependencies {
      compile FAKE_DEPENDENCY2
    }

    when:
    project.evaluate()
    LicenseReportTask task = project.tasks.getByName("licenseReport")
    task.execute()

    def actualHtml = task.htmlFile.text.trim()
    def expectedHtml =
      """
<html>
  <head>
    <style>body{font-family: sans-serif} pre{background-color: #eeeeee; padding: 1em; white-space: pre-wrap}</style>
    <title>Open source licenses</title>
  </head>
  <body>
    <h3>Notice for libraries:</h3>
    <ul>
      <li>
        <a href='#755502249'>Fake dependency name</a>
      </li>
    </ul>
    <a name='755502249' />
    <h3>Some license</h3>
    <pre>Some license, http://website.tld/</pre>
  </body>
</html>
""".trim()
    def actualJson = task.jsonFile.text.trim()
    def expectedJson =
      """
[
    {
        "project": "Fake dependency name",
        "developers": "name",
        "url": "https://github.com/user/repo.git",
        "year": "2017",
        "license": "Some license",
        "license_url": "http://website.tld/"
    }
]
""".trim()

    then:
    actualHtml == expectedHtml
    actualJson == expectedJson
  }

  @Unroll "readme example - #taskName"() {
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

    def actualHtml = task.htmlFile.text.trim()
    def expectedHtml =
      """
<html>
  <head>
    <style>body{font-family: sans-serif} pre{background-color: #eeeeee; padding: 1em; white-space: pre-wrap}</style>
    <title>Open source licenses</title>
  </head>
  <body>
    <h3>Notice for libraries:</h3>
    <ul>
      <li>
        <a href='#-989311426'>Android GIF Drawable Library</a>
      </li>
      <li>
        <a href='#1288288048'>Design</a>
      </li>
    </ul>
    <a name='-989311426' />
    <h3>The MIT License</h3>
    <pre>The MIT License, http://opensource.org/licenses/MIT</pre>
    <a name='1288288048' />
    <h3>The Apache Software License</h3>
    <pre>The Apache Software License, http://www.apache.org/licenses/LICENSE-2.0.txt</pre>
  </body>
</html>
""".trim()
    def actualJson = task.jsonFile.text.trim()
    def expectedJson =
      """
[
    {
        "project": "Android GIF Drawable Library",
        "developers": "Karol Wr\\u00c3\\u00b3tniak",
        "url": "https://github.com/koral--/android-gif-drawable.git",
        "year": null,
        "license": "The MIT License",
        "license_url": "http://opensource.org/licenses/MIT"
    },
    {
        "project": "Design",
        "developers": null,
        "url": null,
        "year": null,
        "license": "The Apache Software License",
        "license_url": "http://www.apache.org/licenses/LICENSE-2.0.txt"
    }
]
""".trim()

    then:
    actualHtml == expectedHtml
    actualJson == expectedJson

    where:
    taskName << ["licenseDebugReport", "licenseReleaseReport"]
  }
}
