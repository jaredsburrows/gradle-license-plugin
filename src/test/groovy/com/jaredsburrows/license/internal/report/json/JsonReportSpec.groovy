package com.jaredsburrows.license.internal.report.json

import com.jaredsburrows.license.internal.License
import com.jaredsburrows.license.internal.Project
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

  def "test openSourceJson"() {
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
    def projects = [project, project]
    def sut = new JsonReport(projects)

    when:
    def actual = sut.string().trim()
    def expected =
      """
[
    {
        "project": "name",
        "developers": "developers",
        "url": "url",
        "year": "year",
        "license": "name",
        "license_url": "url"
    },
    {
        "project": "name",
        "developers": "developers",
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
