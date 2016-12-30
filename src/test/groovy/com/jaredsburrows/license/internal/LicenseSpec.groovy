package com.jaredsburrows.license.internal

import spock.lang.Specification

/**
 * @author <a href="mailto:jaredsburrows@gmail.com">Jared Burrows</a>
 */
final class LicenseSpec extends Specification {
  def sut = License.builder().name("name").url("url").build()

  def "test get name"() {
    expect:
    sut.name == "name"
    sut.getName() == "name"
  }

  def "test url"() {
    expect:
    sut.url == "url"
    sut.getUrl() == "url"
  }

  def "test equals/hashcode"() {
    given:
    def one = License.builder().name("name").url("url").build()
    def two = License.builder().name("name").url("url").build()

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
