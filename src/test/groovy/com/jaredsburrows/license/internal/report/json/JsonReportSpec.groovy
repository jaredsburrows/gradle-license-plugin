package com.jaredsburrows.license.internal.report.json

import com.jaredsburrows.license.internal.pom.Developer
import com.jaredsburrows.license.internal.pom.License
import com.jaredsburrows.license.internal.pom.Project
import spock.lang.Specification

/**
 * @author <a href="mailto:jaredsburrows@gmail.com">Jared Burrows</a>
 */
final class JsonReportSpec extends Specification {
  def "test noOpenSourceJson"() {
    given:
    def projects = []
    def sut = new JsonReport(projects)

    when:
    def actual = sut.string().trim()
    def expected =
      """
[]
""".trim()

    then:
    actual == expected
  }

  def "test openSourceJson - missing values"() {
    given:
    def license = new License(name: "name", url: "url")
    def project = new Project(name: "name", license: license)
    def projects = [project, project]
    def sut = new JsonReport(projects)

    when:
    def actual = sut.string().trim()
    def expected =
      """
[
    {
        "project": "name",
        "developers": null,
        "url": null,
        "year": null,
        "license": "name",
        "license_url": "url"
    },
    {
        "project": "name",
        "developers": null,
        "url": null,
        "year": null,
        "license": "name",
        "license_url": "url"
    }
]
""".trim()

    then:
    actual == expected
  }

  def "test openSourceJson - all values"() {
    given:
    def developer = new Developer(name: "name")
    def developers = [developer, developer]
    def license = new License(name: "name", url: "url")
    def project = new Project(name: "name", license: license, url: "url", developers: developers, year: "year")
    def projects = [project, project]
    def sut = new JsonReport(projects)

    when:
    def actual = sut.string().trim()
    def expected =
      """
[
    {
        "project": "name",
        "developers": "name, name",
        "url": "url",
        "year": "year",
        "license": "name",
        "license_url": "url"
    },
    {
        "project": "name",
        "developers": "name, name",
        "url": "url",
        "year": "year",
        "license": "name",
        "license_url": "url"
    }
]
""".trim()

    then:
    actual == expected
  }
}
