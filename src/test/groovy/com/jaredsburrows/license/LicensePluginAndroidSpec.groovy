package com.jaredsburrows.license

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS
import static test.TestUtils.assertHtml
import static test.TestUtils.assertJson
import static test.TestUtils.gradleWithCommand
import static test.TestUtils.myGetLicenseText

import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import spock.lang.Unroll

final class LicensePluginAndroidSpec extends Specification {
  @Rule public TemporaryFolder testProjectDir = new TemporaryFolder()
  private List<File> pluginClasspath
  private String classpathString
  private String mavenRepoUrl
  private File buildFile
  private String reportFolder
  private String assetsFolder

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
    assetsFolder = "${testProjectDir.root.path.replaceAll('\\\\', '/')}/src/main/assets"
  }

  @Unroll def 'licenseDebugReport with gradle #gradleVersion and android gradle plugin #agpVersion'() {
    given:
    buildFile <<
      """
      buildscript {
        repositories {
          mavenCentral()
          jcenter() // Remove when https://github.com/Kotlin/kotlinx.html/issues/173 is fixed
          google()
        }

        dependencies {
          classpath "com.android.tools.build:gradle:${agpVersion}"
          classpath files($classpathString)
        }
      }

      apply plugin: 'com.android.application'
      apply plugin: 'com.jaredsburrows.license'

      android {
        compileSdkVersion 28

        defaultConfig {
          applicationId 'com.example'
        }
      }
      """

    when:
    def result = GradleRunner.create()
      .withGradleVersion(gradleVersion)
      .withProjectDir(testProjectDir.root)
      .withArguments('licenseDebugReport', '-s')
      .build()

    then:
    result.task(':licenseDebugReport').outcome == SUCCESS
    result.output.find("Wrote CSV report to .*${reportFolder}/licenseDebugReport.csv.")
    result.output.find("Wrote HTML report to .*${reportFolder}/licenseDebugReport.html.")
    result.output.find("Wrote JSON report to .*${reportFolder}/licenseDebugReport.json.")

    where:
    [gradleVersion, agpVersion] << [
      [
        '5.6.4',
        '6.1.1'
      ],
      [
        '3.5.0',
        '3.6.0',
        '4.0.0'
      ]
    ].combinations()
  }

  @Unroll def '#taskName that has no dependencies'() {
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
        compileSdkVersion 28

        defaultConfig {
          applicationId 'com.example'
        }
      }
      """

    when:
    def result = gradleWithCommand(testProjectDir.root, "${taskName}", '-s')
    def actualHtml = new File("${reportFolder}/${taskName}.html").text
    def expectedHtml =
      """
      <html>
        <head>
          <style>
            body { font-family: sans-serif }
            pre { background-color: #eeeeee; padding: 1em; white-space: pre-wrap; display: inline-block }
          </style>
          <title>Open source licenses</title>
        </head>
        <body>
          <h3>None</h3>
        </body>
      </html>
      """
    def actualJson = new File("${reportFolder}/${taskName}.json").text
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

  @Unroll def '#taskName with default buildTypes'() {
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
        compileSdkVersion 28

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
    def actualHtml = new File("${reportFolder}/${taskName}.html").text
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
            <li><a href="#1934118923">appcompat-v7 (26.1.0)</a>
              <dl>
                <dt>Copyright &copy; 20xx The original author or authors</dt>
              </dl>
            </li>
            <li><a href="#1934118923">design (26.1.0)</a>
              <dl>
                <dt>Copyright &copy; 20xx The original author or authors</dt>
              </dl>
            </li>
      <a name="1934118923"></a>
            <pre>${myGetLicenseText('apache-2.0.txt')}</pre>
      <br>
            <hr>
          </ul>
        </body>
      </html>
      """
    def actualJson = new File("${reportFolder}/${taskName}.json").text
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

  @Unroll def '#taskName with buildTypes'() {
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
        compileSdkVersion 28

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
    def actualHtml = new File("${reportFolder}/${taskName}.html").text
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
            <li><a href="#1934118923">appcompat-v7 (26.1.0)</a>
              <dl>
                <dt>Copyright &copy; 20xx The original author or authors</dt>
              </dl>
            </li>
            <li><a href="#1934118923">design (26.1.0)</a>
              <dl>
                <dt>Copyright &copy; 20xx The original author or authors</dt>
              </dl>
            </li>
      <a name="1934118923"></a>
            <pre>${myGetLicenseText('apache-2.0.txt')}</pre>
      <br>
            <hr>
          </ul>
        </body>
      </html>
      """
    def actualJson = new File("${reportFolder}/${taskName}.json").text
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

  @Unroll def '#taskName with buildTypes + productFlavors + flavorDimensions'() {
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
        compileSdkVersion 28

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
    def actualHtml = new File("${reportFolder}/${taskName}.html").text
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
            <li><a href="#1934118923">appcompat-v7 (26.1.0)</a>
              <dl>
                <dt>Copyright &copy; 20xx The original author or authors</dt>
              </dl>
            </li>
            <li><a href="#1934118923">design (26.1.0)</a>
              <dl>
                <dt>Copyright &copy; 20xx The original author or authors</dt>
              </dl>
            </li>
            <li><a href="#1934118923">support-annotations (26.1.0)</a>
              <dl>
                <dt>Copyright &copy; 20xx The original author or authors</dt>
              </dl>
            </li>
            <li><a href="#1934118923">support-v4 (26.1.0)</a>
              <dl>
                <dt>Copyright &copy; 20xx The original author or authors</dt>
              </dl>
            </li>
      <a name="1934118923"></a>
            <pre>${myGetLicenseText('apache-2.0.txt')}</pre>
      <br>
            <hr>
          </ul>
        </body>
      </html>
      """
    def actualJson = new File("${reportFolder}/${taskName}.json").text
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

    then:
    result.task(":${taskName}").outcome == SUCCESS
    result.output.find("Wrote CSV report to .*${reportFolder}/${taskName}.csv.")
    result.output.find("Wrote HTML report to .*${reportFolder}/${taskName}.html.")
    result.output.find("Wrote JSON report to .*${reportFolder}/${taskName}.json.")
    assertHtml(expectedHtml, actualHtml)
    assertJson(expectedJson, actualJson)

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
        compileSdkVersion 28

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

  @Unroll def '#taskName from readme example'() {
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
        compileSdkVersion 28

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
    def actualHtml = new File("${reportFolder}/${taskName}.html").text
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
            <li><a href="#1934118923">design (26.1.0)</a>
              <dl>
                <dt>Copyright &copy; 20xx The original author or authors</dt>
              </dl>
            </li>
      <a name="1934118923"></a>
            <pre>${myGetLicenseText('apache-2.0.txt')}</pre>
      <br>
            <hr>
            <li><a href="#1783810846">Android GIF Drawable Library (1.2.3)</a>
              <dl>
                <dt>Copyright &copy; 20xx Karol WrXXtniak</dt>
              </dl>
            </li>
      <a name="1783810846"></a>
            <pre>${myGetLicenseText('mit.txt')}</pre>
      <br>
            <hr>
          </ul>
        </body>
      </html>
      """
    def actualJson = new File("${reportFolder}/${taskName}.json").text
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

  @Unroll def '#taskName with no open source dependencies'() {
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
          compileSdkVersion 28

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
    def actualHtml = new File("${reportFolder}/${taskName}.html").text
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
            <li><a href="#0">Fake dependency name (1.0.0)</a>
              <dl>
                <dt>Copyright &copy; 2017 name</dt>
              </dl>
            </li>
      <a name="0"></a>
            <pre>No license found</pre>
            <hr>
          </ul>
        </body>
      </html>
      """
    def actualJson = new File("${reportFolder}/${taskName}.json").text
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

  @Unroll def '#taskName with default buildTypes, multi module and android and java'() {
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
        compileSdkVersion 28

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
    def actualHtml = new File("${reportFolder}/${taskName}.html").text
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
            <li><a href="#1934118923">design (26.1.0)</a>
              <dl>
                <dt>Copyright &copy; 20xx The original author or authors</dt>
              </dl>
            </li>
      <a name="1934118923"></a>
            <pre>${myGetLicenseText('apache-2.0.txt')}</pre>
      <br>
            <hr>
            <li><a href="#-296292112">Fake dependency name (1.0.0)</a>
              <dl>
                <dt>Copyright &copy; 2017 name</dt>
              </dl>
            </li>
      <a name="-296292112"></a>
            <pre>Some license
      <a href="http://website.tld/">http://website.tld/</a></pre>
      <br>
            <hr>
          </ul>
        </body>
      </html>
      """
    def actualJson = new File("${reportFolder}/${taskName}.json").text
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

  @Unroll def '#taskName with default buildTypes, multi module and android and android'() {
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
        compileSdkVersion 28

        defaultConfig {
          applicationId 'com.example'
        }
      }

      dependencies {
        api project(':subproject')
        implementation 'group:name:1.0.0'
      }

      project(':subproject') {
        apply plugin: 'com.android.library'

        android {
          compileSdkVersion 28
        }

        dependencies {
          implementation 'com.android.support:design:26.1.0'
        }
      }
      """

    when:
    def result = gradleWithCommand(testProjectDir.root, "${taskName}", '-s')
    def actualHtml = new File("${reportFolder}/${taskName}.html").text
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
            <li><a href="#1934118923">design (26.1.0)</a>
              <dl>
                <dt>Copyright &copy; 20xx The original author or authors</dt>
              </dl>
            </li>
      <a name="1934118923"></a>
            <pre>${myGetLicenseText('apache-2.0.txt')}</pre>
      <br>
            <hr>
            <li><a href="#-296292112">Fake dependency name (1.0.0)</a>
              <dl>
                <dt>Copyright &copy; 2017 name</dt>
              </dl>
            </li>
      <a name="-296292112"></a>
            <pre>Some license
      <a href="http://website.tld/">http://website.tld/</a></pre>
      <br>
            <hr>
          </ul>
        </body>
      </html>
      """
    def actualJson = new File("${reportFolder}/${taskName}.json").text
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

    then:
    result.task(":${taskName}").outcome == SUCCESS
    result.output.find("Wrote HTML report to .*${reportFolder}/${taskName}.html.")
    result.output.find("Wrote JSON report to .*${reportFolder}/${taskName}.json.")
    assertHtml(expectedHtml, actualHtml)
    assertJson(expectedJson, actualJson)

    where:
    taskName << ['licenseDebugReport', 'licenseReleaseReport']
  }

  @Unroll def '#taskName with reports enabled and copy enabled #copyEnabled'() {
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
        compileSdkVersion 28

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
        generateHtmlReport = true
        generateJsonReport = true
        copyHtmlReportToAssets = "${copyEnabled}"
        copyJsonReportToAssets = "${copyEnabled}"
      }
      """

    when:
    def result = gradleWithCommand(testProjectDir.root, "${taskName}", '-s')

    then:
    result.task(":${taskName}").outcome == SUCCESS
    result.output.find("Wrote CSV report to .*${reportFolder}/${taskName}.csv.")
    result.output.find("Wrote HTML report to .*${reportFolder}/${taskName}.html.")
    result.output.find("Wrote JSON report to .*${reportFolder}/${taskName}.json.")
    result.output.find("Copied HTML report to .*${assetsFolder}/open_source_licenses.html.")
    result.output.find("Copied JSON report to .*${assetsFolder}/open_source_licenses.json.")

    where:
    taskName << ['licenseDebugReport', 'licenseReleaseReport']
    copyEnabled << [true, false]
  }

  @Unroll def '#taskName with reports disabled and copy enabled #copyEnabled'() {
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
        compileSdkVersion 28

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
        generateHtmlReport = false
        generateJsonReport = false
        copyHtmlReportToAssets = "${copyEnabled}"
        copyJsonReportToAssets = "${copyEnabled}"
      }
      """

    when:
    def result = gradleWithCommand(testProjectDir.root, "${taskName}", '-s')

    then:
    result.task(":${taskName}").outcome == SUCCESS
    result.output.find("Wrote CSV report to .*${reportFolder}/${taskName}.csv.")
    !result.output.find("Wrote HTML report to .*${reportFolder}/${taskName}.html.")
    !result.output.find("Wrote JSON report to .*${reportFolder}/${taskName}.json.")
    !result.output.find("Copied HTML report to .*${assetsFolder}/open_source_licenses.html.")
    !result.output.find("Copied JSON report to .*${assetsFolder}/open_source_licenses.json.")

    where:
    taskName << ['licenseDebugReport', 'licenseReleaseReport']
    copyEnabled << [true, false]
  }
}
