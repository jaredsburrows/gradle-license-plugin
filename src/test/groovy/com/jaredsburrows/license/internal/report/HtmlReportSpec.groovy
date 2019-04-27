package com.jaredsburrows.license.internal.report

import com.jaredsburrows.license.internal.pom.Developer
import com.jaredsburrows.license.internal.pom.License
import com.jaredsburrows.license.internal.pom.Project
import org.xmlunit.builder.DiffBuilder
import org.xmlunit.builder.Input
import spock.lang.Specification

final class HtmlReportSpec extends Specification {
  def 'no open source html'() {
    given:
    def projects = []
    def report = new HtmlReport(projects)

    when:
    def actual = report.string()
    def expected =
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

    def diff = DiffBuilder.compare(Input.fromString(actual).build())
      .withTest(Input.fromString(expected).build())
      .normalizeWhitespace()
      .ignoreWhitespace()
      .build()

    then:
    !diff.hasDifferences()
  }

  def 'open source html'() {
    given:
    def developer = new Developer(name: 'name')
    def developers = [developer, developer]
    def license = new License(
      name: 'name', url: 'url'
    )
    def project = new Project(
      name: 'name',
      licenses: [license],
      url: 'url',
      developers: developers,
      year: 'year'
    )
    def missingLicensesProject = new Project(
      name: 'name',
      url: 'url',
      developers: developers,
      year: 'year'
    )
    def projects = [project, project, missingLicensesProject]
    def sut = new HtmlReport(projects)

    when:
    def actual = sut.string()
    def expected =
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
          <h3>Notice for packages:</h3>
          <ul>
            <li>
              <a href="#0">name</a>
            </li>
            <a name="0" />
            <pre>No license found</pre>
            <li>
              <a href="#116079">name</a>
            </li>
            <li>
              <a href="#116079">name</a>
            </li>
            <a name="116079" />
            <pre>name
            <a href="url">url</a></pre>
          </ul>
        </body>
      </html>
      """

    def diff = DiffBuilder.compare(Input.fromString(actual).build())
      .withTest(Input.fromString(expected).build())
      .normalizeWhitespace()
      .ignoreWhitespace()
      .build()

    then:
    !diff.hasDifferences()
  }
}
