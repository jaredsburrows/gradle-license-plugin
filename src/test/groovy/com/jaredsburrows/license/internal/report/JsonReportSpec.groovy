package com.jaredsburrows.license.internal.report

import static test.TestUtils.assertJson

import org.apache.maven.model.Developer
import org.apache.maven.model.License
import org.apache.maven.model.Model
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
    def developer = new Developer(id: 'name')
    def project1 = new Model(
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
    def project2 = new Model(
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
          "version": "1.2.3",
          "developers": [],
          "url": null,
          "year": null,
          "licenses": [],
          "dependency": "foo:bar:1.2.3"
        },
        {
          "project": "name",
          "description": null,
          "version": "1.2.3",
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
          "version": "1.2.3",
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
          "version": "1.2.3",
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
