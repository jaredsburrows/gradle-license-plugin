package com.jaredsburrows.license.internal.report

import com.jaredsburrows.license.internal.pom.Developer
import com.jaredsburrows.license.internal.pom.License
import com.jaredsburrows.license.internal.pom.Project
import spock.lang.Specification

final class TextReportSpec extends Specification {
  def 'no open source'() {
    given:
    def projects = []
    def sut = new TextReport(projects)

    when:
    def actual = sut.toString()
    def expected = ""

    then:
    expected == actual
  }

  def 'open source - missing values'() {
    given:
    def developer = new Developer(name: 'developer-name')
    def project1 = new Project(
      name: 'project-name',
      developers: [],
      gav: 'foo:bar:1.2.3'
    )
    def project2 = new Project(
      name: 'project-name',
      developers: [developer, developer],
      gav: 'foo:bar:1.2.3'
    )
    def projects = [project1, project2]
    def sut = new TextReport(projects)

    when:
    def actual = sut.toString().stripIndent().trim()
    def expected =
      """
      Notice for packages


      project-name


      project-name
      """.stripIndent().trim()

    then:
    expected == actual
  }

  def 'open source - all values'() {
    given:
    def developer = new Developer(name: 'developer-name')
    def developers = [developer, developer]
    def license = new License(
      name: 'license-name',
      url: 'license-url'
    )
    def project = new Project(
      name: 'project-name',
      description: 'project-description',
      version: '1.0.0',
      licenses: [license],
      url: 'project-url',
      developers: developers,
      year: 'project-year',
      gav: 'foo:bar:1.2.3'
    )
    def projects = [project, project]
    def sut = new TextReport(projects)

    when:
    def actual = sut.toString().stripIndent().trim()
    def expected =
      """
      Notice for packages


      project-name (1.0.0) - license-name
      project-description
      project-url

      project-name (1.0.0) - license-name
      project-description
      project-url
      """.stripIndent().trim()

    then:
    expected == actual
  }
}
