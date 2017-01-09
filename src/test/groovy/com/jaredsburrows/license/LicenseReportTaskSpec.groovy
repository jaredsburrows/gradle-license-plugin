package com.jaredsburrows.license

import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification
import spock.lang.Unroll

/**
 * @author <a href="mailto:jaredsburrows@gmail.com">Jared Burrows</a>
 */
final class LicenseReportTaskSpec extends Specification {
  final static def COMPILE_SDK_VERSION = 25
  final static def BUILD_TOOLS_VERSION = "25.0.2"
  final static def APPLICATION_ID = "com.example"
  final static def SUPPORT_VERSION = "25.1.0"
  // Maven repo - "file://${System.env.ANDROID_HOME}/extras/android/m2repository"
  final static def APPCOMPAT_V7 = "com.android.support:appcompat-v7:${SUPPORT_VERSION}"
  final static def DESIGN = "com.android.support:design:${SUPPORT_VERSION}"
  final static def SUPPORT_ANNOTATIONS = "com.android.support:support-annotations:${SUPPORT_VERSION}"
  final static def SUPPORT_V4 = "com.android.support:support-v4:${SUPPORT_VERSION}"
  // Maven repo - "file://${System.env.ANDROID_HOME}/extras/google/m2repository"
  final static def FIREBASE_CORE = "com.google.firebase:firebase-core:10.0.1"
  // Others
  final static def ANDROID_GIF_DRAWABLE = "pl.droidsonroids.gif:android-gif-drawable:1.2.3"
  // Test fixture that emulates a mavenCentral()/jcenter()/"https://plugins.gradle.org/m2/"
  final static def TEST_MAVEN_REPOSITORY = getClass().getResource("/maven/").toURI()
  final LicensePlugin plugin = new LicensePlugin()
  def project
  def assertDir
  def htmlFile
  def jsonFile

  def "setup"() {
    // Common project
    project = ProjectBuilder.builder().build()
    project.repositories {
      maven { url TEST_MAVEN_REPOSITORY }
    }

    // Override output directories
    assertDir = File.createTempDir()
    assertDir.deleteOnExit()
    htmlFile = new File(assertDir.path, "test.html")
    jsonFile = new File(assertDir.path, "test.json")
  }

  @Unroll def "test #projectPlugin licenseReport - no dependencies"() {
    given:
    project.apply plugin: projectPlugin
    project.dependencies {}

    when:
    project.evaluate()
    plugin.apply(project)

    // Change output directory for testing
    final LicenseReportTask task = project.tasks.getByName("licenseReport")
    task.htmlFile = htmlFile
    task.jsonFile = jsonFile
    task.execute()

    def actualHtml = task.htmlFile.text.trim()
    def expectedHtml =
      """
<html>
  <head>
    <style>body{font-family:sans-serif;}pre{background-color:#eee;padding:1em;white-space:pre-wrap;}</style>
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

  @Unroll def "test android #taskName - no dependencies"() {
    given:
    project.apply plugin: "com.android.application"
    project.android {
      compileSdkVersion COMPILE_SDK_VERSION
      buildToolsVersion BUILD_TOOLS_VERSION

      defaultConfig {
        applicationId APPLICATION_ID
      }
    }
    project.dependencies {}

    when:
    project.evaluate()
    plugin.apply(project)

    // Change output directory for testing
    final LicenseReportTask task = project.tasks.getByName(taskName)
    task.assetDirs = [assertDir]
    task.htmlFile = htmlFile
    task.jsonFile = jsonFile
    task.execute()

    def actualHtml = task.htmlFile.text.trim()
    def expectedHtml =
      """
<html>
  <head>
    <style>body{font-family:sans-serif;}pre{background-color:#eee;padding:1em;white-space:pre-wrap;}</style>
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

  @Unroll def "test #projectPlugin licenseReport - no open source dependencies"() {
    given:
    project.apply plugin: projectPlugin
    project.dependencies {
      delegate.compile(FIREBASE_CORE)
    }

    when:
    project.evaluate()
    plugin.apply(project)

    // Change output directory for testing
    final LicenseReportTask task = project.tasks.getByName("licenseReport")
    task.htmlFile = htmlFile
    task.jsonFile = jsonFile
    task.execute()

    def actualHtml = task.htmlFile.text.trim()
    def expectedHtml =
      """
<html>
  <head>
    <style>body{font-family:sans-serif;}pre{background-color:#eee;padding:1em;white-space:pre-wrap;}</style>
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

  @Unroll def "test android #taskName - no open source dependencies"() {
    given:
    project.apply plugin: "com.android.application"
    project.android {
      compileSdkVersion COMPILE_SDK_VERSION
      buildToolsVersion BUILD_TOOLS_VERSION

      defaultConfig {
        applicationId APPLICATION_ID
      }
    }
    project.dependencies {
      delegate.compile FIREBASE_CORE
    }

    when:
    project.evaluate()
    plugin.apply(project)

    // Change output directory for testing
    final LicenseReportTask task = project.tasks.getByName(taskName)
    task.assetDirs = [assertDir]
    task.htmlFile = htmlFile
    task.jsonFile = jsonFile
    task.execute()

    def actualHtml = task.htmlFile.text.trim()
    def expectedHtml =
      """
<html>
  <head>
    <style>body{font-family:sans-serif;}pre{background-color:#eee;padding:1em;white-space:pre-wrap;}</style>
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

  @Unroll def "test #projectPlugin licenseReport"() {
    given:
    project.apply plugin: projectPlugin
    project.dependencies {
      // Handles duplicates
      delegate.compile APPCOMPAT_V7
      delegate.compile APPCOMPAT_V7
      delegate.compile DESIGN
    }

    when:
    project.evaluate()
    plugin.apply(project)

    // Change output directory for testing
    final LicenseReportTask task = project.tasks.getByName("licenseReport")
    task.htmlFile = htmlFile
    task.jsonFile = jsonFile
    task.execute()

    def actualHtml = task.htmlFile.text.trim()
    def expectedHtml =
      """
<html>
  <head>
    <style>body{font-family:sans-serif;}pre{background-color:#eee;padding:1em;white-space:pre-wrap;}</style>
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

  @Unroll def "test android #taskName - default buildTypes"() {
    given:
    project.apply plugin: "com.android.application"
    project.android {
      compileSdkVersion COMPILE_SDK_VERSION
      buildToolsVersion BUILD_TOOLS_VERSION

      defaultConfig {
        applicationId APPLICATION_ID
      }
    }
    project.dependencies {
      // Handles duplicates
      delegate.compile APPCOMPAT_V7
      delegate.compile APPCOMPAT_V7
      delegate.compile DESIGN
    }

    when:
    project.evaluate()
    plugin.apply(project)

    // Change output directory for testing
    final LicenseReportTask task = project.tasks.getByName(taskName)
    task.assetDirs = [assertDir]
    task.htmlFile = htmlFile
    task.jsonFile = jsonFile
    task.execute()

    def actualHtml = task.htmlFile.text.trim()
    def expectedHtml =
      """
<html>
  <head>
    <style>body{font-family:sans-serif;}pre{background-color:#eee;padding:1em;white-space:pre-wrap;}</style>
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


  def "test android licenseDebugReport - buildTypes"() {
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
    project.dependencies {
      delegate.compile APPCOMPAT_V7
      delegate.debugCompile DESIGN
      delegate.releaseCompile SUPPORT_ANNOTATIONS
    }

    when:
    project.evaluate()
    plugin.apply(project)

    // Change output directory for testing
    final LicenseReportTask task = project.tasks.getByName("licenseDebugReport")
    task.assetDirs = [assertDir]
    task.htmlFile = htmlFile
    task.jsonFile = jsonFile
    task.execute()

    def actualHtml = task.htmlFile.text.trim()
    def expectedHtml =
      """
<html>
  <head>
    <style>body{font-family:sans-serif;}pre{background-color:#eee;padding:1em;white-space:pre-wrap;}</style>
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
  }

  def "test android licenseReleaseReport - buildTypes"() {
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
    project.dependencies {
      delegate.compile APPCOMPAT_V7
      delegate.debugCompile SUPPORT_ANNOTATIONS
      delegate.releaseCompile DESIGN
    }

    when:
    project.evaluate()
    plugin.apply(project)

    // Change output directory for testing
    final LicenseReportTask task = project.tasks.getByName("licenseReleaseReport")
    task.assetDirs = [assertDir]
    task.htmlFile = htmlFile
    task.jsonFile = jsonFile
    task.execute()

    def actualHtml = task.htmlFile.text.trim()
    def expectedHtml =
      """
<html>
  <head>
    <style>body{font-family:sans-serif;}pre{background-color:#eee;padding:1em;white-space:pre-wrap;}</style>
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
  }

  def "test android licenseFlavor1DebugReport - buildTypes + productFlavors"() {
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
    project.dependencies {
      delegate.compile APPCOMPAT_V7
      delegate.debugCompile DESIGN
      delegate.flavor1Compile SUPPORT_V4
    }

    when:
    project.evaluate()
    plugin.apply(project)

    // Change output directory for testing
    final LicenseReportTask task = project.tasks.getByName("licenseFlavor1DebugReport")
    task.assetDirs = [assertDir]
    task.htmlFile = htmlFile
    task.jsonFile = jsonFile
    task.execute()

    def actualHtml = task.htmlFile.text.trim()
    def expectedHtml =
      """
<html>
  <head>
    <style>body{font-family:sans-serif;}pre{background-color:#eee;padding:1em;white-space:pre-wrap;}</style>
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
  }

  def "test android licenseFlavor2ReleaseReport - buildTypes + productFlavors"() {
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
    project.dependencies {
      delegate.compile APPCOMPAT_V7
      delegate.releaseCompile DESIGN
      delegate.flavor2Compile SUPPORT_V4
    }

    when:
    project.evaluate()
    plugin.apply(project)

    // Change output directory for testing
    final LicenseReportTask task = project.tasks.getByName("licenseFlavor2ReleaseReport")
    task.assetDirs = [assertDir]
    task.htmlFile = htmlFile
    task.jsonFile = jsonFile
    task.execute()

    def actualHtml = task.htmlFile.text.trim()
    def expectedHtml =
      """
<html>
  <head>
    <style>body{font-family:sans-serif;}pre{background-color:#eee;padding:1em;white-space:pre-wrap;}</style>
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
  }

  def "test android licenseFlavor1Flavor3DebugReport - buildTypes + productFlavors + flavorDimensions"() {
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
    project.dependencies {
      delegate.compile APPCOMPAT_V7
      delegate.debugCompile DESIGN
      delegate.flavor1Compile SUPPORT_V4
      delegate.flavor3Compile SUPPORT_ANNOTATIONS
    }

    when:
    project.evaluate()
    plugin.apply(project)

    // Change output directory for testing
    final LicenseReportTask task = project.tasks.getByName("licenseFlavor1Flavor3DebugReport")
    task.assetDirs = [assertDir]
    task.htmlFile = htmlFile
    task.jsonFile = jsonFile
    task.execute()

    def actualHtml = task.htmlFile.text.trim()
    def expectedHtml =
      """
<html>
  <head>
    <style>body{font-family:sans-serif;}pre{background-color:#eee;padding:1em;white-space:pre-wrap;}</style>
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
  }

  def "test android licenseFlavor2Flavor4ReleaseReport - buildTypes + productFlavors + flavorDimensions"() {
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
    project.dependencies {
      delegate.compile APPCOMPAT_V7
      delegate.releaseCompile DESIGN
      delegate.flavor2Compile SUPPORT_V4
      delegate.flavor4Compile SUPPORT_ANNOTATIONS
    }

    when:
    project.evaluate()
    plugin.apply(project)

    // Change output directory for testing
    final LicenseReportTask task = project.tasks.getByName("licenseFlavor2Flavor4ReleaseReport")
    task.assetDirs = [assertDir]
    task.htmlFile = htmlFile
    task.jsonFile = jsonFile
    task.execute()

    def actualHtml = task.htmlFile.text.trim()
    def expectedHtml =
      """
<html>
  <head>
    <style>body{font-family:sans-serif;}pre{background-color:#eee;padding:1em;white-space:pre-wrap;}</style>
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
  }

  def "test readme example"() {
    given:
    project.apply plugin: "com.android.application"
    project.android {
      compileSdkVersion COMPILE_SDK_VERSION
      buildToolsVersion BUILD_TOOLS_VERSION

      defaultConfig {
        applicationId APPLICATION_ID
      }
    }
    project.dependencies {
      delegate.compile DESIGN
      delegate.compile ANDROID_GIF_DRAWABLE
    }

    when:
    project.evaluate()
    plugin.apply(project)

    // Change output directory for testing
    final LicenseReportTask task = project.tasks.getByName("licenseDebugReport")
    task.assetDirs = [assertDir]
    task.htmlFile = htmlFile
    task.jsonFile = jsonFile
    task.execute()

    def actualHtml = task.htmlFile.text.trim()
    def expectedHtml =
      """
<html>
  <head>
    <style>body{font-family:sans-serif;}pre{background-color:#eee;padding:1em;white-space:pre-wrap;}</style>
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
  }
}
