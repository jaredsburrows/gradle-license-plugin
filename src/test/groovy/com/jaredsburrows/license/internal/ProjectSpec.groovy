package com.jaredsburrows.license.internal

import spock.lang.Specification

/**
 * @author <a href="mailto:jaredsburrows@gmail.com">Jared Burrows</a>
 */
final class ProjectSpec extends Specification {
  def license = new License(name: "name", url: "url")
  def sut = new Project(name: "name", license: license, url: "url", developers: "developers", year: "year")

  def "test name"() {
    expect:
    sut.name == "name"
    sut.getName() == "name"
  }

  def "test license"() {
    expect:
    sut.license == license
    sut.getLicense() == license
  }

  def "test url"() {
    expect:
    sut.url == "url"
    sut.getUrl() == "url"
  }

  def "test developers"() {
    expect:
    sut.developers == "developers"
    sut.getDevelopers() == "developers"
  }

  def "test year"() {
    expect:
    sut.year == "year"
    sut.getYear() == "year"
  }
}
