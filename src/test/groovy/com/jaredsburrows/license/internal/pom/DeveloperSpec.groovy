package com.jaredsburrows.license.internal.pom

import spock.lang.Specification

final class DeveloperSpec extends Specification {
  def sut = new Developer(name: "name")

  def "name"() {
    expect:
    sut.name == "name"
    sut.getName() == "name"
  }
}
