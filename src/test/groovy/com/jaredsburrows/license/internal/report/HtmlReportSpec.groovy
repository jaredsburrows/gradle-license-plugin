package com.jaredsburrows.license.internal.report

import static test.TestUtils.assertHtml

import org.apache.maven.model.Developer
import org.apache.maven.model.License
import org.apache.maven.model.Model
import spock.lang.Specification

final class HtmlReportSpec extends Specification {
  def 'no open source html'() {
    given:
    def projects = []
    def report = new HtmlReport(projects)

    when:
    def actual = report.toString()
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

    then:
    assertHtml(expected, actual)
  }

  def 'open source html'() {
    given:
    def developer = new Developer(id: 'name')
    def developers = [developer, developer]
    def license = new License(
      name: 'name',
      url: 'url'
    )
    def project = new Model(
      name: 'name',
      description: 'description',
      licenses: [license],
      url: 'url',
      developers: developers,
      inceptionYear: 'year',
      groupId: 'foo',
      artifactId: 'bar',
      version: '1.2.3',
    )
    def missingLicensesProject = new Model(
      name: 'name',
      description: '',
      licenses: [],
      url: '',
      developers: [developer, developer],
      inceptionYear: '',
      groupId: 'foo',
      artifactId: 'bar',
      version: '1.2.3',
    )
    def projects = [project, project, missingLicensesProject]
    def sut = new HtmlReport(projects)

    when:
    def actual = sut.toString()
    def expected =
      """
      <html>
        <head>
          <style>body { font-family: sans-serif } pre { background-color: #eeeeee; padding: 1em; white-space: pre-wrap; display: inline-block }</style>
          <title>Open source licenses</title>
        </head>
        <body>
          <h3>Notice for packages:</h3>
          <ul>
            <li><a href="#87638953">name (1.2.3)</a>
              <dl>
                <dt>Copyright &copy; year name</dt>
                <dt>Copyright &copy; year name</dt>
              </dl>
            </li>
            <li><a href="#87638953">name (1.2.3)</a>
              <dl>
                <dt>Copyright &copy; year name</dt>
                <dt>Copyright &copy; year name</dt>
              </dl>
            </li>
      <a name="87638953"></a>
            <pre>name
      <a href="url">url</a></pre>
      <br>
            <hr>
            <li><a href="#0">name (1.2.3)</a>
              <dl>
                <dt>Copyright &copy; 20xx name</dt>
                <dt>Copyright &copy; 20xx name</dt>
              </dl>
            </li>
      <a name="0"></a>
            <pre>No license found</pre>
            <hr>
          </ul>
        </body>
      </html>
      """

    then:
    assertHtml(expected, actual)
  }
}
