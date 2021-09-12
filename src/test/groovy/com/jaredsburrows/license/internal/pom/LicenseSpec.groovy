package com.jaredsburrows.license.internal.pom

import spock.lang.Specification

final class LicenseSpec extends Specification {
  private final def sut = new License(
    name: 'name',
    url: 'url'
  )

  def 'get name'() {
    expect:
    sut.name == 'name'
  }

  def 'url'() {
    expect:
    sut.url == 'url'
  }

  def 'equals and hashcode'() {
    given:
    def one = new License(
      name: 'name',
      url: 'url'
    )
    def two = new License(
      name: 'name',
      url: 'url'
    )

    expect:
    one.name == two.name
    one.url == two.url
  }
}
