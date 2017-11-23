package com.jaredsburrows.license

import spock.lang.Unroll
import test.BaseAndroidSpecification

/**
 * @author <a href="mailto:jaredsburrows@gmail.com">Jared Burrows</a>
 */
final class LicenseReportTaskAndroidSpec extends BaseAndroidSpecification {
  @Unroll "android - #taskName - no dependencies"() {
    given:
    project.apply plugin: "com.android.application"
    new LicensePlugin().apply(project) // project.apply plugin: "com.jaredsburrows.license"
    project.android {
      compileSdkVersion COMPILE_SDK_VERSION
      buildToolsVersion BUILD_TOOLS_VERSION

      defaultConfig {
        applicationId APPLICATION_ID
      }
    }

    when:
    project.evaluate()
    def task = project.tasks.getByName(taskName) as LicenseReportTask
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

  @Unroll "android - #taskName - no open source dependencies"() {
    given:
    project.apply plugin: "com.android.application"
    new LicensePlugin().apply(project) // project.apply plugin: "com.jaredsburrows.license"
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
    def task = project.tasks.getByName(taskName) as LicenseReportTask
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

  @Unroll "android - #taskName - default buildTypes"() {
    given:
    project.apply plugin: "com.android.application"
    new LicensePlugin().apply(project) // project.apply plugin: "com.jaredsburrows.license"
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
    def task = project.tasks.getByName(taskName) as LicenseReportTask
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

  @Unroll "android - #taskName - buildTypes"() {
    given:
    project.apply plugin: "com.android.application"
    new LicensePlugin().apply(project) // project.apply plugin: "com.jaredsburrows.license"
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
    def task = project.tasks.getByName(taskName) as LicenseReportTask
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

  @Unroll "android - #taskName - buildTypes + productFlavors + flavorDimensions"() {
    given:
    project.apply plugin: "com.android.application"
    new LicensePlugin().apply(project) // project.apply plugin: "com.jaredsburrows.license"
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
    def task = project.tasks.getByName(taskName) as LicenseReportTask
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

  @Unroll "readme example - #taskName"() {
    given:
    project.apply plugin: "com.android.application"
    new LicensePlugin().apply(project) // project.apply plugin: "com.jaredsburrows.license"
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
    def task = project.tasks.getByName(taskName) as LicenseReportTask
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

  // TODO
  def "jvm - #projectPlugin licenseReport - test new configurations, remove when AGP 3 is out"() {
    given:
    project.apply plugin: projectPlugin
    new LicensePlugin().apply(project) // project.apply plugin: "com.jaredsburrows.license"
    project.dependencies {
      api APPCOMPAT_V7
      implementation project.project(":subproject")
    }

    subproject.apply plugin: "java-library"
    subproject.dependencies {
      implementation DESIGN
    }

    when:
    project.evaluate()
    def task = project.tasks.getByName("licenseReport") as LicenseReportTask
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
    projectPlugin << ["java-library"]
  }
}
