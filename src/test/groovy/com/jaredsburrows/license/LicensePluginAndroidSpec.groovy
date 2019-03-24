package com.jaredsburrows.license

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS
import static test.TestUtils.getLicenseText

import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import spock.lang.Unroll

final class LicensePluginAndroidSpec extends Specification {
  @Rule public TemporaryFolder testProjectDir = new TemporaryFolder()
  private List<File> pluginClasspath
  private String mavenRepoUrl
  private File buildFile
  private String reportFolder

  def 'setup'() {
    def pluginClasspathResource = getClass().classLoader.findResource('plugin-classpath.txt')
    if (pluginClasspathResource == null) {
      throw new IllegalStateException(
        'Did not find plugin classpath resource, run `testClasses` build task.')
    }

    pluginClasspath = pluginClasspathResource.readLines().collect { new File(it) }
    mavenRepoUrl = getClass().getResource('/maven').toURI()
    buildFile = testProjectDir.newFile('build.gradle')
    reportFolder = "${testProjectDir.root.path}/build/reports/licenses"
  }

  @Unroll def 'licenseDebugReport with gradle #gradleVersion and android gradle plugin #agpVersion'() {
    given:
    def classpathString = pluginClasspath
      .collect { it.absolutePath.replace('\\', '\\\\') } // escape backslashes in Windows paths
      .collect { "'$it'" }
      .join(", ")

    buildFile <<
      """
        buildscript {
          repositories {
            google()
            jcenter()
            mavenCentral()
          }

          dependencies {
            classpath "com.android.tools.build:gradle:${agpVersion}"
            classpath files($classpathString)
          }
        }
        
        repositories {
          google()
          jcenter()
          mavenCentral()
        }

        apply plugin: 'com.android.application'
        apply plugin: 'com.jaredsburrows.license'

        android {
          compileSdkVersion 28

          defaultConfig {
            applicationId 'com.example'
          }
        }
      """.stripIndent().trim()

    when:
    def result = GradleRunner.create()
      .withGradleVersion(gradleVersion)
      .withProjectDir(testProjectDir.root)
      .withArguments('licenseDebugReport')
      .build()

    then:
    result.task(':licenseDebugReport').outcome == SUCCESS
    result.output.find("Wrote HTML report to .*${reportFolder}/licenseDebugReport.html.")
    result.output.find("Wrote JSON report to .*${reportFolder}/licenseDebugReport.json.")

    where:
    [gradleVersion, agpVersion] << [
      [
        '4.6',
        '4.10',
        '5.0',
        '5.2'
      ],
      [
        '3.0.0',
        '3.3.0'
      ]
    ].combinations()
  }

  @Unroll def '#taskName that has no dependencies'() {
    given:
    def classpathString = pluginClasspath
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

        apply plugin: 'com.android.application'
        apply plugin: 'com.jaredsburrows.license'

        android {
          compileSdkVersion 28

          defaultConfig {
            applicationId 'com.example'
          }
        }
      """.stripIndent().trim()

    when:
    def result = GradleRunner.create()
      .withProjectDir(testProjectDir.root)
      .withArguments("${taskName}")
      .build()

    then:
    result.task(":${taskName}").outcome == SUCCESS
    result.output.find("Wrote HTML report to .*${reportFolder}/${taskName}.html.")
    result.output.find("Wrote JSON report to .*${reportFolder}/${taskName}.json.")

    def actualHtml = new File("${reportFolder}/${taskName}.html").text.stripIndent().trim()
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
    def actualJson = new File("${reportFolder}/${taskName}.json").text.stripIndent().trim()
    def expectedJson =
      """
[]
""".stripIndent().trim()

    actualHtml == expectedHtml
    actualJson == expectedJson

    where:
    taskName << ['licenseDebugReport', 'licenseReleaseReport']
  }

  @Unroll def '#taskName with default buildTypes'() {
    given:
    def classpathString = pluginClasspath
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
      """.stripIndent().trim()

    when:
    def result = GradleRunner.create()
      .withProjectDir(testProjectDir.root)
      .withArguments("${taskName}")
      .build()

    then:
    result.task(":${taskName}").outcome == SUCCESS
    result.output.find("Wrote HTML report to .*${reportFolder}/${taskName}.html.")
    result.output.find("Wrote JSON report to .*${reportFolder}/${taskName}.json.")

    def actualHtml = new File("${reportFolder}/${taskName}.html").text.stripIndent().trim()
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
        <a href='#314129783'>Appcompat-v7</a>
      </li>
      <li>
        <a href='#314129783'>Design</a>
      </li>
      <a name='314129783' />
      <pre>${getLicenseText('apache-2.0.txt')}</pre>
    </ul>
  </body>
</html>
""".stripIndent().trim()
    def actualJson = new File("${reportFolder}/${taskName}.json").text.stripIndent().trim()
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
        "dependency": "com.android.support:appcompat-v7:26.1.0"
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
        "dependency": "com.android.support:design:26.1.0"
    }
]
""".stripIndent().trim()

    actualHtml == expectedHtml
    actualJson == expectedJson

    where:
    taskName << ['licenseDebugReport', 'licenseReleaseReport']
  }

  @Unroll def '#taskName with buildTypes'() {
    given:
    def classpathString = pluginClasspath
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
      """.stripIndent().trim()

    when:
    def result = GradleRunner.create()
      .withProjectDir(testProjectDir.root)
      .withArguments("${taskName}")
      .build()

    then:
    result.task(":${taskName}").outcome == SUCCESS
    result.output.find("Wrote HTML report to .*${reportFolder}/${taskName}.html.")
    result.output.find("Wrote JSON report to .*${reportFolder}/${taskName}.json.")

    def actualHtml = new File("${reportFolder}/${taskName}.html").text.stripIndent().trim()
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
        <a href='#314129783'>Appcompat-v7</a>
      </li>
      <li>
        <a href='#314129783'>Design</a>
      </li>
      <a name='314129783' />
      <pre>${getLicenseText('apache-2.0.txt')}</pre>
    </ul>
  </body>
</html>
""".stripIndent().trim()
    def actualJson = new File("${reportFolder}/${taskName}.json").text.stripIndent().trim()
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
        "dependency": "com.android.support:appcompat-v7:26.1.0"
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
        "dependency": "com.android.support:design:26.1.0"
    }
]
""".stripIndent().trim()

    actualHtml == expectedHtml
    actualJson == expectedJson

    where:
    taskName << ['licenseDebugReport', 'licenseReleaseReport']
  }

  @Unroll def '#taskName with buildTypes + productFlavors + flavorDimensions'() {
    given:
    def classpathString = pluginClasspath
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
      """.stripIndent().trim()

    when:
    def result = GradleRunner.create()
      .withProjectDir(testProjectDir.root)
      .withArguments("${taskName}")
      .build()

    then:
    result.task(":${taskName}").outcome == SUCCESS
    result.output.find("Wrote HTML report to .*${reportFolder}/${taskName}.html.")
    result.output.find("Wrote JSON report to .*${reportFolder}/${taskName}.json.")

    def actualHtml = new File("${reportFolder}/${taskName}.html").text.stripIndent().trim()
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
        <a href='#314129783'>Appcompat-v7</a>
      </li>
      <li>
        <a href='#314129783'>Design</a>
      </li>
      <li>
        <a href='#314129783'>Support-annotations</a>
      </li>
      <li>
        <a href='#314129783'>Support-v4</a>
      </li>
      <a name='314129783' />
      <pre>${getLicenseText('apache-2.0.txt')}</pre>
    </ul>
  </body>
</html>
""".stripIndent().trim()
    def actualJson = new File("${reportFolder}/${taskName}.json").text.stripIndent().trim()
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
        "dependency": "com.android.support:appcompat-v7:26.1.0"
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
        "dependency": "com.android.support:design:26.1.0"
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
        "dependency": "com.android.support:support-annotations:26.1.0"
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
        "dependency": "com.android.support:support-v4:26.1.0"
    }
]
""".stripIndent().trim()

    actualHtml == expectedHtml
    actualJson == expectedJson

    where:
    taskName << ['licenseFlavor1Flavor3DebugReport', 'licenseFlavor1Flavor3ReleaseReport',
                 'licenseFlavor2Flavor4DebugReport', 'licenseFlavor2Flavor4ReleaseReport']
  }

  @Unroll def '#taskName from readme example'() {
    given:
    def classpathString = pluginClasspath
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
      """.stripIndent().trim()

    when:
    def result = GradleRunner.create()
      .withProjectDir(testProjectDir.root)
      .withArguments("${taskName}")
      .build()

    then:
    result.task(":${taskName}").outcome == SUCCESS
    result.output.find("Wrote HTML report to .*${reportFolder}/${taskName}.html.")
    result.output.find("Wrote JSON report to .*${reportFolder}/${taskName}.json.")

    def actualHtml = new File("${reportFolder}/${taskName}.html").text.stripIndent().trim()
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
        <a href='#1068328410'>Android GIF Drawable Library</a>
      </li>
      <a name='1068328410' />
      <pre>${getLicenseText('mit.txt')}</pre>
      <li>
        <a href='#314129783'>Design</a>
      </li>
      <a name='314129783' />
      <pre>${getLicenseText('apache-2.0.txt')}</pre>
    </ul>
  </body>
</html>
""".stripIndent().trim()
    def actualJson = new File("${reportFolder}/${taskName}.json").text.stripIndent().trim()
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
        "dependency": "pl.droidsonroids.gif:android-gif-drawable:1.2.3"
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
        "dependency": "com.android.support:design:26.1.0"
    }
]
""".stripIndent().trim()

    actualHtml == expectedHtml
    actualJson == expectedJson

    where:
    taskName << ['licenseDebugReport', 'licenseReleaseReport']
  }

  @Unroll def '#taskName with no open source dependencies'() {
    given:
    def classpathString = pluginClasspath
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
    def result = GradleRunner.create()
      .withProjectDir(testProjectDir.root)
      .withArguments("${taskName}")
      .build()

    then:
    result.task(":${taskName}").outcome == SUCCESS
    result.output.find("Wrote HTML report to .*${reportFolder}/${taskName}.html.")
    result.output.find("Wrote JSON report to .*${reportFolder}/${taskName}.json.")

    def actualHtml = new File("${reportFolder}/${taskName}.html").text.stripIndent().trim()
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
        <a href='#0'>Fake dependency name</a>
      </li>
      <a name='0' />
      <pre>No license found</pre>
    </ul>
  </body>
</html>
""".stripIndent().trim()
    def actualJson = new File("${reportFolder}/${taskName}.json").text.stripIndent().trim()
    def expectedJson =
      """
[
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
            
        ],
        "dependency": "group:name4:1.0.0"
    }
]
""".stripIndent().trim()

    then:
    actualHtml == expectedHtml
    actualJson == expectedJson

    where:
    taskName << ['licenseDebugReport', 'licenseReleaseReport']
  }

  @Unroll def '#taskName with default buildTypes, multi module and android and java'() {
    given:
    def classpathString = pluginClasspath
      .collect { it.absolutePath.replace('\\', '\\\\') } // escape backslashes in Windows paths
      .collect { "'$it'" }
      .join(", ")

    testProjectDir.newFile('settings.gradle') << """
include 'subproject'
        """

    buildFile << """
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
    def result = GradleRunner.create()
      .withProjectDir(testProjectDir.root)
      .withArguments("${taskName}")
      .build()

    then:
    result.task(":${taskName}").outcome == SUCCESS
    result.output.find("Wrote HTML report to .*${reportFolder}/${taskName}.html.")
    result.output.find("Wrote JSON report to .*${reportFolder}/${taskName}.json.")

    def actualHtml = new File("${reportFolder}/${taskName}.html").text.stripIndent().trim()
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
        <a href='#314129783'>Design</a>
      </li>
      <a name='314129783' />
      <pre>${getLicenseText('apache-2.0.txt')}</pre>
      <li>
        <a href='#755498312'>Fake dependency name</a>
      </li>
      <a name='755498312' />
      <pre>Some license
<a href='http://website.tld/'>http://website.tld/</a></pre>
    </ul>
  </body>
</html>
""".stripIndent().trim()
    def actualJson = new File("${reportFolder}/${taskName}.json").text.stripIndent().trim()
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
}
