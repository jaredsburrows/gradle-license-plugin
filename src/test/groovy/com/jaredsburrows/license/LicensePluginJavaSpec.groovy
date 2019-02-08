package com.jaredsburrows.license

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS
import static test.TestUtils.getLicenseText

import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import spock.lang.Unroll

final class LicensePluginJavaSpec extends Specification {
  @Rule public TemporaryFolder testProjectDir = new TemporaryFolder()
  private String mavenRepoUrl
  private File buildFile
  private String reportFolder

  def 'setup'() {
    mavenRepoUrl = getClass().getResource('/maven').toURI()
    buildFile = testProjectDir.newFile('build.gradle')
    reportFolder = "${testProjectDir.root.path}/build/reports/licenses"
  }

  @Unroll def 'licenseReport using with gradle #gradleVersion'() {
    given:
    buildFile <<
      """
        plugins {
          id 'java'
          id 'com.jaredsburrows.license'
        }
      """

    when:
    def result = GradleRunner.create()
      .withGradleVersion(gradleVersion)
      .withProjectDir(testProjectDir.root)
      .withArguments('licenseReport')
      .withPluginClasspath()
      .build()

    then:
    result.task(':licenseReport').outcome == SUCCESS
    result.output.find("Wrote HTML report to .*${reportFolder}/licenseReport.html.")
    result.output.find("Wrote JSON report to .*${reportFolder}/licenseReport.json.")

    where:
    gradleVersion << [
      '3.5',
      '4.0',
      '4.5',
      '4.10',
      '5.0',
      '5.1',
      '5.2'
    ]
  }

  def 'licenseReport with no dependencies'() {
    given:
    buildFile <<
      """
        plugins {
          id 'java-library'
          id 'com.jaredsburrows.license'
        }
      """

    when:
    def result = GradleRunner.create()
      .withProjectDir(testProjectDir.root)
      .withArguments('licenseReport')
      .withPluginClasspath()
      .build()

    then:
    result.task(':licenseReport').outcome == SUCCESS
    result.output.find("Wrote HTML report to .*${reportFolder}/licenseReport.html.")
    result.output.find("Wrote JSON report to .*${reportFolder}/licenseReport.json.")

    def actualHtml = new File("${reportFolder}/licenseReport.html").text.stripIndent().trim()
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
    def actualJson = new File("${reportFolder}/licenseReport.json").text.stripIndent().trim()
    def expectedJson =
      """
[]
""".stripIndent().trim()

    actualHtml == expectedHtml
    actualJson == expectedJson
  }

  def 'licenseReport with no open source dependencies'() {
    given:
    buildFile <<
      """
        plugins {
          id 'java-library'
          id 'com.jaredsburrows.license'
        }

        repositories {
          maven {
            url '${mavenRepoUrl}'
          }
        }

        dependencies {
          implementation 'com.google.firebase:firebase-core:10.0.1'
        }
      """

    when:
    def result = GradleRunner.create()
      .withProjectDir(testProjectDir.root)
      .withArguments('licenseReport')
      .withPluginClasspath()
      .build()

    then:
    result.task(':licenseReport').outcome == SUCCESS
    result.output.find("Wrote HTML report to .*${reportFolder}/licenseReport.html.")
    result.output.find("Wrote JSON report to .*${reportFolder}/licenseReport.json.")

    def actualHtml = new File("${reportFolder}/licenseReport.html").text.stripIndent().trim()
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
    def actualJson = new File("${reportFolder}/licenseReport.json").text.stripIndent().trim()
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

    actualHtml == expectedHtml
    actualJson == expectedJson
  }

  def 'licenseReport with duplicate dependencies'() {
    given:
    buildFile <<
      """
        plugins {
          id 'java-library'
          id 'com.jaredsburrows.license'
        }

        repositories {
          maven {
            url '${mavenRepoUrl}'
          }
        }

        dependencies {
          implementation 'com.android.support:appcompat-v7:26.1.0'
          implementation 'com.android.support:appcompat-v7:26.1.0'
          implementation 'com.android.support:design:26.1.0'
        }
      """

    when:
    def result = GradleRunner.create()
      .withProjectDir(testProjectDir.root)
      .withArguments('licenseReport')
      .withPluginClasspath()
      .build()

    then:
    result.task(':licenseReport').outcome == SUCCESS
    result.output.find("Wrote HTML report to .*${reportFolder}/licenseReport.html.")
    result.output.find("Wrote JSON report to .*${reportFolder}/licenseReport.json.")

    def actualHtml = new File("${reportFolder}/licenseReport.html").text.stripIndent().trim()
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
      <pre>${getLicenseText('apache-2.0.txt')}</pre>
    </ul>
  </body>
</html>
""".stripIndent().trim()
    def actualJson = new File("${reportFolder}/licenseReport.json").text.stripIndent().trim()
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
  }

  def 'licenseReport with dependency with full pom with project name, developers, url, year, bad license'() {
    given:
    buildFile <<
      """
        plugins {
          id 'java-library'
          id 'com.jaredsburrows.license'
        }

        repositories {
          maven {
            url '${mavenRepoUrl}'
          }
        }

        dependencies {
          implementation 'group:name3:1.0.0'
        }
      """

    when:
    def result = GradleRunner.create()
      .withProjectDir(testProjectDir.root)
      .withArguments('licenseReport')
      .withPluginClasspath()
      .build()

    then:
    result.task(':licenseReport').outcome == SUCCESS
    result.output.find("Wrote HTML report to .*${reportFolder}/licenseReport.html.")
    result.output.find("Wrote JSON report to .*${reportFolder}/licenseReport.json.")

    def actualHtml = new File("${reportFolder}/licenseReport.html").text.stripIndent().trim()
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
    def actualJson = new File("${reportFolder}/licenseReport.json").text.stripIndent().trim()
    def expectedJson =
      """
[]
""".stripIndent().trim()

    actualHtml == expectedHtml
    actualJson == expectedJson
  }

  def 'licenseReport with dependency with full pom and project name, developers, url, year, single license'() {
    given:
    buildFile <<
      """
        plugins {
          id 'java-library'
          id 'com.jaredsburrows.license'
        }

        repositories {
          maven {
            url '${mavenRepoUrl}'
          }
        }

        dependencies {
          implementation 'group:name:1.0.0'
        }
      """

    when:
    def result = GradleRunner.create()
      .withProjectDir(testProjectDir.root)
      .withArguments('licenseReport')
      .withPluginClasspath()
      .build()

    then:
    result.task(':licenseReport').outcome == SUCCESS
    result.output.find("Wrote HTML report to .*${reportFolder}/licenseReport.html.")
    result.output.find("Wrote JSON report to .*${reportFolder}/licenseReport.json.")

    def actualHtml = new File("${reportFolder}/licenseReport.html").text.stripIndent().trim()
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
    </ul>
  </body>
</html>
""".stripIndent().trim()
    def actualJson = new File("${reportFolder}/licenseReport.json").text.stripIndent().trim()
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
            {
                "license": "Some license",
                "license_url": "http://website.tld/"
            }
        ],
        "dependency": "group:name:1.0.0"
    }
]
""".stripIndent().trim()

    actualHtml == expectedHtml
    actualJson == expectedJson
  }

  def 'licenseReport dependency with full pom - project name, multiple developers, url, year, multiple licenses'() {
    given:
    buildFile <<
      """
        plugins {
          id 'java-library'
          id 'com.jaredsburrows.license'
        }

        repositories {
          maven {
            url '${mavenRepoUrl}'
          }
        }

        dependencies {
          implementation 'group:name2:1.0.0'
        }
      """

    when:
    def result = GradleRunner.create()
      .withProjectDir(testProjectDir.root)
      .withArguments('licenseReport')
      .withPluginClasspath()
      .build()

    then:
    result.task(':licenseReport').outcome == SUCCESS
    result.output.find("Wrote HTML report to .*${reportFolder}/licenseReport.html.")
    result.output.find("Wrote JSON report to .*${reportFolder}/licenseReport.json.")

    def actualHtml = new File("${reportFolder}/licenseReport.html").text.stripIndent().trim()
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
    </ul>
  </body>
</html>
""".stripIndent().trim()
    def actualJson = new File("${reportFolder}/licenseReport.json").text.stripIndent().trim()
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
            {
                "license": "Some license",
                "license_url": "http://website.tld/"
            },
            {
                "license": "Some license",
                "license_url": "http://website.tld/"
            }
        ],
        "dependency": "group:name2:1.0.0"
    }
]
""".stripIndent().trim()

    actualHtml == expectedHtml
    actualJson == expectedJson
  }

  def 'licenseReport with dependency without license information that in parent pom'() {
    given:
    buildFile <<
      """
        plugins {
          id 'java-library'
          id 'com.jaredsburrows.license'
        }

        repositories {
          maven {
            url '${mavenRepoUrl}'
          }
        }

        dependencies {
          implementation 'group:child:1.0.0'
          implementation 'com.squareup.retrofit2:retrofit:2.3.0'
        }
      """

    when:
    def result = GradleRunner.create()
      .withProjectDir(testProjectDir.root)
      .withArguments('licenseReport')
      .withPluginClasspath()
      .build()

    then:
    result.task(':licenseReport').outcome == SUCCESS
    result.output.find("Wrote HTML report to .*${reportFolder}/licenseReport.html.")
    result.output.find("Wrote JSON report to .*${reportFolder}/licenseReport.json.")

    def actualHtml = new File("${reportFolder}/licenseReport.html").text.stripIndent().trim()
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
        <a href='#1288284111'>Retrofit</a>
      </li>
      <a name='1288284111' />
      <pre>${getLicenseText('apache-2.0.txt')}</pre>
    </ul>
  </body>
</html>
""".stripIndent().trim()
    def actualJson = new File("${reportFolder}/licenseReport.json").text.stripIndent().trim()
    def expectedJson =
      """
[
    {
        "project": "Fake dependency name",
        "description": "Fake dependency description",
        "version": null,
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
        "dependency": "group:child:1.0.0"
    },
    {
        "project": "Retrofit",
        "description": null,
        "version": null,
        "developers": [
            
        ],
        "url": null,
        "year": null,
        "licenses": [
            {
                "license": "Apache 2.0",
                "license_url": "http://www.apache.org/licenses/LICENSE-2.0.txt"
            }
        ],
        "dependency": "com.squareup.retrofit2:retrofit:2.3.0"
    }
]
""".stripIndent().trim()

    actualHtml == expectedHtml
    actualJson == expectedJson
  }

  def 'licenseReport with project dependencies - multi java modules'() {
    given:
    testProjectDir.newFile('settings.gradle') << """
include 'subproject'
        """

    buildFile << """
plugins {
  id 'java-library'
  id 'com.jaredsburrows.license'
}

allprojects {
  repositories {
    maven {
      url '${mavenRepoUrl}'
    }
  }
}

dependencies {
  implementation project(':subproject')
  implementation 'com.android.support:appcompat-v7:26.1.0'
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
      .withArguments('licenseReport')
      .withPluginClasspath()
      .build()

    then:
    result.task(':licenseReport').outcome == SUCCESS
    result.output.find("Wrote HTML report to .*${reportFolder}/licenseReport.html.")
    result.output.find("Wrote JSON report to .*${reportFolder}/licenseReport.json.")

    def actualHtml = new File("${reportFolder}/licenseReport.html").text.stripIndent().trim()
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
      <pre>${getLicenseText('apache-2.0.txt')}</pre>
    </ul>
  </body>
</html>
""".stripIndent().trim()
    def actualJson = new File("${reportFolder}/licenseReport.json").text.stripIndent().trim()
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

    then:
    actualHtml == expectedHtml
    actualJson == expectedJson
  }

  def 'licenseReport using api and implementation configurations with multi java modules'() {
    given:
    testProjectDir.newFile('settings.gradle') << """
include 'subproject'
        """

    buildFile << """
plugins {
  id 'java-library'
  id 'com.jaredsburrows.license'
}

allprojects {
  repositories {
    maven {
      url '${mavenRepoUrl}'
    }
  }
}

dependencies {
  api project(':subproject')
  implementation 'com.android.support:appcompat-v7:26.1.0'
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
      .withArguments('licenseReport')
      .withPluginClasspath()
      .build()

    then:
    result.task(':licenseReport').outcome == SUCCESS
    result.output.find("Wrote HTML report to .*${reportFolder}/licenseReport.html.")
    result.output.find("Wrote JSON report to .*${reportFolder}/licenseReport.json.")

    def actualHtml = new File("${reportFolder}/licenseReport.html").text.stripIndent().trim()
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
      <pre>${getLicenseText('apache-2.0.txt')}</pre>
    </ul>
  </body>
</html>
""".stripIndent().trim()
    def actualJson = new File("${reportFolder}/licenseReport.json").text.stripIndent().trim()
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

    then:
    actualHtml == expectedHtml
    actualJson == expectedJson
  }
}
