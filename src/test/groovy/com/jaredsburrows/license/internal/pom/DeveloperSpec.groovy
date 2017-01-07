package com.jaredsburrows.license.internal.pom

import spock.lang.Specification

/**
 * @author <a href="mailto:jaredsburrows@gmail.com">Jared Burrows</a>
 */
final class DeveloperSpec extends Specification {
  def sut = new Developer(name: "name")

  def "test name"() {
    expect:
    sut.name == "name"
    sut.getName() == "name"
  }
}
