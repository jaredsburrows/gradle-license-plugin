package com.jaredsburrows.license.internal.report

import org.apache.maven.model.Developer
import org.apache.maven.model.License
import org.apache.maven.model.Model
import groovy.transform.TypeChecked
import spock.lang.Specification

@TypeChecked
final class TextReportSpec extends Specification {
  def 'no open source text'() {
    given:
    List<Model> projects = []
    TextReport sut = new TextReport(projects)

    when:
    String actual = sut.toString().stripIndent().trim()
    String expected = "".stripIndent().trim()

    then:
    expected == actual
  }

  def 'open source text - missing values'() {
    given:
    Developer developer = new Developer(id: 'developer-name')
    Model project1 = new Model(
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
    Model project2 = new Model(
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
    List<Model> projects = [project1, project2]
    TextReport sut = new TextReport(projects)

    when:
    String actual = sut.toString().stripIndent().trim()
    String expected =
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
    Developer developer = new Developer(id: 'name')
    List<Developer> developers = [developer, developer]
    License license = new License(
      name: 'license-name',
      url: 'license-url'
    )
    Model project = new Model(
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
    List<Model> projects = [project, project]
    TextReport sut = new TextReport(projects)

    when:
    String actual = sut.toString().stripIndent().trim()
    String expected =
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
