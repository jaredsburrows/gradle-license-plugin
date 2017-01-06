package com.jaredsburrows.license.internal.report.html

import com.jaredsburrows.license.internal.License
import com.jaredsburrows.license.internal.Project
import spock.lang.Specification

/**
 * @author <a href="mailto:jaredsburrows@gmail.com">Jared Burrows</a>
 */
final class HtmlReportSpec extends Specification {
  def "test noOpenSourceHtml"() {
    given:
    def projects = []
    def sut = new HtmlReport(projects)

    when:
    def actual = sut.string().trim()
    def expected =
      """
<html>
  <head>
    <style>body{font-family:sans-serif;}pre{background-color:#eee;padding:1em;white-space:pre-wrap;}</style>
    <title>Open source licenses</title>
  </head>
  <body>
    <h3>No open source libraries</h3>
  </body>
</html>
""".trim()

    then:
    actual == expected
  }

  def "test openSourceHtml"() {
    given:
    def license = License.builder()
      .name("name")
      .url("url")
      .build()
    def project = Project.builder()
      .name("name")
      .license(license)
      .url("url")
      .developers("developers")
      .year("year")
      .build()
    def projects = [project]
    def sut = new HtmlReport(projects)

    when:
    def actual = sut.string().trim()
    def expected =
      """
<html>
  <head>
    <style>body{font-family:sans-serif;}pre{background-color:#eee;padding:1em;white-space:pre-wrap;}</style>
    <title>Open source licenses</title>
  </head>
  <body>
    <h3>Notice for libraries:</h3>
    <ul>
      <li>
        <a href='#120016'>name</a>
      </li>
    </ul>
    <a name='120016' />
    <h3>name</h3>
    <pre>name, url</pre>
  </body>
</html>
""".trim()

    then:
    actual == expected
  }
}
