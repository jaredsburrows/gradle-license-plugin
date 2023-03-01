package com.jaredsburrows.license.internal.report

import static test.TestUtils.assertCsv

import org.apache.maven.model.Developer
import org.apache.maven.model.License
import org.apache.maven.model.Model
import spock.lang.Specification

final class CsvReportSpec extends Specification {
  def 'no open source csv'() {
    given:
    def projects = []
    def sut = new CsvReport(projects)

    when:
    def actual = sut.toString()
    def expected = ""

    then:
    assertCsv(expected, actual)
  }

  def 'open source csv - missing values'() {
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
    def sut = new CsvReport(projects)

    when:
    def actual = sut.toString()
    def expected =
      "project,description,version,developers,url,year,licenses,license urls,dependency\n" +
        "name,,1.2.3,,,,,,foo:bar:1.2.3\n" +
        "name,,1.2.3,\"name,name\",,,,,foo:bar:1.2.3"

    then:
    assertCsv(expected, actual)
  }

  def 'open source csv - all values'() {
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
    def sut = new CsvReport(projects)

    when:
    def actual = sut.toString()
    def expected =
      "project,description,version,developers,url,year,licenses,license urls,dependency\n" +
        "name,description,1.2.3,\"name,name\",url,year,name,url,foo:bar:1.2.3\n" +
        "name,description,1.2.3,\"name,name\",url,year,name,url,foo:bar:1.2.3"

    then:
    assertCsv(expected, actual)
  }

  def 'open source csv - escape characters'() {
    given:
    def developerA = new Developer(id: 'Joe')
    def developerB = new Developer(id: '5\" Above Ground')
    def developers = [developerA, developerB]
    def license = new License(
      name: 'Apache, 2.0',
      url: 'url'
    )
    def project = new Model(
      name: "Joe's project",
      description: 'Copyright "Joe" 2023\n\nAll right reserved\\to me',
      licenses: [license],
      url: 'url',
      developers: developers,
      inceptionYear: 'year',
      groupId: 'foo',
      artifactId: 'bar',
      version: '1.2.3',
    )
    def projects = [project]
    def sut = new CsvReport(projects)

    when:
    def actual = sut.toString()
    def expected =
      "project,description,version,developers,url,year,licenses,license urls,dependency\n" +
        "\"Joe\'s project\",\"Copyright \"\"Joe\"\" 2023\n\nAll right reserved\\to me\",1.2.3,\"Joe,5\"\" Above Ground\",url,year,\"Apache, 2.0\",url,foo:bar:1.2.3"

    then:
    assertCsv(expected, actual)
  }
}
