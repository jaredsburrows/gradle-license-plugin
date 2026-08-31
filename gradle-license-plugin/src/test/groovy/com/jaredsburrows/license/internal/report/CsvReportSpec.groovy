package com.jaredsburrows.license.internal.report

import org.apache.maven.model.Developer
import org.apache.maven.model.License
import org.apache.maven.model.Model
import spock.lang.Specification

import static test.TestUtils.assertCsv

final class CsvReportSpec extends Specification {
  def 'no open source csv'() {
    given:
    List<Model> projects = []
    CsvReport sut = new CsvReport(projects)

    when:
    String actual = sut.toString()
    String expected = ""

    then:
    assertCsv(expected, actual)
  }

  def 'open source csv - missing values'() {
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
    CsvReport sut = new CsvReport(projects)

    when:
    String actual = sut.toString()
    String expected =
      "project,description,version,developers,url,year,licenses,license urls,dependency\n" +
        "name,,1.2.3,,,,,,foo:bar:1.2.3\n" +
        "name,,1.2.3,\"name,name\",,,,,foo:bar:1.2.3"

    then:
    assertCsv(expected, actual)
  }

  def 'open source csv - all values'() {
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
    CsvReport sut = new CsvReport(projects)

    when:
    String actual = sut.toString()
    String expected =
      "project,description,version,developers,url,year,licenses,license urls,dependency\n" +
        "name,description,1.2.3,\"name,name\",url,year,name,url,foo:bar:1.2.3\n" +
        "name,description,1.2.3,\"name,name\",url,year,name,url,foo:bar:1.2.3"

    then:
    assertCsv(expected, actual)
  }

  def 'open source csv - escape characters'() {
    given:
    Developer developerA = new Developer(id: 'Joe')
    Developer developerB = new Developer(id: '5\" Above Ground')
    List<Developer> developers = [developerA, developerB]
    License license = new License(
      name: 'Apache, 2.0',
      url: 'url'
    )
    Model project = new Model(
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
    List<Model> projects = [project]
    CsvReport sut = new CsvReport(projects)

    when:
    String actual = sut.toString()
    String expected =
      "project,description,version,developers,url,year,licenses,license urls,dependency\n" +
        "\"Joe\'s project\",\"Copyright \"\"Joe\"\" 2023\n\nAll right reserved\\to me\",1.2.3,\"Joe,5\"\" Above Ground\",url,year,\"Apache, 2.0\",url,foo:bar:1.2.3"

    then:
    assertCsv(expected, actual)
  }
}
