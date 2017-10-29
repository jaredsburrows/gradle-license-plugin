package com.jaredsburrows.license

import com.android.build.gradle.internal.SdkHandler
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Unroll
import test.BaseSpecification

/**
 * @author <a href="mailto:jaredsburrows@gmail.com">Jared Burrows</a>
 */
final class LicenseReportTaskSpec extends BaseSpecification {
  // Maven repo - "file://${System.env.ANDROID_HOME}/extras/android/m2repository"
  final static APPCOMPAT_V7 = "com.android.support:appcompat-v7:$SUPPORT_VERSION"
  final static DESIGN = "com.android.support:design:$SUPPORT_VERSION"
  final static SUPPORT_ANNOTATIONS = "com.android.support:support-annotations:$SUPPORT_VERSION"
  final static SUPPORT_V4 = "com.android.support:support-v4:$SUPPORT_VERSION"
  // Maven repo - "file://${System.env.ANDROID_HOME}/extras/google/m2repository"
  final static FIREBASE_CORE = "com.google.firebase:firebase-core:10.0.1"
  // Others
  final static ANDROID_GIF_DRAWABLE = "pl.droidsonroids.gif:android-gif-drawable:1.2.3"
  final static FAKE_DEPENDENCY = "group:name:1.0.0"                               // Single license
  final static FAKE_DEPENDENCY2 = "group:name2:1.0.0"                             // Multiple license
  final static FAKE_DEPENDENCY3 = "group:name3:1.0.0"                             // Bad license
  final static CHILD_DEPENDENCY = "group:child:1.0.0"                             // Child license -> Parent license
  final static RETROFIT_DEPENDENCY = "com.squareup.retrofit2:retrofit:2.3.0"      // Child license -> Parent license
  // Projects
  def project
  def subproject

  def "setup"() {
    // Setup project
    project = ProjectBuilder.builder()
      .withProjectDir(new File(PROJECT_SOURCE_DIR))
      .withName("project")
      .build()
    // Make sure Android projects have a manifest
    project.file(MANIFEST_FILE_PATH).text = MANIFEST
    project.repositories {
      maven { url TEST_MAVEN_REPOSITORY }
    }

    // Setup subproject
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

  @Unroll "jvm - #projectPlugin licenseReport - no dependencies"() {
    given:
    project.apply plugin: projectPlugin
    new LicensePlugin().apply(project) // project.apply plugin: "com.jaredsburrows.license"

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
    projectPlugin << LicensePlugin.JVM_PLUGINS
  }

  @Unroll "jvm - #projectPlugin licenseReport - project dependencies"() {
    given:
    project.apply plugin: projectPlugin
    new LicensePlugin().apply(project) // project.apply plugin: "com.jaredsburrows.license"
    project.dependencies {
      compile APPCOMPAT_V7
      compile project.project(":subproject")
    }

    subproject.apply plugin: "java-library"
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
    projectPlugin << LicensePlugin.JVM_PLUGINS
  }

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

  @Unroll "jvm - #projectPlugin licenseReport - no open source dependencies"() {
    given:
    project.apply plugin: projectPlugin
    new LicensePlugin().apply(project) // project.apply plugin: "com.jaredsburrows.license"
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
    projectPlugin << LicensePlugin.JVM_PLUGINS
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

  @Unroll "jvm - #projectPlugin licenseReport"() {
    given:
    project.apply plugin: projectPlugin
    new LicensePlugin().apply(project) // project.apply plugin: "com.jaredsburrows.license"
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
    projectPlugin << LicensePlugin.JVM_PLUGINS
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
    project.apply plugin: "java-library"
    new LicensePlugin().apply(project) // project.apply plugin: "com.jaredsburrows.license"
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
    project.apply plugin: "java-library"
    new LicensePlugin().apply(project) // project.apply plugin: "com.jaredsburrows.license"
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

  def "jvm - dependency with full pom - project name, multiple developers, url, year, multiple licenses"() {
    given:
    project.apply plugin: "java-library"
    new LicensePlugin().apply(project) // project.apply plugin: "com.jaredsburrows.license"
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

  def "jvm - dependency without license information - check it's parent"() {
    given:
    project.apply plugin: "java-library"
    new LicensePlugin().apply(project) // project.apply plugin: "com.jaredsburrows.license"
    project.dependencies {
      compile CHILD_DEPENDENCY
      compile RETROFIT_DEPENDENCY
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
      <li>
        <a href='#1288288048'>Retrofit</a>
      </li>
    </ul>
    <a name='755502249' />
    <h3>Some license</h3>
    <pre>Some license, http://website.tld/</pre>
    <a name='1288288048' />
    <h3>Apache 2.0</h3>
    <pre>Apache 2.0, http://www.apache.org/licenses/LICENSE-2.0.txt</pre>
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
    },
    {
        "project": "Retrofit",
        "developers": null,
        "url": null,
        "year": null,
        "license": "Apache 2.0",
        "license_url": "http://www.apache.org/licenses/LICENSE-2.0.txt"
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
    projectPlugin << ["java-library"]
  }
}
