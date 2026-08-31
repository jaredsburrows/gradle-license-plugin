package com.jaredsburrows.license.internal.report

import com.jaredsburrows.license.internal.LicenseHelper
import org.apache.maven.model.Developer
import org.apache.maven.model.License
import org.apache.maven.model.Model
import spock.lang.Specification
import spock.lang.Unroll

import static test.TestUtils.assertHtml

final class HtmlReportSpec extends Specification {
  def 'no open source html'() {
    given:
    List<Model> projects = []
    HtmlReport report = new HtmlReport(projects, true)

    when:
    String actual = report.toString()
    String expected =
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
    Developer developer = new Developer(id: 'name')
    List<Developer> developers = [developer, developer]
    License license = new License(
      name: 'name',
      url: 'url'
    )
    Model project = new Model(
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
    Model missingLicensesProject = new Model(
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
    List<Model> projects = [project, project, missingLicensesProject]
    HtmlReport sut = new HtmlReport(projects, true)

    when:
    String actual = sut.toString()
    String expected =
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
    Model project = new Model(
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
    String actual = new HtmlReport([project], false).toString()

    then: 'the text is inlined rather than linked'
    actual.contains('Permission is hereby granted, free of charge')
    !actual.contains('<a href="https://spdx.org/licenses/MIT.html">')
  }

  private static Model projectWith(License license) {
    new Model(
      name: 'name',
      description: '',
      licenses: [license],
      url: '',
      developers: [],
      inceptionYear: '',
      groupId: 'foo',
      artifactId: 'bar',
      version: '1.2.3',
    )
  }

  @Unroll
  def 'bundled license text is escaped, not swallowed as markup - #spdxId'() {
    given: 'a license whose bundled text contains angle-bracketed placeholders'
    HtmlReport report = new HtmlReport([projectWith(new License(name: spdxId, url: ''))], true)

    when:
    String actual = report.toString()

    then: 'the placeholder survives, escaped'
    actual.contains(escaped)

    and: 'it is not emitted raw, where a browser would parse it as a tag and drop it'
    !actual.contains(raw)

    where:
    spdxId     | raw                | escaped
    'GPL-3.0'  | '<year>'           | '&lt;year&gt;'
    'GPL-3.0'  | '<name of author>' | '&lt;name of author&gt;'
    'GPL-3.0'  | '<program>'        | '&lt;program&gt;'
    'GPL-2.0'  | '<year>'           | '&lt;year&gt;'
    'AGPL-3.0' | '<year>'           | '&lt;year&gt;'
    'LGPL-2.1' | '<year>'           | '&lt;year&gt;'
  }

  def 'every bundled license text survives rendering intact'() {
    expect: 'nothing between angle brackets is lost from any of the bundled texts'
    LicenseHelper.INSTANCE.bundledFileNames().every { fileName ->
      String text = LicenseHelper.INSTANCE.licenseText(fileName)
      String spdxId = LicenseHelper.INSTANCE.allAliases().find { alias, file -> file == fileName }?.key
      String html = new HtmlReport([projectWith(new License(name: spdxId, url: ''))], true).toString()
      // Every angle-bracketed run in the source text must appear escaped in the output.
      (text =~ /<[^>\n]{1,60}>/).collect { it }.every { fragment ->
        html.contains(fragment.replace('&', '&amp;').replace('<', '&lt;').replace('>', '&gt;'))
      }
    }
  }

  @Unroll
  def 'license metadata from a POM cannot inject markup - #description'() {
    given: 'a dependency POM carrying markup in its license fields'
    HtmlReport report = new HtmlReport([projectWith(new License(name: name, url: url))], true)

    when:
    String actual = report.toString()

    then: 'the markup is escaped rather than emitted as elements'
    !actual.contains(mustNotContain)

    where:
    description            | name                        | url                          | mustNotContain
    'a script tag in name' | '<script>alert(1)</script>' | 'http://website.tld/'        | '<script>'
    'a script tag in url'  | 'Some license'              | 'http://x/<script>a()' + '</script>' | '<script>'
    'an attribute break'   | 'Some license'              | 'http://x/" onmouseover="a()' | 'onmouseover="a()"'
    'an img onerror'       | '<img src=x onerror=a()>'   | 'http://website.tld/'        | '<img src=x'
  }

  def 'an unknown license still renders its url as a working link'() {
    given:
    HtmlReport report = new HtmlReport([projectWith(new License(name: 'Some license', url: 'http://website.tld/'))], true)

    when:
    String actual = report.toString()

    then: 'escaping did not cost us the anchor'
    actual.contains('<a href="http://website.tld/">http://website.tld/</a>')
    actual.contains('Some license')
  }

  def 'showVersions leaves the version out when disabled'() {
    given:
    Model project = new Model(
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
