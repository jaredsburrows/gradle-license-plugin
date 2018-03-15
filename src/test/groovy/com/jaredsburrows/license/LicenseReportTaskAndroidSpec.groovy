package com.jaredsburrows.license

import spock.lang.Unroll
import test.BaseAndroidSpecification

final class LicenseReportTaskAndroidSpec extends BaseAndroidSpecification {
  @Unroll def "android - #taskName - no dependencies"() {
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
    def task = project.tasks.getByName(taskName) as LicenseReportTask
    task.execute()

    def actualHtml = task.htmlFile.text.stripIndent().trim()
    def expectedHtml =
      """
<html>
  <head>
    <style>body{font-family: sans-serif} pre{background-color: #eeeeee; padding: 1em; white-space: pre-wrap}</style>
    <title>Open source licenses</title>
  </head>
  <body>
    <h3>None</h3>
  </body>
</html>
""".stripIndent().trim()
    def actualJson = task.jsonFile.text.stripIndent().trim()
    def expectedJson =
      """
[]
""".stripIndent().trim()

    then:
    actualHtml == expectedHtml
    actualJson == expectedJson

    where:
    taskName << ["licenseDebugReport", "licenseReleaseReport"]
  }

  @Unroll def "android - #taskName - no open source dependencies"() {
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
    def task = project.tasks.getByName(taskName) as LicenseReportTask
    task.execute()

    def actualHtml = task.htmlFile.text.stripIndent().trim()
    def expectedHtml =
      """
<html>
  <head>
    <style>body{font-family: sans-serif} pre{background-color: #eeeeee; padding: 1em; white-space: pre-wrap}</style>
    <title>Open source licenses</title>
  </head>
  <body>
    <h3>None</h3>
  </body>
</html>
""".stripIndent().trim()
    def actualJson = task.jsonFile.text.stripIndent().trim()
    def expectedJson =
      """
[]
""".stripIndent().trim()

    then:
    actualHtml == expectedHtml
    actualJson == expectedJson

    where:
    taskName << ["licenseDebugReport", "licenseReleaseReport"]
  }

  @Unroll def "android - #taskName - default buildTypes"() {
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
    def task = project.tasks.getByName(taskName) as LicenseReportTask
    task.execute()

    def actualHtml = task.htmlFile.text.stripIndent().trim()
    def expectedHtml =
      """
<html>
  <head>
    <style>body{font-family: sans-serif} pre{background-color: #eeeeee; padding: 1em; white-space: pre-wrap}</style>
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
        ]
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
        ]
    }
]
""".stripIndent().trim()

    then:
    actualHtml == expectedHtml
    actualJson == expectedJson

    where:
    taskName << ["licenseDebugReport", "licenseReleaseReport"]
  }

  @Unroll def "android - #taskName - buildTypes"() {
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
    def task = project.tasks.getByName(taskName) as LicenseReportTask
    task.execute()

    def actualHtml = task.htmlFile.text.stripIndent().trim()
    def expectedHtml =
      """
<html>
  <head>
    <style>body{font-family: sans-serif} pre{background-color: #eeeeee; padding: 1em; white-space: pre-wrap}</style>
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
        ]
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
        ]
    }
]
""".stripIndent().trim()

    then:
    actualHtml == expectedHtml
    actualJson == expectedJson

    where:
    taskName << ["licenseDebugReport", "licenseReleaseReport"]
  }

  @Unroll def "android - #taskName - buildTypes + productFlavors + flavorDimensions"() {
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
    def task = project.tasks.getByName(taskName) as LicenseReportTask
    task.execute()

    def actualHtml = task.htmlFile.text.stripIndent().trim()
    def expectedHtml =
      """
<html>
  <head>
    <style>body{font-family: sans-serif} pre{background-color: #eeeeee; padding: 1em; white-space: pre-wrap}</style>
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
        ]
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
        ]
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
        ]
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
        ]
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

  @Unroll def "readme example - #taskName"() {
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
    def task = project.tasks.getByName(taskName) as LicenseReportTask
    task.execute()

    def actualHtml = task.htmlFile.text.stripIndent().trim()
    def expectedHtml =
      """
<html>
  <head>
    <style>body{font-family: sans-serif} pre{background-color: #eeeeee; padding: 1em; white-space: pre-wrap}</style>
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
        ]
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
        ]
    }
]
""".stripIndent().trim()

    then:
    actualHtml == expectedHtml
    actualJson == expectedJson

    where:
    taskName << ["licenseDebugReport", "licenseReleaseReport"]
  }

  @Unroll def "android - #taskName - reports enabled - copy enabled #copyEnabled"() {
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
    def task = project.tasks.getByName(taskName) as LicenseReportTask
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

  @Unroll def "android - #taskName - reports disabled - copy enabled #copyEnabled"() {
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
    def task = project.tasks.getByName(taskName) as LicenseReportTask
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

  @Unroll def "android - #taskName - default buildTypes - multi module - android and java"() {
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
    def task = project.tasks.getByName(taskName) as LicenseReportTask
    task.execute()

    def actualHtml = task.htmlFile.text.stripIndent().trim()
    def expectedHtml =
      """
<html>
  <head>
    <style>body{font-family: sans-serif} pre{background-color: #eeeeee; padding: 1em; white-space: pre-wrap}</style>
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
        ]
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
        ]
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
