package com.jaredsburrows.license

import groovy.json.JsonSlurper
import org.gradle.testkit.runner.BuildResult
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Issue
import spock.lang.Specification
import spock.lang.Unroll

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS
import static org.gradle.testkit.runner.TaskOutcome.UP_TO_DATE
import static test.TestUtils.assertHtml
import static test.TestUtils.assertJson
import static test.TestUtils.getLicenseText
import static test.TestUtils.gradleWithCommand

final class LicensePluginJvmSpec extends Specification {
  @Rule
  public final TemporaryFolder testProjectDir = new TemporaryFolder()
  private String mavenRepoUrl
  private File buildFile
  private String reportFolder

  def 'setup'() {
    mavenRepoUrl = getClass().getResource('/maven').toURI()
    buildFile = testProjectDir.newFile('build.gradle')
    // In case we're on Windows, fix the \s in the string containing the name
    reportFolder = "${testProjectDir.root.path.replaceAll("\\\\", '/')}/build/reports/licenses"
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
    BuildResult result = gradleWithCommand(testProjectDir.root, 'licenseReport', '-s')
    File actualCsv = new File(reportFolder, 'licenseReport.csv')
    File actualHtml = new File(reportFolder, 'licenseReport.html')
    String expectedHtml =
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
    File actualJson = new File(reportFolder, 'licenseReport.json')
    String expectedJson =
      """
      []
      """
    File actualText = new File(reportFolder, 'licenseReport.txt')

    then:
    result.task(':licenseReport').outcome == SUCCESS
    result.output.find("Wrote CSV report to .*${reportFolder}/licenseReport.csv.")
    actualCsv.exists()
    result.output.find("Wrote HTML report to .*${reportFolder}/licenseReport.html.")
    actualHtml.exists()
    result.output.find("Wrote JSON report to .*${reportFolder}/licenseReport.json.")
    actualJson.exists()
    result.output.find("Wrote Text report to .*${reportFolder}/licenseReport.txt.")
    actualText.exists()
    assertHtml(expectedHtml, actualHtml.text)
    assertJson(expectedJson, actualJson.text)
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
    BuildResult result = gradleWithCommand(testProjectDir.root, 'licenseReport', '-s')
    File actualCsv = new File(reportFolder, 'licenseReport.csv')
    File actualHtml = new File(reportFolder, 'licenseReport.html')
    String expectedHtml =
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
              <a href="#0">firebase-core</a>
              <dl>
                <dt>Copyright &copy; 20xx The original author or authors</dt>
                <dd></dd>
              </dl>
            </li>
          </ul>
          <pre id="0">No license found</pre>
          <hr>
        </body>
      </html>
      """
    File actualJson = new File(reportFolder, 'licenseReport.json')
    String expectedJson =
      """
      [
        {
          "project":"firebase-core",
          "description":null,
          "version":"10.0.1",
          "developers":[],
          "url":null,
          "year":null,
          "licenses":[],
          "dependency":"com.google.firebase:firebase-core:10.0.1"
        }
      ]
      """
    File actualText = new File(reportFolder, 'licenseReport.txt')

    then:
    result.task(':licenseReport').outcome == SUCCESS
    result.output.find("Wrote CSV report to .*${reportFolder}/licenseReport.csv.")
    actualCsv.exists()
    result.output.find("Wrote HTML report to .*${reportFolder}/licenseReport.html.")
    actualHtml.exists()
    result.output.find("Wrote JSON report to .*${reportFolder}/licenseReport.json.")
    actualJson.exists()
    result.output.find("Wrote Text report to .*${reportFolder}/licenseReport.txt.")
    actualText.exists()
    assertHtml(expectedHtml, actualHtml.text)
    assertJson(expectedJson, actualJson.text)
  }

  def 'licenseReport default - version numbers - do not show version numbers by default'() {
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
        implementation 'com.android.support:design:26.1.0'
      }

      licenseReport {
        showVersions = true
      }
      """

    when:
    BuildResult result = gradleWithCommand(testProjectDir.root, 'licenseReport', '-s')
    File actualCsv = new File(reportFolder, 'licenseReport.csv')
    File actualHtml = new File(reportFolder, 'licenseReport.html')
    String expectedHtml =
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
              <a href="#1934118923">appcompat-v7 (26.1.0)</a>
              <dl>
                <dt>Copyright &copy; 20xx The original author or authors</dt>
                <dd></dd>
              </dl>
            </li>
            <li>
              <a href="#1934118923">design (26.1.0)</a>
              <dl>
                <dt>Copyright &copy; 20xx The original author or authors</dt>
                <dd></dd>
              </dl>
            </li>
          </ul>
          <pre id="1934118923">${getLicenseText('apache-2.0.txt')}</pre>
          <br>
          <hr>
        </body>
      </html>
      """
    File actualJson = new File(reportFolder, 'licenseReport.json')
    String expectedJson =
      """
      [
        {
          "project":"appcompat-v7",
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
          "dependency":"com.android.support:appcompat-v7:26.1.0"
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
    File actualText = new File(reportFolder, 'licenseReport.txt')

    then:
    result.task(':licenseReport').outcome == SUCCESS
    result.output.find("Wrote CSV report to .*${reportFolder}/licenseReport.csv.")
    actualCsv.exists()
    result.output.find("Wrote HTML report to .*${reportFolder}/licenseReport.html.")
    actualHtml.exists()
    result.output.find("Wrote JSON report to .*${reportFolder}/licenseReport.json.")
    actualJson.exists()
    result.output.find("Wrote Text report to .*${reportFolder}/licenseReport.txt.")
    actualText.exists()
    assertHtml(expectedHtml, actualHtml.text)
    assertJson(expectedJson, actualJson.text)
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
    BuildResult result = gradleWithCommand(testProjectDir.root, 'licenseReport', '-s')
    File actualCsv = new File(reportFolder, 'licenseReport.csv')
    File actualHtml = new File(reportFolder, 'licenseReport.html')
    String expectedHtml =
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
          <pre id="1934118923">${getLicenseText('apache-2.0.txt')}</pre>
          <br>
          <hr>
        </body>
      </html>
      """
    File actualJson = new File(reportFolder, 'licenseReport.json')
    String expectedJson =
      """
      [
        {
          "project":"appcompat-v7",
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
          "dependency":"com.android.support:appcompat-v7:26.1.0"
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
    File actualText = new File(reportFolder, 'licenseReport.txt')

    then:
    result.task(':licenseReport').outcome == SUCCESS
    result.output.find("Wrote CSV report to .*${reportFolder}/licenseReport.csv.")
    actualCsv.exists()
    result.output.find("Wrote HTML report to .*${reportFolder}/licenseReport.html.")
    actualHtml.exists()
    result.output.find("Wrote JSON report to .*${reportFolder}/licenseReport.json.")
    actualJson.exists()
    result.output.find("Wrote Text report to .*${reportFolder}/licenseReport.txt.")
    actualText.exists()
    assertHtml(expectedHtml, actualHtml.text)
    assertJson(expectedJson, actualJson.text)
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
    BuildResult result = gradleWithCommand(testProjectDir.root, 'licenseReport', '-s')
    File actualCsv = new File(reportFolder, 'licenseReport.csv')
    File actualHtml = new File(reportFolder, 'licenseReport.html')
    String expectedHtml =
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
    File actualJson = new File(reportFolder, 'licenseReport.json')
    String expectedJson =
      """
      []
      """
    File actualText = new File(reportFolder, 'licenseReport.txt')

    then:
    result.task(':licenseReport').outcome == SUCCESS
    result.output.find("Wrote CSV report to .*${reportFolder}/licenseReport.csv.")
    actualCsv.exists()
    result.output.find("Wrote HTML report to .*${reportFolder}/licenseReport.html.")
    actualHtml.exists()
    result.output.find("Wrote JSON report to .*${reportFolder}/licenseReport.json.")
    actualJson.exists()
    result.output.find("Wrote Text report to .*${reportFolder}/licenseReport.txt.")
    actualText.exists()
    assertHtml(expectedHtml, actualHtml.text)
    assertJson(expectedJson, actualJson.text)
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
    BuildResult result = gradleWithCommand(testProjectDir.root, 'licenseReport', '-s')
    File actualCsv = new File(reportFolder, 'licenseReport.csv')
    File actualHtml = new File(reportFolder, 'licenseReport.html')
    String expectedHtml =
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
              <a href="#-296292112">Fake dependency name</a>
              <dl>
                <dt>Copyright &copy; 2017 name</dt>
                <dd></dd>
              </dl>
            </li>
          </ul>
          <pre id="-296292112">Some license
            <a href="http://website.tld/">http://website.tld/</a>
          </pre>
          <br>
          <hr>
        </body>
      </html>
      """
    File actualJson = new File(reportFolder, 'licenseReport.json')
    String expectedJson =
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
    File actualText = new File(reportFolder, 'licenseReport.txt')

    then:
    result.task(':licenseReport').outcome == SUCCESS
    result.output.find("Wrote CSV report to .*${reportFolder}/licenseReport.csv.")
    actualCsv.exists()
    result.output.find("Wrote HTML report to .*${reportFolder}/licenseReport.html.")
    actualHtml.exists()
    result.output.find("Wrote JSON report to .*${reportFolder}/licenseReport.json.")
    actualJson.exists()
    result.output.find("Wrote Text report to .*${reportFolder}/licenseReport.txt.")
    actualText.exists()
    assertHtml(expectedHtml, actualHtml.text)
    assertJson(expectedJson, actualJson.text)
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
    BuildResult result = gradleWithCommand(testProjectDir.root, 'licenseReport', '-s')
    File actualCsv = new File(reportFolder, 'licenseReport.csv')
    File actualHtml = new File(reportFolder, 'licenseReport.html')
    String expectedHtml =
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
              <a href="#1195092182">Fake dependency name</a>
              <dl>
                <dt>Copyright &copy; 2017 name</dt>
                <dd></dd>
              </dl>
            </li>
          </ul>
          <pre id="1195092182">Some license
            <a href="http://website.tld/">http://website.tld/</a>
          </pre>
          <br>
          <pre>Some license
            <a href="http://website.tld/">http://website.tld/</a>
          </pre>
          <br>
          <hr>
        </body>
      </html>
      """
    File actualJson = new File(reportFolder, 'licenseReport.json')
    String expectedJson =
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
          "licenses":[
            {
              "license":"Some license",
              "license_url":"http://website.tld/"
            },
            {
              "license":"Some license",
              "license_url":"http://website.tld/"
            }
          ],
          "dependency":"group:name2:1.0.0"
        }
      ]
      """
    File actualText = new File(reportFolder, 'licenseReport.txt')

    then:
    result.task(':licenseReport').outcome == SUCCESS
    result.output.find("Wrote CSV report to .*${reportFolder}/licenseReport.csv.")
    actualCsv.exists()
    result.output.find("Wrote HTML report to .*${reportFolder}/licenseReport.html.")
    actualHtml.exists()
    result.output.find("Wrote JSON report to .*${reportFolder}/licenseReport.json.")
    actualJson.exists()
    result.output.find("Wrote Text report to .*${reportFolder}/licenseReport.txt.")
    actualText.exists()
    assertHtml(expectedHtml, actualHtml.text)
    assertJson(expectedJson, actualJson.text)
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
    BuildResult result = gradleWithCommand(testProjectDir.root, 'licenseReport', '-s')
    File actualCsv = new File(reportFolder, 'licenseReport.csv')
    File actualHtml = new File(reportFolder, 'licenseReport.html')
    String expectedHtml =
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
              <a href="#1934118923">Retrofit</a>
              <dl>
                <dt>Copyright &copy; 20xx The original author or authors</dt>
                <dd></dd>
              </dl>
            </li>
          </ul>
          <pre id="1934118923">${getLicenseText('apache-2.0.txt')}</pre>
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
          <pre id="-296292112">Some license
            <a href="http://website.tld/">http://website.tld/</a>
          </pre>
          <br>
          <hr>
        </body>
      </html>
      """
    File actualJson = new File(reportFolder, 'licenseReport.json')
    String expectedJson =
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
          "licenses":[
            {
              "license":"Some license",
              "license_url":"http://website.tld/"
            }
          ],
          "dependency":"group:child:1.0.0"
        },
        {
          "project":"Retrofit",
          "description":null,
          "version":"2.3.0",
          "developers":[],
          "url":null,
          "year":null,
          "licenses":[
            {
              "license":"Apache 2.0",
              "license_url":"http://www.apache.org/licenses/LICENSE-2.0.txt"
            }
          ],
          "dependency":"com.squareup.retrofit2:retrofit:2.3.0"
        }
      ]
      """
    File actualText = new File(reportFolder, 'licenseReport.txt')

    then:
    result.task(':licenseReport').outcome == SUCCESS
    result.output.find("Wrote CSV report to .*${reportFolder}/licenseReport.csv.")
    actualCsv.exists()
    result.output.find("Wrote HTML report to .*${reportFolder}/licenseReport.html.")
    actualHtml.exists()
    result.output.find("Wrote JSON report to .*${reportFolder}/licenseReport.json.")
    actualJson.exists()
    result.output.find("Wrote Text report to .*${reportFolder}/licenseReport.txt.")
    actualText.exists()
    assertHtml(expectedHtml, actualHtml.text)
    assertJson(expectedJson, actualJson.text)
  }

  @Issue("jaredsburrows/gradle-license-plugin/issues/800")
  def 'licenseReport with parents of the same module at different versions'() {
    given: 'two dependencies whose parent POMs are different versions of the same module'
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
        implementation 'group:childa:1.0.0'
        implementation 'group:childb:1.0.0'
      }
      """

    when:
    BuildResult result = gradleWithCommand(testProjectDir.root, 'licenseReport', '-s')
    File actualJson = new File(reportFolder, 'licenseReport.json')
    String expectedJson =
      """
      [
        {
          "project":"Child A",
          "description":"Fake dependency with parent at version 1",
          "version":"1.0.0",
          "developers":[],
          "url":null,
          "year":null,
          "licenses":[
            {
              "license":"License One",
              "license_url":"http://license-1.tld/"
            }
          ],
          "dependency":"group:childa:1.0.0"
        },
        {
          "project":"Child B",
          "description":"Fake dependency with parent at version 2",
          "version":"1.0.0",
          "developers":[],
          "url":null,
          "year":null,
          "licenses":[
            {
              "license":"License Two",
              "license_url":"http://license-2.tld/"
            }
          ],
          "dependency":"group:childb:1.0.0"
        }
      ]
      """

    then: 'both parent POMs are resolved and each child reports its own parent license'
    result.task(':licenseReport').outcome == SUCCESS
    !result.output.contains('Parent POM group:multiparent:1.0.0@pom not found')
    !result.output.contains('Parent POM group:multiparent:2.0.0@pom not found')
    assertJson(expectedJson, actualJson.text)
  }

  def 'licenseReport with same set of multiple licenses'() {
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
        implementation 'group:name5-1:1.0.0'
        implementation 'group:name5-2:1.0.0'
      }
      """

    when:
    BuildResult result = gradleWithCommand(testProjectDir.root, 'licenseReport', '-s')
    File actualCsv = new File(reportFolder, 'licenseReport.csv')
    File actualHtml = new File(reportFolder, 'licenseReport.html')
    String expectedHtml =
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
              <a href="#1929112087">Fake dependency name 1</a>
              <dl>
                <dt>Copyright &copy; 2017 name</dt>
                <dd></dd>
              </dl>
            </li>
            <li>
              <a href="#1929112087">Fake dependency name 2</a>
              <dl>
                <dt>Copyright &copy; 2017 name</dt>
                <dd></dd>
              </dl>
            </li>
          </ul>
          <pre id="1929112087">Some license 1
            <a href="http://website-1.tld/">http://website-1.tld/</a>
          </pre>
          <br>
          <pre>Some license 2
            <a href="http://website-2.tld/">http://website-2.tld/</a>
          </pre>
          <br>
          <hr>
        </body>
      </html>
      """
    File actualJson = new File(reportFolder, 'licenseReport.json')
    String expectedJson =
      """
      [
        {
          "project":"Fake dependency name 1",
          "description":"Fake dependency description 1",
          "version":"1.0.0",
          "developers":["name"],
          "url":"https://github.com/user/repo",
          "year":"2017",
          "licenses":[
            {
              "license":"Some license 1",
              "license_url":"http://website-1.tld/"
            },
            {
              "license":
              "Some license 2",
              "license_url":"http://website-2.tld/"
            }
          ],
          "dependency":"group:name5-1:1.0.0"
        },
        {
          "project":"Fake dependency name 2",
          "description":"Fake dependency description 2",
          "version":"1.0.0",
          "developers":["name"],
          "url":"https://github.com/user/repo",
          "year":"2017",
          "licenses":[
            {
              "license":"Some license 2",
              "license_url":"http://website-2.tld/"
            },
            {
              "license":
              "Some license 1",
              "license_url":"http://website-1.tld/"
            }
          ],
          "dependency":"group:name5-2:1.0.0"
        }
      ]
      """
    File actualText = new File(reportFolder, 'licenseReport.txt')

    then:
    result.task(':licenseReport').outcome == SUCCESS
    result.output.find("Wrote CSV report to .*${reportFolder}/licenseReport.csv.")
    actualCsv.exists()
    result.output.find("Wrote HTML report to .*${reportFolder}/licenseReport.html.")
    actualHtml.exists()
    result.output.find("Wrote JSON report to .*${reportFolder}/licenseReport.json.")
    actualJson.exists()
    result.output.find("Wrote Text report to .*${reportFolder}/licenseReport.txt.")
    actualText.exists()
    assertHtml(expectedHtml, actualHtml.text)
    assertJson(expectedJson, actualJson.text)
  }

  def 'licenseReport with project dependencies - multi java modules'() {
    given:
    testProjectDir.newFile('settings.gradle') <<
      """
      include 'subproject'
      """
    testProjectDir.newFolder('subproject')

    buildFile <<
      """
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
    BuildResult result = gradleWithCommand(testProjectDir.root, 'licenseReport', '-s')
    File actualCsv = new File(reportFolder, 'licenseReport.csv')
    File actualHtml = new File(reportFolder, 'licenseReport.html')
    String expectedHtml =
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
          <pre id="1934118923">${getLicenseText('apache-2.0.txt')}</pre>
          <br>
          <hr>
        </body>
      </html>
      """
    File actualJson = new File(reportFolder, 'licenseReport.json')
    String expectedJson =
      """
      [
        {
          "project":"appcompat-v7",
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
          "dependency":"com.android.support:appcompat-v7:26.1.0"
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
    File actualText = new File(reportFolder, 'licenseReport.txt')

    then:
    result.task(':licenseReport').outcome == SUCCESS
    result.output.find("Wrote CSV report to .*${reportFolder}/licenseReport.csv.")
    actualCsv.exists()
    result.output.find("Wrote HTML report to .*${reportFolder}/licenseReport.html.")
    actualHtml.exists()
    result.output.find("Wrote JSON report to .*${reportFolder}/licenseReport.json.")
    actualJson.exists()
    result.output.find("Wrote Text report to .*${reportFolder}/licenseReport.txt.")
    actualText.exists()
    assertHtml(expectedHtml, actualHtml.text)
    assertJson(expectedJson, actualJson.text)
  }

  def 'licenseReport with project dependencies - deep dependency graph'() {
    given:
    int depth = 18

    testProjectDir.newFile('settings.gradle') <<
      """
      ${(1..depth).collect {
        """
        include 'subproject_${it}a'
        include 'subproject_${it}b'
        include 'subproject_${it}c'
        """
      }.join()}
      """
    (1..depth).each {
      testProjectDir.newFolder("subproject_${it}a")
      testProjectDir.newFolder("subproject_${it}b")
      testProjectDir.newFolder("subproject_${it}c")
    }

    buildFile <<
      """
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
        implementation project(':subproject_1a')
        implementation project(':subproject_1b')
        implementation project(':subproject_1c')
        implementation 'com.android.support:appcompat-v7:26.1.0'
      }

      ${(1..depth - 1).collect {
        """
        project(':subproject_${it}a') {
          apply plugin: 'java-library'

          dependencies {
            implementation project(':subproject_${it + 1}a')
            implementation project(':subproject_${it + 1}b')
            implementation project(':subproject_${it + 1}c')
          }
        }

        project(':subproject_${it}b') {
          apply plugin: 'java-library'

          dependencies {
            implementation project(':subproject_${it + 1}a')
            implementation project(':subproject_${it + 1}b')
            implementation project(':subproject_${it + 1}c')
          }
        }

        project(':subproject_${it}c') {
          apply plugin: 'java-library'

          dependencies {
            implementation project(':subproject_${it + 1}a')
            implementation project(':subproject_${it + 1}b')
            implementation project(':subproject_${it + 1}c')
          }
        }
        """
      }.join()}

      project(':subproject_${depth}a') {
        apply plugin: 'java-library'

        dependencies {
          implementation 'com.android.support:design:26.1.0'
        }
      }

      project(':subproject_${depth}b') {
        apply plugin: 'java-library'

        dependencies {
          implementation 'com.android.support:design:26.1.0'
        }
      }

      project(':subproject_${depth}c') {
        apply plugin: 'java-library'

        dependencies {
          implementation 'com.android.support:design:26.1.0'
        }
      }
      """

    when:
    BuildResult result = gradleWithCommand(testProjectDir.root, 'licenseReport', '-s')
    File actualCsv = new File(reportFolder, 'licenseReport.csv')
    File actualHtml = new File(reportFolder, 'licenseReport.html')
    String expectedHtml =
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
          <pre id="1934118923">${getLicenseText('apache-2.0.txt')}</pre>
          <br>
          <hr>
        </body>
      </html>
      """
    File actualJson = new File(reportFolder, 'licenseReport.json')
    String expectedJson =
      """
      [
        {
          "project":"appcompat-v7",
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
          "dependency":"com.android.support:appcompat-v7:26.1.0"
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
    File actualText = new File(reportFolder, 'licenseReport.txt')

    then:
    result.task(':licenseReport').outcome == SUCCESS
    result.output.find("Wrote CSV report to .*${reportFolder}/licenseReport.csv.")
    actualCsv.exists()
    result.output.find("Wrote HTML report to .*${reportFolder}/licenseReport.html.")
    actualHtml.exists()
    result.output.find("Wrote JSON report to .*${reportFolder}/licenseReport.json.")
    actualJson.exists()
    result.output.find("Wrote Text report to .*${reportFolder}/licenseReport.txt.")
    actualText.exists()
    assertHtml(expectedHtml, actualHtml.text)
    assertJson(expectedJson, actualJson.text)
  }

  def 'licenseReport using api and implementation configurations with multi java modules'() {
    given:
    testProjectDir.newFile('settings.gradle') <<
      """
      include 'subproject'
      """
    testProjectDir.newFolder('subproject')

    buildFile <<
      """
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
    BuildResult result = gradleWithCommand(testProjectDir.root, 'licenseReport', '-s')
    File actualCsv = new File(reportFolder, 'licenseReport.csv')
    File actualHtml = new File(reportFolder, 'licenseReport.html')
    String expectedHtml =
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
          <pre id="1934118923">${getLicenseText('apache-2.0.txt')}</pre>
          <br>
          <hr>
        </body>
      </html>
      """
    File actualJson = new File(reportFolder, 'licenseReport.json')
    String expectedJson =
      """
      [
        {
          "project":"appcompat-v7",
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
          "dependency":"com.android.support:appcompat-v7:26.1.0"
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
    File actualText = new File(reportFolder, 'licenseReport.txt')

    then:
    result.task(':licenseReport').outcome == SUCCESS
    result.output.find("Wrote CSV report to .*${reportFolder}/licenseReport.csv.")
    actualCsv.exists()
    result.output.find("Wrote HTML report to .*${reportFolder}/licenseReport.html.")
    actualHtml.exists()
    result.output.find("Wrote JSON report to .*${reportFolder}/licenseReport.json.")
    actualJson.exists()
    result.output.find("Wrote Text report to .*${reportFolder}/licenseReport.txt.")
    actualText.exists()
    assertHtml(expectedHtml, actualHtml.text)
    assertJson(expectedJson, actualJson.text)
  }

  @Issue("jaredsburrows/gradle-license-plugin/issues/275")
  def 'licenseReport with encoding, such as iso-8859-1 instead of UTF-8'() {
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
        implementation 'com.sun.activation:javax.activation:1.2.0' // iso-8859-1
      }
      """

    when:
    BuildResult result = gradleWithCommand(testProjectDir.root, 'licenseReport', '-s')

    then:
    result.task(':licenseReport').outcome == SUCCESS
  }

  @Unroll
  def 'licenseReport sorting by id when package name is the same'() {
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
        implementation 'group:module-same-name-2:1.0.0'
        implementation 'group:module-same-name-1:1.0.0'
      }
      """

    when:
    BuildResult result = gradleWithCommand(testProjectDir.root, "licenseReport", '-s')
    String actualHtml = new File(reportFolder, "licenseReport.html").text
    String expectedHtml =
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
          <pre id="0">No license found</pre>
          <hr>
        </body>
      </html>
      """
    String actualJson = new File(reportFolder, "licenseReport.json").text
    String expectedJson =
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
    result.task(":licenseReport").outcome == SUCCESS
    result.output.find("Wrote CSV report to .*${reportFolder}/licenseReport.csv.")
    result.output.find("Wrote HTML report to .*${reportFolder}/licenseReport.html.")
    result.output.find("Wrote JSON report to .*${reportFolder}/licenseReport.json.")
    assertHtml(expectedHtml, actualHtml)
    assertJson(expectedJson, actualJson)
  }

  def 'licenseReport deduplicates kotlin multiplatform root and platform variant artifacts'() {
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
        implementation 'group:samename:1.0.0'
        implementation 'group:samename-android:1.0.0'
      }
      """

    when:
    BuildResult result = gradleWithCommand(testProjectDir.root, 'licenseReport', '-s')
    File actualJson = new File(reportFolder, 'licenseReport.json')
    String expectedJson =
      """
      [
        {
          "project":"Same module",
          "description":null,
          "version":"1.0.0",
          "developers":[],
          "url":null,
          "year":"2017",
          "licenses":[
            {
              "license":"Some license",
              "license_url":"http://website.tld/"
            }
          ],
          "dependency":"group:samename:1.0.0"
        }
      ]
      """

    then:
    result.task(':licenseReport').outcome == SUCCESS
    assertJson(expectedJson, actualJson.text)
  }

  def 'licenseReport removes duplicate developers within a project'() {
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
        implementation 'group:dupedev:1.0.0'
      }
      """

    when:
    BuildResult result = gradleWithCommand(testProjectDir.root, 'licenseReport', '-s')
    File actualJson = new File(reportFolder, 'licenseReport.json')
    String expectedJson =
      """
      [
        {
          "project":"Duplicate dev module",
          "description":null,
          "version":"1.0.0",
          "developers":["Sam"],
          "url":null,
          "year":null,
          "licenses":[],
          "dependency":"group:dupedev:1.0.0"
        }
      ]
      """

    then:
    result.task(':licenseReport').outcome == SUCCESS
    assertJson(expectedJson, actualJson.text)
  }

  def 'licenseReport falls back to artifact id when a pom name has unresolved placeholders'() {
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
        implementation 'group:placeholder:1.0.0'
      }
      """

    when:
    BuildResult result = gradleWithCommand(testProjectDir.root, 'licenseReport', '-s')
    File actualJson = new File(reportFolder, 'licenseReport.json')
    String expectedJson =
      """
      [
        {
          "project":"placeholder",
          "description":null,
          "version":"1.0.0",
          "developers":[],
          "url":null,
          "year":null,
          "licenses":[],
          "dependency":"group:placeholder:1.0.0"
        }
      ]
      """

    then:
    result.task(':licenseReport').outcome == SUCCESS
    assertJson(expectedJson, actualJson.text)
  }

  def 'licenseReport collapses the same library resolved at different versions (compile vs runtime)'() {
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
        compileOnly 'group:verlib:2.0.0'
        runtimeOnly 'group:verlib:1.0.0'
      }
      """

    when:
    BuildResult result = gradleWithCommand(testProjectDir.root, 'licenseReport', '-s')
    File actualJson = new File(reportFolder, 'licenseReport.json')
    String expectedJson =
      """
      [
        {
          "project":"Versioned module",
          "description":null,
          "version":"2.0.0",
          "developers":[],
          "url":null,
          "year":"2017",
          "licenses":[
            {
              "license":"Some license",
              "license_url":"http://website.tld/"
            }
          ],
          "dependency":"group:verlib:2.0.0"
        }
      ]
      """

    then:
    result.task(':licenseReport').outcome == SUCCESS
    assertJson(expectedJson, actualJson.text)
  }

  def 'licenseReport resolves a pom name placeholder defined in a parent pom'() {
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
        implementation 'group:propchild:1.0.0'
      }
      """

    when:
    BuildResult result = gradleWithCommand(testProjectDir.root, 'licenseReport', '-s')
    File actualJson = new File(reportFolder, 'licenseReport.json')
    String expectedJson =
      """
      [
        {
          "project":"Cool Library",
          "description":null,
          "version":"1.0.0",
          "developers":[],
          "url":null,
          "year":null,
          "licenses":[
            {
              "license":"Some license",
              "license_url":"http://website.tld/"
            }
          ],
          "dependency":"group:propchild:1.0.0"
        }
      ]
      """

    then:
    result.task(':licenseReport').outcome == SUCCESS
    assertJson(expectedJson, actualJson.text)
  }

  @Issue("jaredsburrows/gradle-license-plugin/issues/804")
  def 'licenseReport does not resolve configurations at configuration time'() {
    given: 'a build that flags configuration-time resolution the same way AGP does'
    buildFile <<
      """
      import java.util.concurrent.atomic.AtomicBoolean

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

      // Mirror AGP's DependencyResolutionChecks: flag configurations resolved before the task graph is ready.
      AtomicBoolean configurationPhase = new AtomicBoolean(true)
      gradle.taskGraph.whenReady { configurationPhase.set(false) }
      configurations.configureEach { conf ->
        conf.incoming.beforeResolve {
          if (configurationPhase.get()) {
            println("CONFIGURATION-TIME-RESOLUTION: \${conf.name}")
          }
        }
      }
      """

    when:
    BuildResult result = gradleWithCommand(testProjectDir.root, 'licenseReport', '-s')

    then: 'the report is generated without resolving any configuration during configuration'
    result.task(':licenseReport').outcome == SUCCESS
    !result.output.contains('CONFIGURATION-TIME-RESOLUTION:')
  }

  @Issue("jaredsburrows/gradle-license-plugin/issues/804")
  def 'licenseReport re-runs when a POM changes in place'() {
    given: 'a dependency in a file-based repository inside the test project'
    File repoDir = testProjectDir.newFolder('repo', 'group', 'local', '1.0.0')
    File pomFile = new File(repoDir, 'local-1.0.0.pom')
    pomFile.text = """<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0">
  <modelVersion>4.0.0</modelVersion>
  <groupId>group</groupId>
  <artifactId>local</artifactId>
  <version>1.0.0</version>
  <name>Local dependency</name>
  <licenses>
    <license>
      <name>License A</name>
      <url>https://example.com/a</url>
    </license>
  </licenses>
</project>
"""
    new File(repoDir, 'local-1.0.0.jar').createNewFile()
    buildFile <<
      """
      plugins {
        id 'java-library'
        id 'com.jaredsburrows.license'
      }

      repositories {
        maven {
          url '${testProjectDir.root.toURI()}repo'
        }
      }

      dependencies {
        implementation 'group:local:1.0.0'
      }
      """

    when: 'the report is generated'
    BuildResult first = gradleWithCommand(testProjectDir.root, 'licenseReport', '-s')

    then:
    first.task(':licenseReport').outcome == SUCCESS
    new File(reportFolder, 'licenseReport.json').text.contains('License A')

    when: 'nothing changes'
    BuildResult second = gradleWithCommand(testProjectDir.root, 'licenseReport', '-s')

    then:
    second.task(':licenseReport').outcome == UP_TO_DATE

    when: 'the POM content changes at the same path'
    pomFile.text = pomFile.text.replace('License A', 'License B')
    BuildResult third = gradleWithCommand(testProjectDir.root, 'licenseReport', '-s')

    then: 'the task is not up-to-date and regenerates the report'
    third.task(':licenseReport').outcome == SUCCESS
    new File(reportFolder, 'licenseReport.json').text.contains('License B')
  }

  @Issue("jaredsburrows/gradle-license-plugin/issues/444")
  def 'licenseReport collapses a library resolved at different versions across classpaths'() {
    given: 'libraries whose display name or artifact id changed between the resolved versions'
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
        // Same module, renamed between versions: compile resolves 1.0.0, runtime 2.0.0.
        implementation 'group:renamed:1.0.0'
        runtimeOnly 'group:renamed:2.0.0'
        // Renamed to a Kotlin Multiplatform platform artifact between versions.
        implementation 'group:kmpl:1.0.0'
        runtimeOnly 'group:kmpl-jvm:2.0.0'
        // A distinct "-suffix" sibling library that must stay its own report entry.
        implementation 'group:kmpl-jdk7:2.0.0'
      }
      """

    when:
    BuildResult result = gradleWithCommand(testProjectDir.root, 'licenseReport', '-s')
    File actualJson = new File(reportFolder, 'licenseReport.json')
    String expectedJson =
      """
      [
        {
          "project":"kmpl",
          "description":null,
          "version":"2.0.0",
          "developers":[],
          "url":null,
          "year":null,
          "licenses":[
            {
              "license":"Some license",
              "license_url":"http://website.tld/"
            }
          ],
          "dependency":"group:kmpl-jvm:2.0.0"
        },
        {
          "project":"Kmpl Jdk7",
          "description":null,
          "version":"2.0.0",
          "developers":[],
          "url":null,
          "year":null,
          "licenses":[
            {
              "license":"Some license",
              "license_url":"http://website.tld/"
            }
          ],
          "dependency":"group:kmpl-jdk7:2.0.0"
        },
        {
          "project":"renamed",
          "description":null,
          "version":"2.0.0",
          "developers":[],
          "url":null,
          "year":null,
          "licenses":[
            {
              "license":"Some license",
              "license_url":"http://website.tld/"
            }
          ],
          "dependency":"group:renamed:2.0.0"
        }
      ]
      """

    then: 'each library appears once at its highest version; the sibling stays separate'
    result.task(':licenseReport').outcome == SUCCESS
    assertJson(expectedJson, actualJson.text)
  }

  @Issue("jaredsburrows/gradle-license-plugin/issues/397")
  def 'ignoredPatterns does not ignore artifacts that merely extend the pattern'() {
    given: 'two artifacts where one coordinate is a prefix of the other'
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
        implementation 'group:kmpl:1.0.0'
        implementation 'group:kmpl-jdk7:2.0.0'
      }

      licenseReport {
        ignoredPatterns = ["group:kmpl"]
      }
      """

    when:
    BuildResult result = gradleWithCommand(testProjectDir.root, 'licenseReport', '-s')
    File actualJson = new File(reportFolder, 'licenseReport.json')

    then: 'only the exact artifact is ignored, not its -suffix sibling'
    result.task(':licenseReport').outcome == SUCCESS
    !actualJson.text.contains('"dependency":"group:kmpl:1.0.0"')
    actualJson.text.contains('"dependency":"group:kmpl-jdk7:2.0.0"')
  }

  @Issue("jaredsburrows/gradle-license-plugin/issues/488")
  def 'licenseReport as a dependency of processResources produces a populated report'() {
    given: 'processResources depends on licenseReport and bundles its reports'
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

      processResources {
        dependsOn 'licenseReport'
        from(layout.buildDirectory.dir('reports/licenses')) {
          into 'public/oss'
        }
      }
      """

    when: 'the report task runs as part of the build task graph, not directly'
    BuildResult result = gradleWithCommand(testProjectDir.root, 'build', '-s')
    File actualJson = new File(reportFolder, 'licenseReport.json')
    File bundledJson = new File(testProjectDir.root, 'build/resources/main/public/oss/licenseReport.json')
    String expectedJson =
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

    then: 'the report has the same content as when the task is invoked directly'
    result.task(':licenseReport').outcome == SUCCESS
    result.task(':processResources').outcome == SUCCESS
    assertJson(expectedJson, actualJson.text)
    bundledJson.exists()
    assertJson(expectedJson, bundledJson.text)
  }

  def 'licenseReport does not generate the full JSON report by default'() {
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
        implementation 'pl.droidsonroids.gif:android-gif-drawable:1.2.3'
      }
      """

    when:
    BuildResult result = gradleWithCommand(testProjectDir.root, 'licenseReport', '-s')
    File actualJsonFull = new File(reportFolder, 'licenseReport.full.json')

    then:
    result.task(':licenseReport').outcome == SUCCESS
    !actualJsonFull.exists()
    !result.output.find('Wrote Full JSON report to .*')
  }

  def 'licenseReport with generateJsonFullReport interns the license text'() {
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
        implementation 'com.android.support:design:26.1.0'
        implementation 'pl.droidsonroids.gif:android-gif-drawable:1.2.3'
        implementation 'group:name:1.0.0'
      }

      licenseReport {
        generateJsonFullReport = true
      }
      """

    when:
    BuildResult result = gradleWithCommand(testProjectDir.root, 'licenseReport', '-s')
    File actualJsonFull = new File(reportFolder, 'licenseReport.full.json')
    File actualJson = new File(reportFolder, 'licenseReport.json')
    Object report = new JsonSlurper().parseText(actualJsonFull.text)
    Object known = report.dependencies.find { it.dependency == 'pl.droidsonroids.gif:android-gif-drawable:1.2.3' }
    Object unknown = report.dependencies.find { it.dependency == 'group:name:1.0.0' }

    then:
    result.task(':licenseReport').outcome == SUCCESS
    result.output.find("Wrote Full JSON report to .*${reportFolder}/licenseReport.full.json.")
    actualJsonFull.exists()
    // The full report is written next to, and not instead of, the regular JSON report
    actualJson.exists()
    report.dependencies.size() == 4

    // Each license text is stored once, no matter how many dependencies share it
    report.license_texts.keySet() == ['apache-2.0', 'mit'] as Set
    report.license_texts['apache-2.0'] == getLicenseText('apache-2.0.txt')
    report.license_texts['mit'] == getLicenseText('mit.txt')

    // Everything the regular JSON report has, plus a key into license_texts
    known.project == 'Android GIF Drawable Library'
    known.description == 'Views and Drawable for displaying animated GIFs for Android'
    known.version == '1.2.3'
    known.url == 'https://github.com/koral--/android-gif-drawable'
    known.licenses.size() == 1
    known.licenses[0].license == 'The MIT License'
    known.licenses[0].license_url == 'http://opensource.org/licenses/MIT'
    known.licenses[0].license_key == 'mit'

    // Licenses the plugin does not bundle keep their POM name and url, but have no text to point at
    unknown.licenses[0].license == 'Some license'
    unknown.licenses[0].license_url == 'http://website.tld/'
    unknown.licenses[0].license_key == null
  }

  def 'licenseReport with generateJsonFullReport and no dependencies'() {
    given:
    buildFile <<
      """
      plugins {
        id 'java-library'
        id 'com.jaredsburrows.license'
      }

      licenseReport {
        generateJsonFullReport = true
      }
      """

    when:
    BuildResult result = gradleWithCommand(testProjectDir.root, 'licenseReport', '-s')
    File actualJsonFull = new File(reportFolder, 'licenseReport.full.json')
    Object report = new JsonSlurper().parseText(actualJsonFull.text)

    then:
    result.task(':licenseReport').outcome == SUCCESS
    actualJsonFull.exists()
    report.license_texts == [:]
    report.dependencies == []
  }

  def 'licenseReport terminates on a POM that is its own parent'() {
    given: 'a dependency whose POM names itself as its parent'
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
        implementation 'group:selfparent:1.0.0'
      }
      """

    when: 'the parent chain is walked'
    BuildResult result = gradleWithCommand(testProjectDir.root, 'licenseReport', '-s')

    then: 'it stops at the repeat instead of recursing until the stack overflows'
    result.task(':licenseReport').outcome == SUCCESS

    and: 'the dependency really was walked, so the test is not vacuous'
    new File(reportFolder, 'licenseReport.json').text.contains('Self parent')
  }



  def 'licenseReport renders a bundled license that is neither Apache nor MIT, in every format'() {
    given: 'a dependency under EPL-1.0, one of the licenses added in #843'
    buildFile <<
      """
      plugins {
        id 'java-library'
        id 'com.jaredsburrows.license'
      }

      licenseReport {
        generateCsvReport = true
        generateTextReport = true
        generateJsonFullReport = true
      }

      repositories {
        maven {
          url '${mavenRepoUrl}'
        }
      }

      dependencies {
        implementation 'group:eplib:1.0.0'
      }
      """

    when:
    BuildResult result = gradleWithCommand(testProjectDir.root, 'licenseReport', '-s')

    then:
    result.task(':licenseReport').outcome == SUCCESS

    and: 'the HTML inlines the full EPL text rather than only linking to it'
    String html = new File(reportFolder, 'licenseReport.html').text
    html.contains('Eclipse Public License - v 1.0')
    html.contains('EPL library')

    and: 'the full JSON carries it under its own key, not apache-2.0 or mit'
    Object full = new JsonSlurper().parseText(new File(reportFolder, 'licenseReport.full.json').text)
    full.license_texts.keySet() == ['epl-1.0'] as Set
    full.dependencies[0].licenses[0].license_key == 'epl-1.0'

    and: 'the CSV and text reports name it too'
    new File(reportFolder, 'licenseReport.csv').text.contains('Eclipse Public License 1.0')
    new File(reportFolder, 'licenseReport.txt').text.contains('EPL library')
  }


  def 'licenseReport keeps a -ktx sibling that merely shares a display name'() {
    given: 'two separately published libraries whose POMs happen to declare the same <name>'
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
        implementation 'group:ktxlib:1.0.0'
        implementation 'group:ktxlib-ktx:1.0.0'
      }
      """

    when:
    BuildResult result = gradleWithCommand(testProjectDir.root, 'licenseReport', '-s')
    String json = new File(reportFolder, 'licenseReport.json').text

    then:
    result.task(':licenseReport').outcome == SUCCESS

    and: 'both are attributed - dropping one loses a shipped library from the report'
    json.contains('group:ktxlib:1.0.0')
    json.contains('group:ktxlib-ktx:1.0.0')
  }


  def 'licenseReport does not attribute a BOM, which ships no code'() {
    given: 'a platform dependency alongside a real library it constrains'
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
        implementation platform('group:bom:1.0.0')
        implementation 'group:name:1.0.0'
      }
      """

    when:
    BuildResult result = gradleWithCommand(testProjectDir.root, 'licenseReport', '-s')
    String json = new File(reportFolder, 'licenseReport.json').text

    then:
    result.task(':licenseReport').outcome == SUCCESS

    and: 'the library it constrains is attributed'
    json.contains('group:name:1.0.0')

    and: 'the BOM itself is not - it carries no code to attribute'
    !json.contains('group:bom:1.0.0')
  }


  def 'licenseReport inherits url, description, inception year and developers from a parent POM'() {
    given: 'a dependency whose POM declares none of them and relies on its parent'
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
        implementation 'group:inheritchild:1.0.0'
      }
      """

    when:
    BuildResult result = gradleWithCommand(testProjectDir.root, 'licenseReport', '-s')
    Object json = new JsonSlurper().parseText(new File(reportFolder, 'licenseReport.json').text)
    Object entry = json.find { it.dependency == 'group:inheritchild:1.0.0' }

    then:
    result.task(':licenseReport').outcome == SUCCESS
    entry != null

    and: 'Maven inherits these four, so the report shows the parent values instead of blanks'
    entry.description == 'Inherited description'
    entry.url == 'https://github.com/user/inherited'
    entry.year == '2011'
    entry.developers == ['Inherited Developer']

    and: 'name is NOT inherited in Maven, so it still falls back to the artifact id'
    entry.project == 'inheritchild'
  }


  def 'licenseReport falls back to the developer id when the POM gives no name'() {
    given: 'a dependency whose only developer entry has an id and no name'
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
        implementation 'group:devid:1.0.0'
      }
      """

    when:
    BuildResult result = gradleWithCommand(testProjectDir.root, 'licenseReport', '-s')
    Object json = new JsonSlurper().parseText(new File(reportFolder, 'licenseReport.json').text)
    Object entry = json.find { it.dependency == 'group:devid:1.0.0' }

    then:
    result.task(':licenseReport').outcome == SUCCESS

    and: 'the id is used rather than leaving the copyright line blank'
    entry.developers == ['jsmith']
  }




  def 'the plugin works without the Kotlin Gradle Plugin or AGP on the buildscript classpath'() {
    given: 'the plugin classpath with every Kotlin and Android Gradle plugin jar removed'
    String withoutOptionalPlugins = getClass().classLoader.getResource('plugin-classpath.txt')
      .readLines()
      .findAll { !(it.contains('kotlin-gradle-plugin') || it.contains('com.android.tools')) }
      .collect { "'${new File(it).absolutePath.replace('\\', '\\\\')}'" }
      .join(', ')

    buildFile <<
      """
      buildscript {
        dependencies {
          classpath files($withoutOptionalPlugins)
        }
      }

      apply plugin: 'java-library'
      apply plugin: 'com.jaredsburrows.license'

      repositories {
        maven {
          url '${mavenRepoUrl}'
        }
      }

      dependencies {
        implementation 'group:name:1.0.0'
      }
      """

    when: 'a plain Java consumer runs the report'
    BuildResult result = gradleWithCommand(testProjectDir.root, 'licenseReport', '-s')

    then: 'compileOnly AGP and KGP types are never loaded, so nothing fails to resolve'
    result.task(':licenseReport').outcome == SUCCESS
    !result.output.contains('NoClassDefFoundError')
    !result.output.contains('ClassNotFoundException')

    and: 'the report is still produced'
    new File(reportFolder, 'licenseReport.json').text.contains('group:name:1.0.0')
  }

}
