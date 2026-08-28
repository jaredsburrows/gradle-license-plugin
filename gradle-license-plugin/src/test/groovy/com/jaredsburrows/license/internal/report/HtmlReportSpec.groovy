package com.jaredsburrows.license.internal.report

import com.jaredsburrows.license.internal.LicenseHelper
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
          <pre id="0">No license found</pre>
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
          <pre id="87638953">name
          <a href="url">url</a></pre>
          <br>
          <hr>
        </body>
      </html>
      """

    then:
    assertHtml(expected, actual)
  }

  def 'getLicenseText returns the bundled text of a known license'() {
    expect: 'the same text LicenseHelper resolves, so the HTML and JSON reports cannot drift apart'
    HtmlReport.getLicenseText('apache-2.0.txt') == LicenseHelper.INSTANCE.licenseText('apache-2.0.txt')
  }

  def 'getLicenseText reports a license it cannot find rather than failing'() {
    expect:
    HtmlReport.getLicenseText('does-not-exist.txt') == 'Missing standard license text for: does-not-exist.txt'
  }

  def 'a bundled license is rendered as its full text'() {
    given: 'a license the plugin bundles, matched by name because the url is not in the map'
    def project = new Model(
      name: 'name',
      description: 'description',
      licenses: [new License(name: 'The MIT License', url: 'https://spdx.org/licenses/MIT.html')],
      url: 'url',
      developers: [],
      inceptionYear: 'year',
      groupId: 'foo',
      artifactId: 'bar',
      version: '1.2.3',
    )

    when:
    def actual = new HtmlReport([project], false).toString()

    then: 'the text is inlined rather than linked'
    actual.contains('Permission is hereby granted, free of charge')
    !actual.contains('<a href="https://spdx.org/licenses/MIT.html">')
  }

  def 'showVersions leaves the version out when disabled'() {
    given:
    def project = new Model(
      name: 'name',
      description: '',
      licenses: [],
      url: '',
      developers: [],
      inceptionYear: '',
      groupId: 'foo',
      artifactId: 'bar',
      version: '1.2.3',
    )

    expect:
    new HtmlReport([project], true).toString().contains('name (1.2.3)')
    !new HtmlReport([project], false).toString().contains('name (1.2.3)')
  }
}
