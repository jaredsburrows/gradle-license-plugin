package com.jaredsburrows.license.internal.report

import org.apache.maven.model.Developer
import org.apache.maven.model.License
import org.apache.maven.model.Model
import spock.lang.Specification

final class TextReportSpec extends Specification {
  def 'no open source text'() {
    given:
    def projects = []
    def sut = new TextReport(projects)

    when:
    def actual = sut.toString().stripIndent().trim()
    def expected = "".stripIndent().trim()

    then:
    expected == actual
  }

  def 'open source text - missing values'() {
    given:
    def developer = new Developer(id: 'developer-name')
    def project1 = new Model(
      name: 'project-name',
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
      name: 'project-name',
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
    def sut = new TextReport(projects)

    when:
    def actual = sut.toString().stripIndent().trim()
    def expected =
      """
      Notice for packages


      project-name (1.2.3)

      project-name (1.2.3)
      """.stripIndent().trim()

    then:
    expected == actual
  }

  def 'open source text - all values'() {
    given:
    def developer = new Developer(id: 'name')
    def developers = [developer, developer]
    def license = new License(
      name: 'license-name',
      url: 'license-url'
    )
    def project = new Model(
      name: 'project-name',
      description: 'project-description',
      licenses: [license],
      url: 'project-url',
      developers: developers,
      inceptionYear: 'project-year',
      groupId: 'foo',
      artifactId: 'bar',
      version: '1.2.3',
    )
    def projects = [project, project]
    def sut = new TextReport(projects)

    when:
    def actual = sut.toString().stripIndent().trim()
    def expected =
      """
      Notice for packages


      project-name (1.2.3) - license-name
      project-description
      project-url

      project-name (1.2.3) - license-name
      project-description
      project-url
      """.stripIndent().trim()

    then:
    expected == actual
  }
}
