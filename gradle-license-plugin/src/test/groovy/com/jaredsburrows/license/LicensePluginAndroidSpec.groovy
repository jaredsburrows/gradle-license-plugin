package com.jaredsburrows.license

import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import spock.lang.Unroll

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS
import static test.TestUtils.assertHtml
import static test.TestUtils.assertJson
import static test.TestUtils.getLicenseText
import static test.TestUtils.gradleWithCommand

final class LicensePluginAndroidSpec extends Specification {
  @Rule
  public final TemporaryFolder testProjectDir = new TemporaryFolder()
  private int compileSdkVersion = 34
  private List<File> pluginClasspath
  private String classpathString
  private String mavenRepoUrl
  private File buildFile
  private String reportFolder
  private String srcFolder
  private String mainAssetsFolder

  def 'setup'() {
    def pluginClasspathResource = getClass().classLoader.getResource('plugin-classpath.txt')
    if (pluginClasspathResource == null) {
      throw new IllegalStateException(
        'Did not find plugin classpath resource, run `testClasses` build task.')
    }

    pluginClasspath = pluginClasspathResource.readLines().collect { new File(it) }
    classpathString = pluginClasspath
      .collect { it.absolutePath.replace('\\', '\\\\') } // escape backslashes in Windows paths
      .collect { "'$it'" }
      .join(", ")
    mavenRepoUrl = getClass().getResource('/maven').toURI()
    buildFile = testProjectDir.newFile('build.gradle')
    // In case we're on Windows, fix the \s in the string containing the name
    reportFolder = "${testProjectDir.root.path.replaceAll('\\\\', '/')}/build/reports/licenses"
    srcFolder = "${testProjectDir.root.path.replaceAll('\\\\', '/')}/src"
    mainAssetsFolder = "${srcFolder}/main/assets"
  }

  @Unroll
  def '#taskName that has no dependencies'() {
    given:
    buildFile <<
      """
      buildscript {
        dependencies {
          classpath files($classpathString)
        }
      }

      apply plugin: 'com.android.application'
      apply plugin: 'com.jaredsburrows.license'

      android {
        compileSdkVersion $compileSdkVersion

        defaultConfig {
          applicationId 'com.example'
        }
      }
      """

    when:
    def result = gradleWithCommand(testProjectDir.root, "${taskName}", '-s')
    def actualCsv = new File(reportFolder, "${taskName}.csv")
    def actualHtml = new File(reportFolder, "${taskName}.html")
    def openSourceHtml = new File(mainAssetsFolder, "open_source_licenses.html")
    def expectedHtml =
      """
      <!DOCTYPE html>
      <html lang="en">
        <head>
          <meta http-equiv="content-type" content="text/html; charset=utf-8">
          <style>body { font-family: sans-serif; background-color: #ffffff; color: #000000; } a { color: #0000EE; } pre { background-color: #eeeeee; padding: 1em; white-space: pre-wrap; word-break: break-word; display: inline-block; } @media (prefers-color-scheme: dark) { body { background-color: #121212; color: #E0E0E0; } a { color: #BB86FC; } pre { background-color: #333333; color: #E0E0E0; } }</style>
          <title>Open source licenses</title>
        </head>
        <body>
          <h3>None</h3>
        </body>
      </html>
      """
    def actualJson = new File(reportFolder, "${taskName}.json")
    def expectedJson =
      """
      []
      """
    def actualText = new File(reportFolder, "${taskName}.txt")

    then:
    result.task(":${taskName}").outcome == SUCCESS
    result.output.find("Wrote CSV report to .*${reportFolder}/${taskName}.csv.")
    actualCsv.exists()
    result.output.find("Wrote HTML report to .*${reportFolder}/${taskName}.html.")
    actualHtml.exists()
    openSourceHtml.exists()
    result.output.find("Wrote JSON report to .*${reportFolder}/${taskName}.json.")
    actualJson.exists()
    result.output.find("Wrote Text report to .*${reportFolder}/${taskName}.txt.")
    actualText.exists()
    assertHtml(expectedHtml, actualHtml.text)
    assertJson(expectedJson, actualJson.text)

    where:
    taskName << ['licenseDebugReport', 'licenseReleaseReport']
  }

  @Unroll
  def '#taskName with default buildTypes'() {
    given:
    buildFile <<
      """
      buildscript {
        dependencies {
          classpath files($classpathString)
        }
      }

      repositories {
        maven {
          url '${mavenRepoUrl}'
        }
      }

      apply plugin: 'com.android.application'
      apply plugin: 'com.jaredsburrows.license'

      android {
        compileSdkVersion $compileSdkVersion

        defaultConfig {
          applicationId 'com.example'
        }
      }

      dependencies {
        // Handles duplicates
        implementation 'com.android.support:appcompat-v7:26.1.0'
        implementation 'com.android.support:appcompat-v7:26.1.0'
        implementation 'com.android.support:design:26.1.0'
      }
      """

    when:
    def result = gradleWithCommand(testProjectDir.root, "${taskName}", '-s')
    def actualCsv = new File(reportFolder, "${taskName}.csv")
    def actualHtml = new File(reportFolder, "${taskName}.html")
    def openSourceHtml = new File(mainAssetsFolder, "open_source_licenses.html")
    def expectedHtml =
      """
      <!DOCTYPE html>
      <html lang="en">
        <head>
          <meta http-equiv="content-type" content="text/html; charset=utf-8">
          <style>body { font-family: sans-serif; background-color: #ffffff; color: #000000; } a { color: #0000EE; } pre { background-color: #eeeeee; padding: 1em; white-space: pre-wrap; word-break: break-word; display: inline-block; } @media (prefers-color-scheme: dark) { body { background-color: #121212; color: #E0E0E0; } a { color: #BB86FC; } pre { background-color: #333333; color: #E0E0E0; } }</style>
          <title>Open source licenses</title>
        </head>
        <body>
          <h3>Notice for packages:</h3>
          <ul>
            <li>
              <a href="#1934118923">appcompat-v7</a>
              <dl>
                <dt>Copyright &copy; 20xx The original author or authors</dt>
                <dd></dd>
              </dl>
            </li>
            <li>
              <a href="#1934118923">design</a>
              <dl>
                <dt>Copyright &copy; 20xx The original author or authors</dt>
                <dd></dd>
              </dl>
            </li>
          </ul>
          <a id="1934118923"></a>
          <pre>${getLicenseText('apache-2.0.txt')}</pre>
          <br>
          <hr>
        </body>
      </html>
      """
    def actualJson = new File(reportFolder, "${taskName}.json")
    def expectedJson =
      """
      [
        {
          "project": "appcompat-v7",
          "description": null,
          "version": "26.1.0",
          "developers": [],
          "url": null,
          "year": null,
          "licenses": [
            {
              "license": "The Apache Software License",
              "license_url": "http://www.apache.org/licenses/LICENSE-2.0.txt"
            }
          ],
          "dependency": "com.android.support:appcompat-v7:26.1.0"
        },
        {
          "project": "design",
          "description": null,
          "version": "26.1.0",
          "developers": [],
          "url": null,
          "year": null,
          "licenses": [
            {
              "license": "The Apache Software License",
              "license_url": "http://www.apache.org/licenses/LICENSE-2.0.txt"
            }
          ],
          "dependency": "com.android.support:design:26.1.0"
        }
      ]
      """
    def actualText = new File(reportFolder, "${taskName}.txt")

    then:
    result.task(":${taskName}").outcome == SUCCESS
    result.output.find("Wrote CSV report to .*${reportFolder}/${taskName}.csv.")
    actualCsv.exists()
    result.output.find("Wrote HTML report to .*${reportFolder}/${taskName}.html.")
    actualHtml.exists()
    openSourceHtml.exists()
    result.output.find("Wrote JSON report to .*${reportFolder}/${taskName}.json.")
    actualJson.exists()
    result.output.find("Wrote Text report to .*${reportFolder}/${taskName}.txt.")
    actualText.exists()
    assertHtml(expectedHtml, actualHtml.text)
    assertJson(expectedJson, actualJson.text)

    where:
    taskName << ['licenseDebugReport', 'licenseReleaseReport']
  }

  @Unroll
  def 'not UP-TO-DATE when dependencies change'() {
    given:
    def originalBuildFile = """
      buildscript {
        dependencies {
          classpath files($classpathString)
        }
      }

      repositories {
        maven {
          url '${mavenRepoUrl}'
        }
      }

      apply plugin: 'com.android.application'
      apply plugin: 'com.jaredsburrows.license'

      android {
        compileSdkVersion $compileSdkVersion

        defaultConfig {
          applicationId 'com.example'
        }
      }

      dependencies {
        implementation 'com.android.support:appcompat-v7:26.1.0'
      }
      """
    def modifiedBuildFile = """
      buildscript {
        dependencies {
          classpath files($classpathString)
        }
      }

      repositories {
        maven {
          url '${mavenRepoUrl}'
        }
      }

      apply plugin: 'com.android.application'
      apply plugin: 'com.jaredsburrows.license'

      android {
        compileSdkVersion $compileSdkVersion

        defaultConfig {
          applicationId 'com.example'
        }
      }

      dependencies {
        implementation 'com.android.support:appcompat-v7:26.1.0'
        // This is a new dependency
        implementation 'com.android.support:design:26.1.0'
      }
      """

    when:
    buildFile << originalBuildFile
    def result1 = gradleWithCommand(testProjectDir.root, "${taskName}", '-s')
    buildFile << modifiedBuildFile
    def result2 = gradleWithCommand(testProjectDir.root, "${taskName}", '-s')

    then:
    result1.task(":${taskName}").outcome == SUCCESS
    result2.task(":${taskName}").outcome == SUCCESS

    where:
    taskName << ['licenseDebugReport', 'licenseReleaseReport']
  }

  @Unroll
  def '#taskName with buildTypes'() {
    given:
    buildFile <<
      """
      buildscript {
        dependencies {
          classpath files($classpathString)
        }
      }

      repositories {
        maven {
          url '${mavenRepoUrl}'
        }
      }

      apply plugin: 'com.android.application'
      apply plugin: 'com.jaredsburrows.license'

      android {
        compileSdkVersion $compileSdkVersion

        defaultConfig {
          applicationId 'com.example'
        }

        buildTypes {
          debug {}
          release {}
        }
      }

      dependencies {
        implementation 'com.android.support:appcompat-v7:26.1.0'

        debugImplementation 'com.android.support:design:26.1.0'
        releaseImplementation 'com.android.support:design:26.1.0'
      }
      """

    when:
    def result = gradleWithCommand(testProjectDir.root, "${taskName}", '-s')
    def actualCsv = new File(reportFolder, "${taskName}.csv")
    def actualHtml = new File(reportFolder, "${taskName}.html")
    def openSourceHtml = new File(mainAssetsFolder, "open_source_licenses.html")
    def expectedHtml =
      """
      <!DOCTYPE html>
      <html lang="en">
        <head>
          <meta http-equiv="content-type" content="text/html; charset=utf-8">
          <style>body { font-family: sans-serif; background-color: #ffffff; color: #000000; } a { color: #0000EE; } pre { background-color: #eeeeee; padding: 1em; white-space: pre-wrap; word-break: break-word; display: inline-block; } @media (prefers-color-scheme: dark) { body { background-color: #121212; color: #E0E0E0; } a { color: #BB86FC; } pre { background-color: #333333; color: #E0E0E0; } }</style>
          <title>Open source licenses</title>
        </head>
        <body>
          <h3>Notice for packages:</h3>
          <ul>
            <li>
              <a href="#1934118923">appcompat-v7</a>
              <dl>
                <dt>Copyright &copy; 20xx The original author or authors</dt>
                <dd></dd>
              </dl>
            </li>
            <li>
              <a href="#1934118923">design</a>
              <dl>
                <dt>Copyright &copy; 20xx The original author or authors</dt>
                <dd></dd>
              </dl>
            </li>
          </ul>
          <a id="1934118923"></a>
          <pre>${getLicenseText('apache-2.0.txt')}</pre>
          <br>
          <hr>
        </body>
      </html>
      """
    def actualJson = new File(reportFolder, "${taskName}.json")
    def expectedJson =
      """
      [
        {
          "project": "appcompat-v7",
          "description": null,
          "version": "26.1.0",
          "developers": [],
          "url": null,
          "year": null,
          "licenses": [
            {
              "license": "The Apache Software License",
              "license_url": "http://www.apache.org/licenses/LICENSE-2.0.txt"
            }
          ],
          "dependency": "com.android.support:appcompat-v7:26.1.0"
        },
        {
          "project": "design",
          "description": null,
          "version": "26.1.0",
          "developers": [],
          "url": null,
          "year": null,
          "licenses": [
            {
              "license": "The Apache Software License",
              "license_url": "http://www.apache.org/licenses/LICENSE-2.0.txt"
            }
          ],
          "dependency": "com.android.support:design:26.1.0"
        }
      ]
      """
    def actualText = new File(reportFolder, "${taskName}.txt")

    then:
    result.task(":${taskName}").outcome == SUCCESS
    result.output.find("Wrote CSV report to .*${reportFolder}/${taskName}.csv.")
    actualCsv.exists()
    result.output.find("Wrote HTML report to .*${reportFolder}/${taskName}.html.")
    actualHtml.exists()
    openSourceHtml.exists()
    result.output.find("Wrote JSON report to .*${reportFolder}/${taskName}.json.")
    actualJson.exists()
    result.output.find("Wrote Text report to .*${reportFolder}/${taskName}.txt.")
    actualText.exists()
    assertHtml(expectedHtml, actualHtml.text)
    assertJson(expectedJson, actualJson.text)

    where:
    taskName << ['licenseDebugReport', 'licenseReleaseReport']
  }

  @Unroll
  def '#taskName with buildTypes + productFlavors + flavorDimensions'() {
    given:
    buildFile <<
      """
      buildscript {
        dependencies {
          classpath files($classpathString)
        }
      }

      repositories {
        maven {
          url '${mavenRepoUrl}'
        }
      }

      apply plugin: 'com.android.application'
      apply plugin: 'com.jaredsburrows.license'

      android {
        compileSdkVersion $compileSdkVersion

        defaultConfig {
          applicationId 'com.example'
        }

        buildTypes {
          debug {}
          release {}
        }

        flavorDimensions 'a', 'b'

        productFlavors {
          flavor1 { dimension 'a' }
          flavor2 { dimension 'a' }
          flavor3 { dimension 'b' }
          flavor4 { dimension 'b' }
        }
      }

      dependencies {
        implementation 'com.android.support:appcompat-v7:26.1.0'

        debugImplementation 'com.android.support:design:26.1.0'
        releaseImplementation 'com.android.support:design:26.1.0'

        flavor1Implementation 'com.android.support:support-v4:26.1.0'
        flavor2Implementation 'com.android.support:support-v4:26.1.0'
        flavor3Implementation 'com.android.support:support-annotations:26.1.0'
        flavor4Implementation 'com.android.support:support-annotations:26.1.0'
      }
      """

    when:
    def result = gradleWithCommand(testProjectDir.root, "${taskName}", '-s')
    def actualCsv = new File(reportFolder, "${taskName}.csv")
    def actualHtml = new File(reportFolder, "${taskName}.html")
    def openSourceHtml = new File(mainAssetsFolder, "open_source_licenses.html")
    def expectedHtml =
      """
      <!DOCTYPE html>
      <html lang="en">
        <head>
          <meta http-equiv="content-type" content="text/html; charset=utf-8">
          <style>body { font-family: sans-serif; background-color: #ffffff; color: #000000; } a { color: #0000EE; } pre { background-color: #eeeeee; padding: 1em; white-space: pre-wrap; word-break: break-word; display: inline-block; } @media (prefers-color-scheme: dark) { body { background-color: #121212; color: #E0E0E0; } a { color: #BB86FC; } pre { background-color: #333333; color: #E0E0E0; } }</style>
          <title>Open source licenses</title>
        </head>
        <body>
          <h3>Notice for packages:</h3>
          <ul>
            <li><a href="#1934118923">appcompat-v7</a>
              <dl>
                <dt>Copyright &copy; 20xx The original author or authors</dt>
                <dd></dd>
              </dl>
            </li>
            <li><a href="#1934118923">design</a>
              <dl>
                <dt>Copyright &copy; 20xx The original author or authors</dt>
                <dd></dd>
              </dl>
            </li>
            <li><a href="#1934118923">support-annotations</a>
              <dl>
                <dt>Copyright &copy; 20xx The original author or authors</dt>
                <dd></dd>
              </dl>
            </li>
            <li><a href="#1934118923">support-v4</a>
              <dl>
                <dt>Copyright &copy; 20xx The original author or authors</dt>
                <dd></dd>
              </dl>
            </li>
          </ul>
          <a id="1934118923"></a>
          <pre>${getLicenseText('apache-2.0.txt')}</pre>
          <br>
          <hr>
        </body>
      </html>
      """
    def actualJson = new File(reportFolder, "${taskName}.json")
    def expectedJson =
      """
      [
        {
          "project": "appcompat-v7",
          "description": null,
          "version": "26.1.0",
          "developers": [],
          "url": null,
          "year": null,
          "licenses": [
            {
              "license": "The Apache Software License",
              "license_url": "http://www.apache.org/licenses/LICENSE-2.0.txt"
            }
          ],
          "dependency": "com.android.support:appcompat-v7:26.1.0"
        },
        {
          "project": "design",
          "description": null,
          "version": "26.1.0",
          "developers": [],
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
          "project": "support-annotations",
          "description": null,
          "version": "26.1.0",
          "developers": [],
          "url": null,
          "year": null,
          "licenses": [
            {
              "license": "The Apache Software License",
              "license_url": "http://www.apache.org/licenses/LICENSE-2.0.txt"
            }
          ],
          "dependency": "com.android.support:support-annotations:26.1.0"
        },
        {
          "project": "support-v4",
          "description": null,
          "version": "26.1.0",
          "developers": [],
          "url": null,
          "year": null,
          "licenses": [
            {
              "license": "The Apache Software License",
              "license_url": "http://www.apache.org/licenses/LICENSE-2.0.txt"
            }
          ],
          "dependency": "com.android.support:support-v4:26.1.0"
        }
      ]
      """
    def actualText = new File(reportFolder, "${taskName}.txt")

    then:
    result.task(":${taskName}").outcome == SUCCESS
    result.output.find("Wrote CSV report to .*${reportFolder}/${taskName}.csv.")
    actualCsv.exists()
    result.output.find("Wrote HTML report to .*${reportFolder}/${taskName}.html.")
    actualHtml.exists()
    openSourceHtml.exists()
    result.output.find("Wrote JSON report to .*${reportFolder}/${taskName}.json.")
    actualJson.exists()
    result.output.find("Wrote Text report to .*${reportFolder}/${taskName}.txt.")
    actualText.exists()
    assertHtml(expectedHtml, actualHtml.text)
    assertJson(expectedJson, actualJson.text)

    where:
    taskName << ['licenseFlavor1Flavor3DebugReport', 'licenseFlavor1Flavor3ReleaseReport',
                 'licenseFlavor2Flavor4DebugReport', 'licenseFlavor2Flavor4ReleaseReport']
  }

  def 'run build task with buildTypes + productFlavors + flavorDimensions'() {
    given:
    buildFile <<
      """
      buildscript {
        dependencies {
          classpath files($classpathString)
        }
      }

      repositories {
        maven {
          url '${mavenRepoUrl}'
        }
      }

      apply plugin: 'com.android.application'
      apply plugin: 'com.jaredsburrows.license'

      android {
        compileSdkVersion $compileSdkVersion

        defaultConfig {
          applicationId 'com.example'
        }

        buildTypes {
          debug {}
          release {}
        }

        flavorDimensions 'a', 'b'

        productFlavors {
          flavor1 { dimension 'a' }
          flavor2 { dimension 'a' }
          flavor3 { dimension 'b' }
          flavor4 { dimension 'b' }
        }
      }

      dependencies {
      }
      """

    when:
    def result = gradleWithCommand(testProjectDir.root, 'licenseFlavor1Flavor3DebugReport',
      'licenseFlavor1Flavor3ReleaseReport', 'licenseFlavor2Flavor4DebugReport',
      'licenseFlavor2Flavor4ReleaseReport', '-s')

    then:
    result.task(":licenseFlavor1Flavor3DebugReport").outcome == SUCCESS
    result.task(":licenseFlavor1Flavor3ReleaseReport").outcome == SUCCESS
    result.task(":licenseFlavor2Flavor4DebugReport").outcome == SUCCESS
    result.task(":licenseFlavor2Flavor4ReleaseReport").outcome == SUCCESS
  }

  @Unroll
  def '#taskName from readme example'() {
    given:
    buildFile <<
      """
      buildscript {
        dependencies {
          classpath files($classpathString)
        }
      }

      repositories {
        maven {
          url '${mavenRepoUrl}'
        }
      }

      apply plugin: 'com.android.application'
      apply plugin: 'com.jaredsburrows.license'

      android {
        compileSdkVersion $compileSdkVersion

        defaultConfig {
          applicationId 'com.example'
        }
      }

      dependencies {
        debugImplementation 'com.android.support:design:26.1.0'
        debugImplementation 'pl.droidsonroids.gif:android-gif-drawable:1.2.3'
        releaseImplementation 'com.android.support:design:26.1.0'
        releaseImplementation 'pl.droidsonroids.gif:android-gif-drawable:1.2.3'
      }
      """

    when:
    def result = gradleWithCommand(testProjectDir.root, "${taskName}", '-s')
    def actualCsv = new File(reportFolder, "${taskName}.csv")
    def actualHtml = new File(reportFolder, "${taskName}.html")
    def openSourceHtml = new File(mainAssetsFolder, "open_source_licenses.html")
    def expectedHtml =
      """
      <!DOCTYPE html>
      <html lang="en">
        <head>
          <meta http-equiv="content-type" content="text/html; charset=utf-8">
          <style>body { font-family: sans-serif; background-color: #ffffff; color: #000000; } a { color: #0000EE; } pre { background-color: #eeeeee; padding: 1em; white-space: pre-wrap; word-break: break-word; display: inline-block; } @media (prefers-color-scheme: dark) { body { background-color: #121212; color: #E0E0E0; } a { color: #BB86FC; } pre { background-color: #333333; color: #E0E0E0; } }</style>
          <title>Open source licenses</title>
        </head>
        <body>
          <h3>Notice for packages:</h3>
          <ul>
            <li><a href="#1934118923">design</a>
              <dl>
                <dt>Copyright &copy; 20xx The original author or authors</dt>
                <dd></dd>
              </dl>
            </li>
          </ul>
          <a id="1934118923"></a>
          <pre>${getLicenseText('apache-2.0.txt')}</pre>
          <br>
          <hr>
          <ul>
            <li><a href="#1783810846">Android GIF Drawable Library</a>
              <dl>
                <dt>Copyright &copy; 20xx Karol WrXXtniak</dt>
                <dd></dd>
              </dl>
            </li>
          </ul>
          <a id="1783810846"></a>
          <pre>${getLicenseText('mit.txt')}</pre>
          <br>
          <hr>
        </body>
      </html>
      """
    def actualJson = new File(reportFolder, "${taskName}.json")
    def expectedJson =
      """
      [
        {
          "project":"Android GIF Drawable Library",
          "description":"Views and Drawable for displaying animated GIFs for Android",
          "version":"1.2.3",
          "developers":[
            "Karol Wr\\u00c3\\u00b3tniak"
          ],
          "url":"https://github.com/koral--/android-gif-drawable",
          "year":null,
          "licenses":[
            {
              "license":"The MIT License",
              "license_url":"http://opensource.org/licenses/MIT"
            }
          ],
          "dependency":"pl.droidsonroids.gif:android-gif-drawable:1.2.3"
        },
        {
          "project":"design",
          "description":null,
          "version":"26.1.0",
          "developers":[],
          "url":null,
          "year":null,
          "licenses":[
            {
              "license":"The Apache Software License",
              "license_url":"http://www.apache.org/licenses/LICENSE-2.0.txt"
            }
          ],
          "dependency":"com.android.support:design:26.1.0"
        }
      ]
      """
    def actualText = new File(reportFolder, "${taskName}.txt")
    def expectedText =
      """
      Notice for packages


      Android GIF Drawable Library (1.2.3) - The MIT License
      Views and Drawable for displaying animated GIFs for Android
      https://github.com/koral--/android-gif-drawable

      design (26.1.0) - The Apache Software License
      """.stripIndent().trim()

    then:
    result.task(":${taskName}").outcome == SUCCESS
    result.output.find("Wrote CSV report to .*${reportFolder}/${taskName}.csv.")
    actualCsv.exists()
    result.output.find("Wrote HTML report to .*${reportFolder}/${taskName}.html.")
    actualHtml.exists()
    openSourceHtml.exists()
    result.output.find("Wrote JSON report to .*${reportFolder}/${taskName}.json.")
    actualJson.exists()
    result.output.find("Wrote Text report to .*${reportFolder}/${taskName}.txt.")
    actualText.exists()
    assertHtml(expectedHtml, actualHtml.text)
    assertJson(expectedJson, actualJson.text)
    expectedText == actualText.text.stripIndent().trim()

    where:
    taskName << ['licenseDebugReport', 'licenseReleaseReport']
  }

  @Unroll
  def '#taskName with no open source dependencies'() {
    given:
    buildFile <<
      """
        buildscript {
          dependencies {
            classpath files($classpathString)
          }
        }

        repositories {
          maven {
            url '${mavenRepoUrl}'
          }
        }

        apply plugin: 'com.android.application'
        apply plugin: 'com.jaredsburrows.license'

        android {
          compileSdkVersion $compileSdkVersion

          defaultConfig {
            applicationId 'com.example'
          }
        }

        dependencies {
          implementation 'group:name4:1.0.0'
        }
      """

    when:
    def result = gradleWithCommand(testProjectDir.root, "${taskName}", '-s')
    def actualCsv = new File(reportFolder, "${taskName}.csv")
    def actualHtml = new File(reportFolder, "${taskName}.html")
    def openSourceHtml = new File(mainAssetsFolder, "open_source_licenses.html")
    def expectedHtml =
      """
      <!DOCTYPE html>
      <html lang="en">
        <head>
          <meta http-equiv="content-type" content="text/html; charset=utf-8">
          <style>body { font-family: sans-serif; background-color: #ffffff; color: #000000; } a { color: #0000EE; } pre { background-color: #eeeeee; padding: 1em; white-space: pre-wrap; word-break: break-word; display: inline-block; } @media (prefers-color-scheme: dark) { body { background-color: #121212; color: #E0E0E0; } a { color: #BB86FC; } pre { background-color: #333333; color: #E0E0E0; } }</style>
          <title>Open source licenses</title>
        </head>
        <body>
          <h3>Notice for packages:</h3>
          <ul>
            <li><a href="#0">Fake dependency name</a>
              <dl>
                <dt>Copyright &copy; 2017 name</dt>
                <dd></dd>
              </dl>
            </li>
          </ul>
          <a id="0"></a>
          <pre>No license found</pre>
          <hr>
        </body>
      </html>
      """
    def actualJson = new File(reportFolder, "${taskName}.json")
    def expectedJson =
      """
      [
        {
          "project":"Fake dependency name",
          "description":"Fake dependency description",
          "version":"1.0.0",
          "developers":[
            "name"
          ],
          "url":"https://github.com/user/repo",
          "year":"2017",
          "licenses":[],
          "dependency":"group:name4:1.0.0"
        }
      ]
      """
    def actualText = new File(reportFolder, "${taskName}.txt")

    then:
    result.task(":${taskName}").outcome == SUCCESS
    result.output.find("Wrote CSV report to .*${reportFolder}/${taskName}.csv.")
    actualCsv.exists()
    result.output.find("Wrote HTML report to .*${reportFolder}/${taskName}.html.")
    actualHtml.exists()
    openSourceHtml.exists()
    result.output.find("Wrote JSON report to .*${reportFolder}/${taskName}.json.")
    actualJson.exists()
    result.output.find("Wrote Text report to .*${reportFolder}/${taskName}.txt.")
    actualText.exists()
    assertHtml(expectedHtml, actualHtml.text)
    assertJson(expectedJson, actualJson.text)

    where:
    taskName << ['licenseDebugReport', 'licenseReleaseReport']
  }

  @Unroll
  def '#taskName with default buildTypes, multi module and android and java'() {
    given:
    testProjectDir.newFile('settings.gradle') <<
      """
      include 'subproject'
      """

    buildFile <<
      """
      buildscript {
        dependencies {
          classpath files($classpathString)
        }
      }

      allprojects {
        repositories {
          maven {
            url '${mavenRepoUrl}'
          }
        }
      }

      apply plugin: 'com.android.application'
      apply plugin: 'com.jaredsburrows.license'

      android {
        compileSdkVersion $compileSdkVersion

        defaultConfig {
          applicationId 'com.example'
        }
      }

      dependencies {
        api project(':subproject')
        implementation 'group:name:1.0.0'
      }

      project(':subproject') {
        apply plugin: 'java-library'

        dependencies {
          implementation 'com.android.support:design:26.1.0'
        }
      }
      """

    when:
    def result = gradleWithCommand(testProjectDir.root, "${taskName}", '-s')
    def actualCsv = new File(reportFolder, "${taskName}.csv")
    def actualHtml = new File(reportFolder, "${taskName}.html")
    def openSourceHtml = new File(mainAssetsFolder, "open_source_licenses.html")
    def expectedHtml =
      """
      <!DOCTYPE html>
      <html lang="en">
        <head>
          <meta http-equiv="content-type" content="text/html; charset=utf-8">
          <style>body { font-family: sans-serif; background-color: #ffffff; color: #000000; } a { color: #0000EE; } pre { background-color: #eeeeee; padding: 1em; white-space: pre-wrap; word-break: break-word; display: inline-block; } @media (prefers-color-scheme: dark) { body { background-color: #121212; color: #E0E0E0; } a { color: #BB86FC; } pre { background-color: #333333; color: #E0E0E0; } }</style>
          <title>Open source licenses</title>
        </head>
        <body>
          <h3>Notice for packages:</h3>
          <ul>
            <li><a href="#1934118923">design</a>
              <dl>
                <dt>Copyright &copy; 20xx The original author or authors</dt>
                <dd></dd>
              </dl>
            </li>
          </ul>
          <a id="1934118923"></a>
          <pre>${getLicenseText('apache-2.0.txt')}</pre>
          <br>
          <hr>
          <ul>
            <li><a href="#-296292112">Fake dependency name</a>
              <dl>
                <dt>Copyright &copy; 2017 name</dt>
                <dd></dd>
              </dl>
            </li>
          </ul>
          <a id="-296292112"></a>
          <pre>Some license
          <a href="http://website.tld/">http://website.tld/</a></pre>
          <br>
          <hr>
        </body>
      </html>
      """
    def actualJson = new File(reportFolder, "${taskName}.json")
    def expectedJson =
      """
      [
        {
          "project":"design",
          "description":null,
          "version":"26.1.0",
          "developers":[],
          "url":null,
          "year":null,
          "licenses":[
            {
              "license":"The Apache Software License",
              "license_url":"http://www.apache.org/licenses/LICENSE-2.0.txt"
            }
          ],
          "dependency":"com.android.support:design:26.1.0"
        },
        {
          "project":"Fake dependency name",
          "description":"Fake dependency description",
          "version":"1.0.0",
          "developers":[
            "name"
          ],
          "url":"https://github.com/user/repo",
          "year":"2017",
          "licenses":[
            {
              "license":"Some license",
              "license_url":"http://website.tld/"
            }
          ],
          "dependency":"group:name:1.0.0"
        }
      ]
      """
    def actualText = new File(reportFolder, "${taskName}.txt")

    then:
    result.task(":${taskName}").outcome == SUCCESS
    result.output.find("Wrote CSV report to .*${reportFolder}/${taskName}.csv.")
    actualCsv.exists()
    result.output.find("Wrote HTML report to .*${reportFolder}/${taskName}.html.")
    actualHtml.exists()
    openSourceHtml.exists()
    result.output.find("Wrote JSON report to .*${reportFolder}/${taskName}.json.")
    actualJson.exists()
    result.output.find("Wrote Text report to .*${reportFolder}/${taskName}.txt.")
    actualText.exists()
    assertHtml(expectedHtml, actualHtml.text)
    assertJson(expectedJson, actualJson.text)

    where:
    taskName << ['licenseDebugReport', 'licenseReleaseReport']
  }

  @Unroll
  def '#taskName with default buildTypes, multi module and android and android'() {
    given:
    testProjectDir.newFile('settings.gradle') <<
      """
      include 'subproject'
      """

    buildFile <<
      """
      buildscript {
        dependencies {
          classpath files($classpathString)
        }
      }

      allprojects {
        repositories {
          maven {
            url '${mavenRepoUrl}'
          }
        }
      }

      apply plugin: 'com.android.application'
      apply plugin: 'com.jaredsburrows.license'

      android {
        compileSdkVersion $compileSdkVersion

        defaultConfig {
          applicationId 'com.example'
        }
      }

      dependencies {
        api project(':subproject')
        implementation 'group:name:1.0.0'
      }

      project(':subproject') {
        apply plugin: 'com.android.application'

        android {
          compileSdkVersion $compileSdkVersion
        }

        dependencies {
          implementation 'com.android.support:design:26.1.0'
        }
      }
      """

    when:
    def result = gradleWithCommand(testProjectDir.root, "${taskName}", '-s')
    def actualCsv = new File(reportFolder, "${taskName}.csv")
    def actualHtml = new File(reportFolder, "${taskName}.html")
    def openSourceHtml = new File(mainAssetsFolder, "open_source_licenses.html")
    def expectedHtml =
      """
      <!DOCTYPE html>
      <html lang="en">
        <head>
          <meta http-equiv="content-type" content="text/html; charset=utf-8">
          <style>body { font-family: sans-serif; background-color: #ffffff; color: #000000; } a { color: #0000EE; } pre { background-color: #eeeeee; padding: 1em; white-space: pre-wrap; word-break: break-word; display: inline-block; } @media (prefers-color-scheme: dark) { body { background-color: #121212; color: #E0E0E0; } a { color: #BB86FC; } pre { background-color: #333333; color: #E0E0E0; } }</style>
          <title>Open source licenses</title>
        </head>
        <body>
          <h3>Notice for packages:</h3>
          <ul>
            <li>
              <a href="#1934118923">design</a>
              <dl>
                <dt>Copyright &copy; 20xx The original author or authors</dt>
                <dd></dd>
              </dl>
            </li>
          </ul>
          <a id="1934118923"></a>
          <pre>${getLicenseText('apache-2.0.txt')}</pre>
          <br>
          <hr>
          <ul>
            <li>
              <a href="#-296292112">Fake dependency name</a>
              <dl>
                <dt>Copyright &copy; 2017 name</dt>
                <dd></dd>
              </dl>
            </li>
          </ul>
          <a id="-296292112"></a>
          <pre>Some license
            <a href="http://website.tld/">http://website.tld/</a>
          </pre>
          <br>
          <hr>
        </body>
      </html>
      """
    def actualJson = new File(reportFolder, "${taskName}.json")
    def expectedJson =
      """
      [
        {
          "project":"design",
          "description":null,
          "version":"26.1.0",
          "developers":[],
          "url":null,
          "year":null,
          "licenses":[
            {
              "license":"The Apache Software License",
              "license_url":"http://www.apache.org/licenses/LICENSE-2.0.txt"
            }
          ],
          "dependency":"com.android.support:design:26.1.0"
        },
        {
          "project":"Fake dependency name",
          "description":"Fake dependency description",
          "version":"1.0.0",
          "developers":[
            "name"
          ],
          "url":"https://github.com/user/repo",
          "year":"2017",
          "licenses":[
            {
              "license":"Some license",
              "license_url":"http://website.tld/"
            }
          ],
          "dependency":"group:name:1.0.0"
        }
      ]
      """
    def actualText = new File(reportFolder, "${taskName}.txt")

    then:
    result.task(":${taskName}").outcome == SUCCESS
    result.output.find("Wrote CSV report to .*${reportFolder}/${taskName}.csv.")
    actualCsv.exists()
    result.output.find("Wrote HTML report to .*${reportFolder}/${taskName}.html.")
    actualHtml.exists()
    openSourceHtml.exists()
    result.output.find("Wrote JSON report to .*${reportFolder}/${taskName}.json.")
    actualJson.exists()
    result.output.find("Wrote Text report to .*${reportFolder}/${taskName}.txt.")
    actualText.exists()
    assertHtml(expectedHtml, actualHtml.text)
    assertJson(expectedJson, actualJson.text)

    where:
    taskName << ['licenseDebugReport', 'licenseReleaseReport']
  }

  @Unroll
  def '#taskName with reports enabled and copy enabled #copyEnabled'() {
    given:
    buildFile <<
      """
      buildscript {
        dependencies {
          classpath files($classpathString)
        }
      }

      apply plugin: 'com.android.application'
      apply plugin: 'com.jaredsburrows.license'

      android {
        compileSdkVersion $compileSdkVersion

        defaultConfig {
          applicationId 'com.example'
        }
      }

      dependencies {
        implementation 'com.android.support:appcompat-v7:26.1.0'
        implementation 'com.android.support:appcompat-v7:26.1.0'
        implementation 'com.android.support:design:26.1.0'
      }

      licenseReport {
        generateCsvReport = true
        generateHtmlReport = true
        generateJsonReport = true
        generateTextReport = true

        copyCsvReportToAssets = "${copyEnabled}"
        copyHtmlReportToAssets = "${copyEnabled}"
        copyJsonReportToAssets = "${copyEnabled}"
        copyTextReportToAssets = "${copyEnabled}"
      }
      """

    when:
    def result = gradleWithCommand(testProjectDir.root, "${taskName}", '-s')
    def actualCsv = new File(reportFolder, "${taskName}.csv")
    def actualHtml = new File(reportFolder, "${taskName}.html")
    def openSourceHtml = new File(mainAssetsFolder, "open_source_licenses.html")
    def actualJson = new File(reportFolder, "${taskName}.json")
    def actualText = new File(reportFolder, "${taskName}.txt")

    then:
    result.task(":${taskName}").outcome == SUCCESS
    result.output.find("Wrote CSV report to .*${reportFolder}/${taskName}.csv.")
    actualCsv.exists()
    result.output.find("Wrote HTML report to .*${reportFolder}/${taskName}.html.")
    actualHtml.exists()
    result.output.find("Wrote JSON report to .*${reportFolder}/${taskName}.json.")
    actualJson.exists()
    result.output.find("Wrote Text report to .*${reportFolder}/${taskName}.txt.")
    actualText.exists()
    if (copyEnabled) {
      openSourceHtml.exists()
      result.output.find("Copied CSV report to .*${mainAssetsFolder}/open_source_licenses.csv.")
      result.output.find("Copied HTML report to .*${mainAssetsFolder}/open_source_licenses.html.")
      result.output.find("Copied JSON report to .*${mainAssetsFolder}/open_source_licenses.json.")
      result.output.find("Copied Text report to .*${mainAssetsFolder}/open_source_licenses.txt.")
    } else {
      !result.output.find("Copied CSV report to .*${mainAssetsFolder}/open_source_licenses.csv.")
      !result.output.find("Copied HTML report to .*${mainAssetsFolder}/open_source_licenses.html.")
      !result.output.find("Copied JSON report to .*${mainAssetsFolder}/open_source_licenses.json.")
      !result.output.find("Copied Text report to .*${mainAssetsFolder}/open_source_licenses.txt.")
    }

    where:
    taskName << ['licenseDebugReport', 'licenseReleaseReport']
    copyEnabled << [true, false]
  }

  @Unroll
  def '#taskName with reports disabled and copy enabled #copyEnabled'() {
    given:
    buildFile <<
      """
      buildscript {
        dependencies {
          classpath files($classpathString)
        }
      }

      apply plugin: 'com.android.application'
      apply plugin: 'com.jaredsburrows.license'

      android {
        compileSdkVersion $compileSdkVersion

        defaultConfig {
          applicationId 'com.example'
        }
      }

      dependencies {
        implementation 'com.android.support:appcompat-v7:26.1.0'
        implementation 'com.android.support:appcompat-v7:26.1.0'
        implementation 'com.android.support:design:26.1.0'
      }

      licenseReport {
        generateCsvReport = false
        generateHtmlReport = false
        generateJsonReport = false
        generateTextReport = false

        copyCsvReportToAssets = "${copyEnabled}"
        copyHtmlReportToAssets = "${copyEnabled}"
        copyJsonReportToAssets = "${copyEnabled}"
        copyTextReportToAssets = "${copyEnabled}"
      }
      """

    when:
    def result = gradleWithCommand(testProjectDir.root, "${taskName}", '-s')
    def actualCsv = new File(reportFolder, "${taskName}.csv")
    def actualHtml = new File(reportFolder, "${taskName}.html")
    def openSourceHtml = new File(mainAssetsFolder, "open_source_licenses.html")
    def actualJson = new File(reportFolder, "${taskName}.json")
    def actualText = new File(reportFolder, "${taskName}.txt")

    then:
    result.task(":${taskName}").outcome == SUCCESS
    !result.output.find("Wrote CSV report to .*${reportFolder}/${taskName}.csv.")
    !actualCsv.exists()
    !result.output.find("Wrote HTML report to .*${reportFolder}/${taskName}.html.")
    !actualHtml.exists()
    !openSourceHtml.exists()
    !result.output.find("Wrote JSON report to .*${reportFolder}/${taskName}.json.")
    !actualJson.exists()
    !result.output.find("Wrote Text report to .*${reportFolder}/${taskName}.txt.")
    !actualText.exists()
    !result.output.find("Copied CSV report to .*${mainAssetsFolder}/open_source_licenses.csv.")
    !result.output.find("Copied HTML report to .*${mainAssetsFolder}/open_source_licenses.html.")
    !result.output.find("Copied JSON report to .*${mainAssetsFolder}/open_source_licenses.json.")
    !result.output.find("Copied Text report to .*${mainAssetsFolder}/open_source_licenses.txt.")

    where:
    taskName << ['licenseDebugReport', 'licenseReleaseReport']
    copyEnabled << [true, false]
  }

  @Unroll
  def '#taskName with variant-specific report'() {
    given:
    buildFile <<
      """
      buildscript {
        dependencies {
          classpath files($classpathString)
        }
      }

      repositories {
        maven {
          url '${mavenRepoUrl}'
        }
      }

      apply plugin: 'com.android.application'
      apply plugin: 'com.jaredsburrows.license'

      android {
        compileSdkVersion $compileSdkVersion

        defaultConfig {
          applicationId 'com.example'
        }

        buildTypes {
          debug {}
          release {}
        }

        flavorDimensions 'version'
        productFlavors {
          paid {
            dimension "version"
          }
          free {
            dimension "version"
          }
        }
      }

      dependencies {
        implementation 'com.android.support:appcompat-v7:26.1.0'

        debugImplementation 'com.android.support:design:26.1.0'
        releaseImplementation 'com.android.support:design:26.1.0'
      }

      licenseReport {
        generateCsvReport = true
        generateHtmlReport = true
        generateJsonReport = true
        generateTextReport = true

        copyCsvReportToAssets = true
        copyHtmlReportToAssets = true
        copyJsonReportToAssets = true
        copyTextReportToAssets = true

        useVariantSpecificAssetDirs = true
      }
      """

    when:
    def result = gradleWithCommand(testProjectDir.root, "${taskName}", '-s')
    def variantName = taskName.replaceFirst(/^license/, '')
      .replaceFirst(/Report$/, '')
      .uncapitalize()
    def variantAssetFolder = "${srcFolder}/${variantName}/assets"
    def copiedCsv = new File(variantAssetFolder, "open_source_licenses.csv")
    def copiedHtml = new File(variantAssetFolder, "open_source_licenses.html")
    def copiedJson = new File(variantAssetFolder, "open_source_licenses.json")
    def copiedText = new File(variantAssetFolder, "open_source_licenses.txt")

    then:
    result.task(":${taskName}").outcome == SUCCESS
    copiedCsv.exists()
    result.output.find("Copied CSV report to .*${variantAssetFolder}/open_source_licenses.csv.")
    copiedHtml.exists()
    result.output.find("Copied HTML report to .*${variantAssetFolder}/open_source_licenses.html.")
    copiedJson.exists()
    result.output.find("Copied JSON report to .*${variantAssetFolder}/open_source_licenses.json.")
    copiedText.exists()
    result.output.find("Copied Text report to .*${variantAssetFolder}/open_source_licenses.txt.")

    where:
    taskName << [
      "licensePaidDebugReport",
      "licensePaidReleaseReport",
      "licenseFreeDebugReport",
      "licenseFreeReleaseReport"
    ]
  }

  @Unroll
  def '#taskName with android gradle plugin version < 7.1.0 (7.0.4)'() {
    given:
    def androidGradlePluginVersion = '7.0.4'
    buildFile <<
      """
      buildscript {
        repositories {
          mavenCentral()
          google()
        }

        dependencies {
          classpath files($classpathString)
          classpath 'com.android.tools.build:gradle:${androidGradlePluginVersion}'
        }
      }

      repositories {
        maven {
          url '${mavenRepoUrl}'
        }
      }

      apply plugin: 'com.android.application'
      apply plugin: 'com.jaredsburrows.license'

      android {
        compileSdkVersion $compileSdkVersion

        defaultConfig {
          applicationId 'com.example'
        }
      }

      dependencies {
        implementation 'com.android.support:design:26.1.0'
      }
      """

    when:
    def result = gradleWithCommand(testProjectDir.root, "${taskName}", '-s')
    def actualCsv = new File(reportFolder, "${taskName}.csv")
    def actualHtml = new File(reportFolder, "${taskName}.html")
    def openSourceHtml = new File(mainAssetsFolder, "open_source_licenses.html")
    def expectedHtml =
      """
      <!DOCTYPE html>
      <html lang="en">
        <head>
          <meta http-equiv="content-type" content="text/html; charset=utf-8">
          <style>body { font-family: sans-serif; background-color: #ffffff; color: #000000; } a { color: #0000EE; } pre { background-color: #eeeeee; padding: 1em; white-space: pre-wrap; word-break: break-word; display: inline-block; } @media (prefers-color-scheme: dark) { body { background-color: #121212; color: #E0E0E0; } a { color: #BB86FC; } pre { background-color: #333333; color: #E0E0E0; } }</style>
          <title>Open source licenses</title>
        </head>
        <body>
          <h3>Notice for packages:</h3>
          <ul>
            <li>
              <a href="#1934118923">design</a>
              <dl>
                <dt>Copyright &copy; 20xx The original author or authors</dt>
                <dd></dd>
              </dl>
            </li>
          </ul>
          <a id="1934118923"></a>
          <pre>${getLicenseText('apache-2.0.txt')}</pre>
          <br>
          <hr>
        </body>
      </html>
      """
    def actualJson = new File(reportFolder, "${taskName}.json")
    def expectedJson =
      """
      [
        {
          "project": "design",
          "description": null,
          "version": "26.1.0",
          "developers": [],
          "url": null,
          "year": null,
          "licenses": [
            {
              "license": "The Apache Software License",
              "license_url": "http://www.apache.org/licenses/LICENSE-2.0.txt"
            }
          ],
          "dependency": "com.android.support:design:26.1.0"
        }
      ]
      """
    def actualText = new File(reportFolder, "${taskName}.txt")

    then:
    result.task(":${taskName}").outcome == SUCCESS
    result.output.find("Wrote CSV report to .*${reportFolder}/${taskName}.csv.")
    actualCsv.exists()
    result.output.find("Wrote HTML report to .*${reportFolder}/${taskName}.html.")
    actualHtml.exists()
    openSourceHtml.exists()
    result.output.find("Wrote JSON report to .*${reportFolder}/${taskName}.json.")
    actualJson.exists()
    result.output.find("Wrote Text report to .*${reportFolder}/${taskName}.txt.")
    actualText.exists()
    assertHtml(expectedHtml, actualHtml.text)
    assertJson(expectedJson, actualJson.text)

    where:
    taskName << ['licenseDebugReport', 'licenseReleaseReport']
  }

  @Unroll
  def '#taskName with android gradle plugin version >= 7.1.0 (7.1.1)'() {
    given:
    def androidGradlePluginVersion = '7.1.1'
    buildFile <<
      """
      buildscript {
        repositories {
          mavenCentral()
          google()
        }

        dependencies {
          classpath files($classpathString)
          classpath 'com.android.tools.build:gradle:${androidGradlePluginVersion}'
        }
      }

      repositories {
        maven {
          url '${mavenRepoUrl}'
        }
      }

      apply plugin: 'com.android.application'
      apply plugin: 'com.jaredsburrows.license'

      android {
        compileSdkVersion $compileSdkVersion

        defaultConfig {
          applicationId 'com.example'
        }
      }

      dependencies {
        implementation 'com.android.support:design:26.1.0'
      }
      """

    when:
    def result = gradleWithCommand(testProjectDir.root, "${taskName}", '-s')
    def actualCsv = new File(reportFolder, "${taskName}.csv")
    def actualHtml = new File(reportFolder, "${taskName}.html")
    def openSourceHtml = new File(mainAssetsFolder, "open_source_licenses.html")
    def expectedHtml =
      """
      <!DOCTYPE html>
      <html lang="en">
        <head>
          <meta http-equiv="content-type" content="text/html; charset=utf-8">
          <style>body { font-family: sans-serif; background-color: #ffffff; color: #000000; } a { color: #0000EE; } pre { background-color: #eeeeee; padding: 1em; white-space: pre-wrap; word-break: break-word; display: inline-block; } @media (prefers-color-scheme: dark) { body { background-color: #121212; color: #E0E0E0; } a { color: #BB86FC; } pre { background-color: #333333; color: #E0E0E0; } }</style>
          <title>Open source licenses</title>
        </head>
        <body>
          <h3>Notice for packages:</h3>
          <ul>
            <li>
              <a href="#1934118923">design</a>
              <dl>
                <dt>Copyright &copy; 20xx The original author or authors</dt>
                <dd></dd>
              </dl>
            </li>
          </ul>
          <a id="1934118923"></a>
          <pre>${getLicenseText('apache-2.0.txt')}</pre>
          <br>
          <hr>
        </body>
      </html>
      """
    def actualJson = new File(reportFolder, "${taskName}.json")
    def expectedJson =
      """
      [
        {
          "project": "design",
          "description": null,
          "version": "26.1.0",
          "developers": [],
          "url": null,
          "year": null,
          "licenses": [
            {
              "license": "The Apache Software License",
              "license_url": "http://www.apache.org/licenses/LICENSE-2.0.txt"
            }
          ],
          "dependency": "com.android.support:design:26.1.0"
        }
      ]
      """
    def actualText = new File(reportFolder, "${taskName}.txt")

    then:
    result.task(":${taskName}").outcome == SUCCESS
    result.output.find("Wrote CSV report to .*${reportFolder}/${taskName}.csv.")
    actualCsv.exists()
    result.output.find("Wrote HTML report to .*${reportFolder}/${taskName}.html.")
    actualHtml.exists()
    openSourceHtml.exists()
    result.output.find("Wrote JSON report to .*${reportFolder}/${taskName}.json.")
    actualJson.exists()
    result.output.find("Wrote Text report to .*${reportFolder}/${taskName}.txt.")
    actualText.exists()
    assertHtml(expectedHtml, actualHtml.text)
    assertJson(expectedJson, actualJson.text)

    where:
    taskName << ['licenseDebugReport', 'licenseReleaseReport']
  }

  @Unroll
  def '#taskName ignoring one group ID pattern'() {
    given:
    buildFile <<
      """
      buildscript {
        dependencies {
          classpath files($classpathString)
        }
      }
      repositories {
        maven {
          url '${mavenRepoUrl}'
        }
      }
      apply plugin: 'com.android.application'
      apply plugin: 'com.jaredsburrows.license'
      android {
        compileSdkVersion $compileSdkVersion
        defaultConfig {
          applicationId 'com.example'
        }
      }
      dependencies {
        implementation 'pl.droidsonroids.gif:android-gif-drawable:1.2.3'
        implementation 'com.android.support:design:26.1.0'
      }

      licenseReport {
        ignoredPatterns = ["pl.droidsonroids.gif"]
      }
      """

    when:
    def result = gradleWithCommand(testProjectDir.root, "${taskName}", '-s')
    def actualHtml = new File(reportFolder, "${taskName}.html").text
    def expectedHtml =
      """
      <!DOCTYPE html>
      <html lang="en">
        <head><meta http-equiv="content-type" content="text/html; charset=utf-8">
          <style>body { font-family: sans-serif; background-color: #ffffff; color: #000000; } a { color: #0000EE; } pre { background-color: #eeeeee; padding: 1em; white-space: pre-wrap; word-break: break-word; display: inline-block; } @media (prefers-color-scheme: dark) { body { background-color: #121212; color: #E0E0E0; } a { color: #BB86FC; } pre { background-color: #333333; color: #E0E0E0; } }</style>
          <title>Open source licenses</title>
        </head>
        <body>
          <h3>Notice for packages:</h3>
          <ul>
            <li><a href="#1934118923">design</a>
              <dl>
                <dt>Copyright &copy; 20xx The original author or authors</dt>
                <dd></dd>
              </dl>
            </li>
          </ul>
          <a id="1934118923"></a>
          <pre>${getLicenseText('apache-2.0.txt')}</pre>
          <br>
          <hr>
        </body>
      </html>
      """
    def actualJson = new File(reportFolder, "${taskName}.json").text
    def expectedJson =
      """
      [
        {
          "project":"design",
          "description":null,
          "version":"26.1.0",
          "developers":[],
          "url":null,
          "year":null,
          "licenses":[
            {
              "license":"The Apache Software License",
              "license_url":"http://www.apache.org/licenses/LICENSE-2.0.txt"
            }
          ],
          "dependency":"com.android.support:design:26.1.0"
        }
      ]
      """

    then:
    result.task(":${taskName}").outcome == SUCCESS
    result.output.find("Wrote CSV report to .*${reportFolder}/${taskName}.csv.")
    result.output.find("Wrote HTML report to .*${reportFolder}/${taskName}.html.")
    result.output.find("Wrote JSON report to .*${reportFolder}/${taskName}.json.")
    assertHtml(expectedHtml, actualHtml)
    assertJson(expectedJson, actualJson)

    where:
    taskName << ['licenseDebugReport', 'licenseReleaseReport']
  }

  @Unroll
  def '#taskName ignoring all group ID patterns'() {
    given:
    buildFile <<
      """
      buildscript {
        dependencies {
          classpath files($classpathString)
        }
      }
      repositories {
        maven {
          url '${mavenRepoUrl}'
        }
      }
      apply plugin: 'com.android.application'
      apply plugin: 'com.jaredsburrows.license'
      android {
        compileSdkVersion $compileSdkVersion
        defaultConfig {
          applicationId 'com.example'
        }
      }
      dependencies {
        implementation 'pl.droidsonroids.gif:android-gif-drawable:1.2.3'
        implementation 'com.android.support:design:26.1.0'
      }
      licenseReport {
        ignoredPatterns = ["pl.droidsonroids.gif", "com.android.support"]
      }
      """

    when:
    def result = gradleWithCommand(testProjectDir.root, "${taskName}", '-s')
    def actualHtml = new File(reportFolder, "${taskName}.html").text
    def expectedHtml =
      """
      <!DOCTYPE html>
      <html lang="en">
        <head><meta http-equiv="content-type" content="text/html; charset=utf-8">
          <style>body { font-family: sans-serif; background-color: #ffffff; color: #000000; } a { color: #0000EE; } pre { background-color: #eeeeee; padding: 1em; white-space: pre-wrap; word-break: break-word; display: inline-block; } @media (prefers-color-scheme: dark) { body { background-color: #121212; color: #E0E0E0; } a { color: #BB86FC; } pre { background-color: #333333; color: #E0E0E0; } }</style>
          <title>Open source licenses</title>
        </head>
        <body>
          <h3>None</h3>
        </body>
      </html>
      """
    def actualJson = new File(reportFolder, "${taskName}.json").text
    def expectedJson =
      """
      []
      """

    then:
    result.task(":${taskName}").outcome == SUCCESS
    result.output.find("Wrote CSV report to .*${reportFolder}/${taskName}.csv.")
    result.output.find("Wrote HTML report to .*${reportFolder}/${taskName}.html.")
    result.output.find("Wrote JSON report to .*${reportFolder}/${taskName}.json.")
    assertHtml(expectedHtml, actualHtml)
    assertJson(expectedJson, actualJson)

    where:
    taskName << ['licenseDebugReport', 'licenseReleaseReport']
  }

  @Unroll
  def '#taskName ignoring one artifact ID pattern'() {
    given:
    buildFile <<
      """
      buildscript {
        dependencies {
          classpath files($classpathString)
        }
      }
      repositories {
        maven {
          url '${mavenRepoUrl}'
        }
      }
      apply plugin: 'com.android.application'
      apply plugin: 'com.jaredsburrows.license'
      android {
        compileSdkVersion $compileSdkVersion
        defaultConfig {
          applicationId 'com.example'
        }
      }
      dependencies {
        implementation 'pl.droidsonroids.gif:android-gif-drawable:1.2.3'
        implementation 'com.android.support:design:26.1.0'
      }
      licenseReport {
        ignoredPatterns = ["android-gif-drawable"]
      }
      """

    when:
    def result = gradleWithCommand(testProjectDir.root, "${taskName}", '-s')
    def actualHtml = new File(reportFolder, "${taskName}.html").text
    def expectedHtml =
      """
      <!DOCTYPE html>
      <html lang="en">
        <head><meta http-equiv="content-type" content="text/html; charset=utf-8">
          <style>body { font-family: sans-serif; background-color: #ffffff; color: #000000; } a { color: #0000EE; } pre { background-color: #eeeeee; padding: 1em; white-space: pre-wrap; word-break: break-word; display: inline-block; } @media (prefers-color-scheme: dark) { body { background-color: #121212; color: #E0E0E0; } a { color: #BB86FC; } pre { background-color: #333333; color: #E0E0E0; } }</style>
          <title>Open source licenses</title>
        </head>
        <body>
          <h3>Notice for packages:</h3>
          <ul>
            <li><a href="#1934118923">design</a>
              <dl>
                <dt>Copyright &copy; 20xx The original author or authors</dt>
                <dd></dd>
              </dl>
            </li>
          </ul>
          <a id="1934118923"></a>
          <pre>${getLicenseText('apache-2.0.txt')}</pre>
          <br>
          <hr>
        </body>
      </html>
      """
    def actualJson = new File(reportFolder, "${taskName}.json").text
    def expectedJson =
      """
      [
        {
          "project":"design",
          "description":null,
          "version":"26.1.0",
          "developers":[],
          "url":null,
          "year":null,
          "licenses":[
            {
              "license":"The Apache Software License",
              "license_url":"http://www.apache.org/licenses/LICENSE-2.0.txt"
            }
          ],
          "dependency":"com.android.support:design:26.1.0"
        }
      ]
      """

    then:
    result.task(":${taskName}").outcome == SUCCESS
    result.output.find("Wrote CSV report to .*${reportFolder}/${taskName}.csv.")
    result.output.find("Wrote HTML report to .*${reportFolder}/${taskName}.html.")
    result.output.find("Wrote JSON report to .*${reportFolder}/${taskName}.json.")
    assertHtml(expectedHtml, actualHtml)
    assertJson(expectedJson, actualJson)

    where:
    taskName << ['licenseDebugReport', 'licenseReleaseReport']
  }

  @Unroll
  def '#taskName ignoring all artifact ID patterns'() {
    given:
    buildFile <<
      """
      buildscript {
        dependencies {
          classpath files($classpathString)
        }
      }
      repositories {
        maven {
          url '${mavenRepoUrl}'
        }
      }
      apply plugin: 'com.android.application'
      apply plugin: 'com.jaredsburrows.license'
      android {
        compileSdkVersion $compileSdkVersion
        defaultConfig {
          applicationId 'com.example'
        }
      }
      dependencies {
        implementation 'pl.droidsonroids.gif:android-gif-drawable:1.2.3'
        implementation 'com.android.support:design:26.1.0'
      }
      licenseReport {
        ignoredPatterns = ["android-gif-drawable", "design"]
      }
      """

    when:
    def result = gradleWithCommand(testProjectDir.root, "${taskName}", '-s')
    def actualHtml = new File(reportFolder, "${taskName}.html").text
    def expectedHtml =
      """
      <!DOCTYPE html>
      <html lang="en">
        <head><meta http-equiv="content-type" content="text/html; charset=utf-8">
          <style>body { font-family: sans-serif; background-color: #ffffff; color: #000000; } a { color: #0000EE; } pre { background-color: #eeeeee; padding: 1em; white-space: pre-wrap; word-break: break-word; display: inline-block; } @media (prefers-color-scheme: dark) { body { background-color: #121212; color: #E0E0E0; } a { color: #BB86FC; } pre { background-color: #333333; color: #E0E0E0; } }</style>
          <title>Open source licenses</title>
        </head>
        <body>
          <h3>None</h3>
        </body>
      </html>
      """
    def actualJson = new File(reportFolder, "${taskName}.json").text
    def expectedJson =
      """
      []
      """

    then:
    result.task(":${taskName}").outcome == SUCCESS
    result.output.find("Wrote CSV report to .*${reportFolder}/${taskName}.csv.")
    result.output.find("Wrote HTML report to .*${reportFolder}/${taskName}.html.")
    result.output.find("Wrote JSON report to .*${reportFolder}/${taskName}.json.")
    assertHtml(expectedHtml, actualHtml)
    assertJson(expectedJson, actualJson)

    where:
    taskName << ['licenseDebugReport', 'licenseReleaseReport']
  }

  @Unroll
  def '#taskName ignoring multiple artifacts by group ID'() {
    given:
    buildFile <<
      """
      buildscript {
        dependencies {
          classpath files($classpathString)
        }
      }
      repositories {
        maven {
          url '${mavenRepoUrl}'
        }
      }
      apply plugin: 'com.android.application'
      apply plugin: 'com.jaredsburrows.license'
      android {
        compileSdkVersion $compileSdkVersion
        defaultConfig {
          applicationId 'com.example'
        }
      }
      dependencies {
        implementation 'pl.droidsonroids.gif:android-gif-drawable:1.2.3'
        implementation 'com.android.support:appcompat-v7:26.1.0'
        implementation 'com.android.support:design:26.1.0'
      }
      licenseReport {
        ignoredPatterns = ["com.android.support"]
      }
      """

    when:
    def result = gradleWithCommand(testProjectDir.root, "${taskName}", '-s')
    def actualHtml = new File(reportFolder, "${taskName}.html").text
    def expectedHtml =
      """
      <!DOCTYPE html>
      <html lang="en">
        <head><meta http-equiv="content-type" content="text/html; charset=utf-8">
          <style>body { font-family: sans-serif; background-color: #ffffff; color: #000000; } a { color: #0000EE; } pre { background-color: #eeeeee; padding: 1em; white-space: pre-wrap; word-break: break-word; display: inline-block; } @media (prefers-color-scheme: dark) { body { background-color: #121212; color: #E0E0E0; } a { color: #BB86FC; } pre { background-color: #333333; color: #E0E0E0; } }</style>
          <title>Open source licenses</title>
        </head>
        <body>
          <h3>Notice for packages:</h3>
          <ul>
            <li><a href="#1783810846">Android GIF Drawable Library</a>
              <dl>
                <dt>Copyright &copy; 20xx Karol WrXXtniak</dt>
                <dd></dd>
              </dl>
            </li>
          </ul>
          <a id="1783810846"></a>
          <pre>${getLicenseText('mit.txt')}</pre>
          <br>
          <hr>
        </body>
      </html>
      """
    def actualJson = new File(reportFolder, "${taskName}.json").text
    def expectedJson =
      """
      [
        {
          "project":"Android GIF Drawable Library",
          "description":"Views and Drawable for displaying animated GIFs for Android",
          "version":"1.2.3",
          "developers":[
            "Karol Wr\\u00c3\\u00b3tniak"
          ],
          "url":"https://github.com/koral--/android-gif-drawable",
          "year":null,
          "licenses":[
            {
              "license":"The MIT License",
              "license_url":"http://opensource.org/licenses/MIT"
            }
          ],
          "dependency":"pl.droidsonroids.gif:android-gif-drawable:1.2.3"
        }
      ]
      """

    then:
    result.task(":${taskName}").outcome == SUCCESS
    result.output.find("Wrote CSV report to .*${reportFolder}/${taskName}.csv.")
    result.output.find("Wrote HTML report to .*${reportFolder}/${taskName}.html.")
    result.output.find("Wrote JSON report to .*${reportFolder}/${taskName}.json.")
    assertHtml(expectedHtml, actualHtml)
    assertJson(expectedJson, actualJson)

    where:
    taskName << ['licenseDebugReport', 'licenseReleaseReport']
  }

  @Unroll
  def '#taskName ignoring specific artifact via group and artifact IDs'() {
    given:
    buildFile <<
      """
      buildscript {
        dependencies {
          classpath files($classpathString)
        }
      }
      repositories {
        maven {
          url '${mavenRepoUrl}'
        }
      }
      apply plugin: 'com.android.application'
      apply plugin: 'com.jaredsburrows.license'
      android {
        compileSdkVersion $compileSdkVersion
        defaultConfig {
          applicationId 'com.example'
        }
      }
      dependencies {
        implementation 'pl.droidsonroids.gif:android-gif-drawable:1.2.3'
        implementation 'com.android.support:design:26.1.0'
      }
      licenseReport {
        ignoredPatterns = ["pl.droidsonroids.gif:android-gif-drawable"]
      }
      """

    when:
    def result = gradleWithCommand(testProjectDir.root, "${taskName}", '-s')
    def actualHtml = new File(reportFolder, "${taskName}.html").text
    def expectedHtml =
      """
      <!DOCTYPE html>
      <html lang="en">
        <head><meta http-equiv="content-type" content="text/html; charset=utf-8">
          <style>body { font-family: sans-serif; background-color: #ffffff; color: #000000; } a { color: #0000EE; } pre { background-color: #eeeeee; padding: 1em; white-space: pre-wrap; word-break: break-word; display: inline-block; } @media (prefers-color-scheme: dark) { body { background-color: #121212; color: #E0E0E0; } a { color: #BB86FC; } pre { background-color: #333333; color: #E0E0E0; } }</style>
          <title>Open source licenses</title>
        </head>
        <body>
          <h3>Notice for packages:</h3>
          <ul>
            <li><a href="#1934118923">design</a>
              <dl>
                <dt>Copyright &copy; 20xx The original author or authors</dt>
                <dd></dd>
              </dl>
            </li>
          </ul>
          <a id="1934118923"></a>
          <pre>${getLicenseText('apache-2.0.txt')}</pre>
          <br>
          <hr>
        </body>
      </html>
      """
    def actualJson = new File(reportFolder, "${taskName}.json").text
    def expectedJson =
      """
      [
        {
          "project":"design",
          "description":null,
          "version":"26.1.0",
          "developers":[],
          "url":null,
          "year":null,
          "licenses":[
            {
              "license":"The Apache Software License",
              "license_url":"http://www.apache.org/licenses/LICENSE-2.0.txt"
            }
          ],
          "dependency":"com.android.support:design:26.1.0"
        }
      ]
      """

    then:
    result.task(":${taskName}").outcome == SUCCESS
    result.output.find("Wrote CSV report to .*${reportFolder}/${taskName}.csv.")
    result.output.find("Wrote HTML report to .*${reportFolder}/${taskName}.html.")
    result.output.find("Wrote JSON report to .*${reportFolder}/${taskName}.json.")
    assertHtml(expectedHtml, actualHtml)
    assertJson(expectedJson, actualJson)

    where:
    taskName << ['licenseDebugReport', 'licenseReleaseReport']
  }

  @Unroll
  def '#taskName ignoring artifact via version'() {
    given:
    buildFile <<
      """
      buildscript {
        dependencies {
          classpath files($classpathString)
        }
      }
      repositories {
        maven {
          url '${mavenRepoUrl}'
        }
      }
      apply plugin: 'com.android.application'
      apply plugin: 'com.jaredsburrows.license'
      android {
        compileSdkVersion $compileSdkVersion
        defaultConfig {
          applicationId 'com.example'
        }
      }
      dependencies {
        implementation 'pl.droidsonroids.gif:android-gif-drawable:1.2.3'
        implementation 'com.android.support:design:26.1.0'
      }
      licenseReport {
        ignoredPatterns = ["1.2.3"]
      }
      """

    when:
    def result = gradleWithCommand(testProjectDir.root, "${taskName}", '-s')
    def actualHtml = new File(reportFolder, "${taskName}.html").text
    def expectedHtml =
      """
      <!DOCTYPE html>
      <html lang="en">
        <head><meta http-equiv="content-type" content="text/html; charset=utf-8">
          <style>body { font-family: sans-serif; background-color: #ffffff; color: #000000; } a { color: #0000EE; } pre { background-color: #eeeeee; padding: 1em; white-space: pre-wrap; word-break: break-word; display: inline-block; } @media (prefers-color-scheme: dark) { body { background-color: #121212; color: #E0E0E0; } a { color: #BB86FC; } pre { background-color: #333333; color: #E0E0E0; } }</style>
          <title>Open source licenses</title>
        </head>
        <body>
          <h3>Notice for packages:</h3>
          <ul>
            <li><a href="#1934118923">design</a>
              <dl>
                <dt>Copyright &copy; 20xx The original author or authors</dt>
                <dd></dd>
              </dl>
            </li>
          </ul>
          <a id="1934118923"></a>
          <pre>${getLicenseText('apache-2.0.txt')}</pre>
          <br>
          <hr>
        </body>
      </html>
      """
    def actualJson = new File(reportFolder, "${taskName}.json").text
    def expectedJson =
      """
      [
        {
          "project":"design",
          "description":null,
          "version":"26.1.0",
          "developers":[],
          "url":null,
          "year":null,
          "licenses":[
            {
              "license":"The Apache Software License",
              "license_url":"http://www.apache.org/licenses/LICENSE-2.0.txt"
            }
          ],
          "dependency":"com.android.support:design:26.1.0"
        }
      ]
      """

    then:
    result.task(":${taskName}").outcome == SUCCESS
    result.output.find("Wrote CSV report to .*${reportFolder}/${taskName}.csv.")
    result.output.find("Wrote HTML report to .*${reportFolder}/${taskName}.html.")
    result.output.find("Wrote JSON report to .*${reportFolder}/${taskName}.json.")
    assertHtml(expectedHtml, actualHtml)
    assertJson(expectedJson, actualJson)

    where:
    taskName << ['licenseDebugReport', 'licenseReleaseReport']
  }

  @Unroll
  def '#taskName sorting by id when package name is the same'() {
    given:
    buildFile <<
      """
      buildscript {
        dependencies {
          classpath files($classpathString)
        }
      }
      repositories {
        maven {
          url '${mavenRepoUrl}'
        }
      }
      apply plugin: 'com.android.application'
      apply plugin: 'com.jaredsburrows.license'
      android {
        compileSdkVersion $compileSdkVersion
        defaultConfig {
          applicationId 'com.example'
        }
      }
      dependencies {
        implementation 'group:module-same-name-2:1.0.0'
        implementation 'group:module-same-name-1:1.0.0'
      }
      """

    when:
    def result = gradleWithCommand(testProjectDir.root, "${taskName}", '-s')
    def actualHtml = new File(reportFolder, "${taskName}.html").text
    def expectedHtml =
      """
      <!DOCTYPE html>
      <html lang="en">
        <head><meta http-equiv="content-type" content="text/html; charset=utf-8">
          <style>body { font-family: sans-serif; background-color: #ffffff; color: #000000; } a { color: #0000EE; } pre { background-color: #eeeeee; padding: 1em; white-space: pre-wrap; word-break: break-word; display: inline-block; } @media (prefers-color-scheme: dark) { body { background-color: #121212; color: #E0E0E0; } a { color: #BB86FC; } pre { background-color: #333333; color: #E0E0E0; } }</style>
          <title>Open source licenses</title>
        </head>
        <body>
          <h3>Notice for packages:</h3>
          <ul>
            <li><a href="#0">Module same name</a>
              <dl>
                <dt>Copyright &copy; 20xx The original author or authors</dt>
                <dd></dd>
              </dl>
            </li>
            <li><a href="#0">Module same name</a>
              <dl>
                <dt>Copyright &copy; 20xx The original author or authors</dt>
                <dd></dd>
              </dl>
            </li>
          </ul>
          <a id="0"></a>
          <pre>No license found</pre>
          <hr>
        </body>
      </html>
      """
    def actualJson = new File(reportFolder, "${taskName}.json").text
    def expectedJson =
      """
      [
        {
          "project":"Module same name",
          "description":null,
          "version":"1.0.0",
          "developers":[],
          "url":null,
          "year":null,
          "licenses":[],
          "dependency":"group:module-same-name-1:1.0.0"
        },
        {
          "project":"Module same name",
          "description":null,
          "version":"1.0.0",
          "developers":[],
          "url":null,
          "year":null,
          "licenses":[],
          "dependency":"group:module-same-name-2:1.0.0"
        }
      ]
      """

    then:
    result.task(":${taskName}").outcome == SUCCESS
    result.output.find("Wrote CSV report to .*${reportFolder}/${taskName}.csv.")
    result.output.find("Wrote HTML report to .*${reportFolder}/${taskName}.html.")
    result.output.find("Wrote JSON report to .*${reportFolder}/${taskName}.json.")
    assertHtml(expectedHtml, actualHtml)
    assertJson(expectedJson, actualJson)

    where:
    taskName << ['licenseDebugReport', 'licenseReleaseReport']
  }
}
