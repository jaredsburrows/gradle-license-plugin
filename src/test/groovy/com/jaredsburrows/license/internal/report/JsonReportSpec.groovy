package com.jaredsburrows.license.internal.report

import static test.TestUtils.assertJson

import com.jaredsburrows.license.internal.pom.Developer
import com.jaredsburrows.license.internal.pom.License
import com.jaredsburrows.license.internal.pom.Project
import spock.lang.Specification

final class JsonReportSpec extends Specification {
  def 'no open source json'() {
    given:
    def projects = []
    def sut = new JsonReport(projects)

    when:
    def actual = sut.toString()
    def expected =
      """
      []
      """

    then:
    assertJson(expected, actual)
  }

  def 'open source json - missing values'() {
    given:
    def developer = new Developer(name: 'name')
    def project1 = new Project(
      name: 'name',
      developers: [],
      gav: 'foo:bar:1.2.3'
    )
    def project2 = new Project(
      name: 'name',
      developers: [developer, developer],
      gav: 'foo:bar:1.2.3'
    )
    def projects = [project1, project2]
    def sut = new JsonReport(projects)

    when:
    def actual = sut.toString()
    def expected =
      """
      [
        {
          "project": "name",
          "description": null,
          "version": null,
          "developers": [],
          "url": null,
          "year": null,
          "licenses": [],
          "dependency": "foo:bar:1.2.3"
        },
        {
          "project": "name",
          "description": null,
          "version": null,
          "developers": [
            "name",
            "name"
          ],
          "url": null,
          "year": null,
          "licenses": [],
          "dependency": "foo:bar:1.2.3"
        }
      ]
      """

    then:
    assertJson(expected, actual)
  }

  def 'open source json - all values'() {
    given:
    def developer = new Developer(name: 'name')
    def developers = [developer, developer]
    def license = new License(
      name: 'name',
      url: 'url'
    )
    def project = new Project(
      name: 'name',
      description: 'description',
      version: '1.0.0',
      licenses: [license],
      url: 'url',
      developers: developers,
      year: 'year',
      gav: 'foo:bar:1.2.3'
    )
    def projects = [project, project]
    def sut = new JsonReport(projects)

    when:
    def actual = sut.toString()
    def expected =
      """
      [
        {
          "project": "name",
          "description": "description",
          "version": "1.0.0",
          "developers": [
            "name",
            "name"
          ],
          "url": "url",
          "year": "year",
          "licenses": [
            {
              "license": "name",
              "license_url": "url"
            }
          ],
          "dependency": "foo:bar:1.2.3"
        },
        {
          "project": "name",
          "description": "description",
          "version": "1.0.0",
          "developers": [
            "name",
            "name"
          ],
          "url": "url",
          "year": "year",
          "licenses": [
            {
              "license": "name",
              "license_url": "url"
            }
          ],
          "dependency": "foo:bar:1.2.3"
        }
      ]
      """

    then:
    assertJson(expected, actual)
  }
}
