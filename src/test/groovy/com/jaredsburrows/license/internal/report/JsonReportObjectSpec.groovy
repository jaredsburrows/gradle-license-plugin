package com.jaredsburrows.license.internal.report

import com.jaredsburrows.license.internal.License
import com.jaredsburrows.license.internal.report.json.JsonReportObject
import spock.lang.Specification

/**
 * @author <a href="mailto:jaredsburrows@gmail.com">Jared Burrows</a>
 */
final class JsonReportObjectSpec extends Specification {
  def license = new License.Builder().name("name").url("url").build()
  def sut = new JsonReportObject.Builder().name("name").authors("authors").url("url").year("year").license(license).build()

  def "test get name"() {
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

  def "test to json"() {
    given:
    def json = sut.jsonObject()

    expect:
    json.toString() == "[project:name, authors:authors, url:url, year:year, license:name, license_url:url]"
  }

  def "test to json with missing values"() {
    given:
    def json = sut.newBuilder().authors(null).url(null).year(null).license(null).build().jsonObject()

    expect:
    json.toString() == "[project:name]"
  }

  def "test equals/hashcode"() {
    given:
    def one = new JsonReportObject.Builder().name("name").authors("authors").url("url").year("year").license(license).build()
    def two = new JsonReportObject.Builder().name("name").authors("authors").url("url").year("year").license(license).build()

    expect:
    // Values
    one.name == two.name
    one.authors == two.authors
    one.url == two.url
    one.year == two.year
    one.license == two.license
    // Auto generated
    one.hashCode() == two.hashCode()
    // one == two
    one.toString() == two.toString()
  }
}
