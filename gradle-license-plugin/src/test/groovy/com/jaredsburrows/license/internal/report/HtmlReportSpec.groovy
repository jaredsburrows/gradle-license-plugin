package com.jaredsburrows.license.internal.report

import org.apache.maven.model.Developer
import org.apache.maven.model.License
import org.apache.maven.model.Model
import spock.lang.Specification

import static test.TestUtils.assertHtml

final class HtmlReportSpec extends Specification {
  def 'no open source html'() {
    given:
    def projects = []
    def report = new HtmlReport(projects, true)

    when:
    def actual = report.toString()
    def expected =
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
    def sut = new HtmlReport(projects, true)

    when:
    def actual = sut.toString()
    def expected =
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
              <a href="#0">name (1.2.3)</a>
              <dl>
                <dt>Copyright &copy; 20xx name</dt>
                <dd></dd>
                <dt>Copyright &copy; 20xx name</dt>
                <dd></dd>
              </dl>
            </li>
          </ul>
          <a id="0"></a>
          <pre>No license found</pre>
          <hr>
          <ul>
            <li>
              <a href="#87638953">name (1.2.3)</a>
              <dl>
                <dt>Copyright &copy; year name</dt>
                <dd></dd>
                <dt>Copyright &copy; year name</dt>
                <dd></dd>
              </dl>
            </li>
            <li>
              <a href="#87638953">name (1.2.3)</a>
              <dl>
                <dt>Copyright &copy; year name</dt>
                <dd></dd>
                <dt>Copyright &copy; year name</dt>
                <dd></dd>
              </dl>
            </li>
          </ul>
          <a id="87638953"></a>
          <pre>name
          <a href="url">url</a></pre>
          <br>
          <hr>
        </body>
      </html>
      """

    then:
    assertHtml(expected, actual)
  }
}
