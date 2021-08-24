package com.jaredsburrows.license.internal.report

import static test.TestUtils.assertCsv

import com.jaredsburrows.license.internal.pom.Developer
import com.jaredsburrows.license.internal.pom.License
import com.jaredsburrows.license.internal.pom.Project
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
    def sut = new CsvReport(projects)

    when:
    def actual = sut.toString()
    def expected =
      "project,description,version,developers,url,year,licenses,license urls,dependency\n" +
        "name,null,null,null,null,null,null,null,foo:bar:1.2.3\n" +
        "name,null,null,\"name,name\",null,null,null,null,foo:bar:1.2.3"

    then:
    assertCsv(expected, actual)
  }

  def 'open source csv - all values'() {
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
    def sut = new CsvReport(projects)

    when:
    def actual = sut.toString()
    def expected =
      "project,description,version,developers,url,year,licenses,license urls,dependency\n" +
        "name,description,1.0.0,\"name,name\",url,year,name,url,foo:bar:1.2.3\n" +
        "name,description,1.0.0,\"name,name\",url,year,name,url,foo:bar:1.2.3"

    then:
    assertCsv(expected, actual)
  }
}
