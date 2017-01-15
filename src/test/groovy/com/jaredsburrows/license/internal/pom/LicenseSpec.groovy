package com.jaredsburrows.license.internal.pom

import spock.lang.Specification

/**
 * @author <a href="mailto:jaredsburrows@gmail.com">Jared Burrows</a>
 */
final class LicenseSpec extends Specification {
  def sut = new License(name: "name", url: "url")

  def "get name"() {
    expect:
    sut.name == "name"
    sut.getName() == "name"
  }

  def "url"() {
    expect:
    sut.url == "url"
    sut.getUrl() == "url"
  }

  def "equals and hashcode"() {
    given:
    def one = new License(name: "name", url: "url")
    def two = new License(name: "name", url: "url")

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
