package com.jaredsburrows.license.internal

import spock.lang.Specification

/**
 * @author <a href="mailto:jaredsburrows@gmail.com">Jared Burrows</a>
 */
final class ProjectSpec extends Specification {
  def license = new License.Builder().name("name").url("url").build()
  def sut = new Project.Builder().name("name").license(license).url("url").authors("authors").year("year").build()

  def "test name"() {
    expect:
    sut.name == "name"
    sut.getName() == "name"
  }

  def "test no name"() {
    when:
    sut = sut.newBuilder().name(null).build()

    then:
    !sut.name
  }

  def "test license"() {
    expect:
    sut.license == license
    sut.getLicense() == license
  }

  def "test no license"() {
    when:
    sut = sut.newBuilder().license(null).build()

    then:
    !sut.license
  }

  def "test url"() {
    expect:
    sut.url == "url"
    sut.getUrl() == "url"
  }

  def "test no url"() {
    when:
    sut = sut.newBuilder().url(null).build()

    then:
    !sut.url
  }

  def "test authors"() {
    expect:
    sut.authors == "authors"
    sut.getAuthors() == "authors"
  }

  def "test no authors"() {
    when:
    sut = sut.newBuilder().authors(null).build()

    then:
    !sut.authors
  }

  def "test year"() {
    expect:
    sut.year == "year"
    sut.getYear() == "year"
  }

  def "test no year"() {
    when:
    sut = sut.newBuilder().year(null).build()

    then:
    !sut.year
  }

  def "test equals/hashcode"() {
    given:
    def one = new Project.Builder().name("name").license(license).url("url").authors("authors").year("year").build()
    def two = new Project.Builder().name("name").license(license).url("url").authors("authors").year("year").build()

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
