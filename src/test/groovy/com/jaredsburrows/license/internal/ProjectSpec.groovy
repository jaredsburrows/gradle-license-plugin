package com.jaredsburrows.license.internal

import spock.lang.Specification

/**
 * @author <a href="mailto:jaredsburrows@gmail.com">Jared Burrows</a>
 */
final class ProjectSpec extends Specification {
  def license = License.builder().name("name").url("url").build()
  def sut = Project.builder().name("name").license(license).url("url").authors("authors").year("year").build()

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

  def "test authors"() {
    expect:
    sut.authors == "authors"
    sut.getAuthors() == "authors"
  }

  def "test year"() {
    expect:
    sut.year == "year"
    sut.getYear() == "year"
  }

  def "test equals/hashcode"() {
    given:
    def one = Project.builder().name("name").license(license).url("url").authors("authors").year("year").build()
    def two = Project.builder().name("name").license(license).url("url").authors("authors").year("year").build()

    expect:
    // Values
    one.name == two.name
    one.url == two.url
    // Auto generated
    one.hashCode() == two.hashCode()
    // one == two
    one.toString() == two.toString()
  }
}
