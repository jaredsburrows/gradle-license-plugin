package com.jaredsburrows.license.internal.report

import com.jaredsburrows.license.internal.License
import com.jaredsburrows.license.internal.report.json.JsonReportObject
import spock.lang.Specification

/**
 * @author <a href="mailto:jaredsburrows@gmail.com">Jared Burrows</a>
 */
final class JsonReportObjectSpec extends Specification {
  def license = License.builder()
    .name("name")
    .url("url")
    .build()
  def sut = JsonReportObject.builder()
    .name("name")
    .developers("developers")
    .url("url")
    .year("year")
    .license(license)
    .build()

  def "test get name"() {
    expect:
    sut.name == "name"
    sut.getName() == "name"
  }

  def "test get authors"() {
    expect:
    sut.developers == "developers"
    sut.getDevelopers() == "developers"
  }

  def "test get url"() {
    expect:
    sut.url == "url"
    sut.getUrl() == "url"
  }

  def "test get year"() {
    expect:
    sut.year == "year"
    sut.getYear() == "year"
  }

  def "test get license"() {
    expect:
    sut.license == license
    sut.getLicense() == license
  }

  def "test to json - all values"() {
    given:
    def json = sut.jsonObject()

    expect:
    json.project == "name"
    json.developers == "developers"
    json.url == "url"
    json.year == "year"
    json.license == "name"
    json.license_url == "url"
  }

  def "test to json - with missing values"() {
    given:
    def json = JsonReportObject.builder()
      .name("name")
      .developers(null)
      .url(null)
      .year(null)
      .license(null)
      .build()
      .jsonObject()

    expect:
    json.project == "name"
    !json.developers
    !json.url
    !json.year
    !json.license
    !json.license_url
  }

  def "test equals/hashcode"() {
    given:
    def one = JsonReportObject.builder()
      .name("name")
      .developers("developers")
      .url("url")
      .year("year")
      .license(license)
      .build()
    def two = JsonReportObject.builder()
      .name("name")
      .developers("developers")
      .url("url")
      .year("year")
      .license(license)
      .build()

    expect:
    // Values
    one.name == two.name
    one.developers == two.developers
    one.url == two.url
    one.year == two.year
    one.license == two.license
    // Auto generated
    one.hashCode() == two.hashCode()
    // one == two
    one.toString() == two.toString()
  }
}
