package com.jaredsburrows.license.internal.report

import com.jaredsburrows.license.internal.pom.Developer
import com.jaredsburrows.license.internal.pom.License
import com.jaredsburrows.license.internal.pom.Project
import spock.lang.Specification

/**
 * @author <a href="mailto:jaredsburrows@gmail.com">Jared Burrows</a>
 */
final class HtmlReportSpec extends Specification {
  def "no open source html"() {
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

  def "open source html"() {
    given:
    def developer = new Developer(name: "name")
    def developers = [developer, developer]
    def license = new License(name: "name", url: "url")
    def project = new Project(name: "name", license: license, url: "url", developers: developers, year: "year")
    def projects = [project, project]
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
