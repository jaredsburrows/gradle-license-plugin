package com.jaredsburrows.license.internal.pom

import spock.lang.Specification

/**
 * @author <a href="mailto:jaredsburrows@gmail.com">Jared Burrows</a>
 */
final class ProjectSpec extends Specification {
  def developer = new Developer(name: "name")
  def developers = [developer, developer]
  def license = new License(name: "name", url: "url")
  def sut = new Project(name: "name", license: license, url: "url", developers: developers, year: "year")

  def "name"() {
    expect:
    sut.name == "name"
    sut.getName() == "name"
  }

  def "license"() {
    expect:
    sut.license == license
    sut.getLicense() == license
  }

  def "url"() {
    expect:
    sut.url == "url"
    sut.getUrl() == "url"
  }

  def "developers"() {
    expect:
    sut.developers == developers
    sut.getDevelopers() == developers
  }

  def "year"() {
    expect:
    sut.year == "year"
    sut.getYear() == "year"
  }
}
