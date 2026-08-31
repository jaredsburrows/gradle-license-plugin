package com.jaredsburrows.license.internal.report

import org.apache.maven.model.Developer
import org.apache.maven.model.License
import org.apache.maven.model.Model
import spock.lang.Specification

import static test.TestUtils.assertJson

final class JsonReportSpec extends Specification {
  def 'no open source json'() {
    given:
    List<Model> projects = []
    JsonReport sut = new JsonReport(projects)

    when:
    String actual = sut.toString()
    String expected =
      """
      []
      """

    then:
    assertJson(expected, actual)
  }

  def 'open source json - missing values'() {
    given:
    Developer developer = new Developer(id: 'name')
    Model project1 = new Model(
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
    Model project2 = new Model(
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
    List<Model> projects = [project1, project2]
    JsonReport sut = new JsonReport(projects)

    when:
    String actual = sut.toString()
    String expected =
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
    List<Model> projects = [project, project]
    JsonReport sut = new JsonReport(projects)

    when:
    String actual = sut.toString()
    String expected =
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
