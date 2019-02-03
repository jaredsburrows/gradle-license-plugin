package com.jaredsburrows.license

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import test.TestUtils

@Deprecated // TODO migrate to LicensePluginJavaSpec
final class LicenseReportTaskJavaSpec extends Specification {
  @Rule public TemporaryFolder testProjectDir = new TemporaryFolder()
  private String mavenRepoUrl
  private File buildFile
  private String htmlReport
  private String jsonReport
  private Project project
  private Project subproject

  def 'setup'() {
    mavenRepoUrl = getClass().getResource('/maven').toURI()
    buildFile = testProjectDir.newFile('build.gradle')
    htmlReport = "${testProjectDir.root.path}/build/reports/licenses/licenseReport.html"
    jsonReport = "${testProjectDir.root.path}/build/reports/licenses/licenseReport.json"

    project = ProjectBuilder.builder()
      .withProjectDir(testProjectDir.root)
      .withName('project')
      .build()
    project.repositories {
      maven { url mavenRepoUrl }
    }

    // Setup subproject
    subproject = ProjectBuilder.builder()
      .withParent(project)
      .withName('subproject')
      .build()
    subproject.repositories {
      maven { url mavenRepoUrl }
    }
  }

  def 'java project running licenseReport with project dependencies - multi java modules'() {
    given:
    project.apply plugin: 'java'
    new LicensePlugin().apply(project)
    project.dependencies {
      implementation 'com.android.support:appcompat-v7:26.1.0'
      implementation project.project(':subproject')
    }

    subproject.apply plugin: 'java-library'
    subproject.dependencies {
      implementation 'com.android.support:design:26.1.0'
    }

    when:
    project.evaluate()
    LicenseReportTask task = project.tasks.getByName('licenseReport')
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
      <pre>${TestUtils.getLicenseText('apache-2.0.txt')}</pre>
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

  def 'java project running licenseReport using api and implementation configurations with multi java modules'() {
    given:
    project.apply plugin: 'java-library'
    new LicensePlugin().apply(project)
    project.dependencies {
      api 'com.android.support:appcompat-v7:26.1.0'
      implementation project.project(':subproject')
    }

    subproject.apply plugin: 'java-library'
    subproject.dependencies {
      implementation 'com.android.support:design:26.1.0'
    }

    when:
    project.evaluate()
    LicenseReportTask task = project.tasks.getByName('licenseReport')
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
      <pre>${TestUtils.getLicenseText('apache-2.0.txt')}</pre>
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
